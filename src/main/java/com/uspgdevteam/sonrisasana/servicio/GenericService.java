package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.config.JpaUtil;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GenericService<T> {

    @Inject
    protected JpaUtil jpaUtil;

    private final Class<T> entityClass;

    protected GenericService(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ==========================================================
    // BÚSQUEDAS
    // ==========================================================

    public T findById(Long id) {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = jpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM " + entityClass.getSimpleName() + " e",
                    entityClass
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // ==========================================================
    // GUARDAR / EDITAR
    // ==========================================================

    public T guardar(T entity) {
        return save(entity);
    }

    /**
     * GUARDA ENTIDAD SEGURA Y ESTABLE PARA JPA + POSTGRES
     * - persist() para nuevos (ID null)
     * - merge() para existentes
     * - flush() para forzar SQL inmediato
     */
    public T save(T entity) {
        return executeInTx(em -> {

            try {
                // Obtener ID por reflexión
                Method getId = entityClass.getMethod("getId");
                Long id = (Long) getId.invoke(entity);

                if (id == null) {
                    // NUEVA ENTIDAD
                    em.persist(entity);
                    em.flush();
                    return entity; // persist mantiene referencia original
                } else {
                    // EXISTENTE → SE NECESITA merge()
                    T merged = em.merge(entity);
                    em.flush();
                    return merged;
                }

            } catch (Exception ex) {
                throw new RuntimeException(
                        "Error al guardar entidad " + entityClass.getSimpleName() + ": " + ex.getMessage(),
                        ex
                );
            }
        });
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================

    public void eliminar(T entity) {
        executeInTxVoid(em -> {
            T ref = em.contains(entity) ? entity : em.merge(entity);
            em.remove(ref);
        });
    }

    public void delete(Long id) {
        executeInTxVoid(em -> {
            T ref = em.find(entityClass, id);
            if (ref != null) {
                em.remove(ref);
            }
        });
    }

    // ==========================================================
    // TRANSACCIONES
    // ==========================================================

    protected <R> R executeInTx(Function<EntityManager, R> function) {
        EntityManager em = jpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;

        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error en transacción: " + e.getMessage(), e);

        } finally {
            em.close();
        }
    }

    protected void executeInTxVoid(Consumer<EntityManager> consumer) {
        executeInTx(em -> {
            consumer.accept(em);
            return null;
        });
    }
}
