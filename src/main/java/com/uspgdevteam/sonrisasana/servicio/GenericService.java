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

    // ==========================
    //      BÚSQUEDAS
    // ==========================

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

    // ==========================
    //      GUARDAR / EDITAR
    // ==========================

    public T guardar(T entity) {
        return save(entity);
    }

    /**
     * CORREGIDO:
     * - persist() → para nuevos (ID null)
     * - merge() → para existentes
     * - flush() → fuerza el SQL inmediatamente (evita datos fantasma)
     */
    public T save(T entity) {
        return executeInTx(em -> {

            try {
                // Obtener método getId() por reflexión
                Method getIdMethod = entityClass.getMethod("getId");
                Long id = (Long) getIdMethod.invoke(entity);

                if (id == null) {
                    // NUEVO → persist()
                    em.persist(entity);
                    em.flush(); // fuerza INSERT
                    return entity;
                } else {
                    // EXISTENTE → merge()
                    T merged = em.merge(entity);
                    em.flush(); // fuerza UPDATE
                    return merged;
                }

            } catch (Exception ex) {
                throw new RuntimeException(
                        "Error al guardar entidad " + entityClass.getSimpleName(),
                        ex
                );
            }

        });
    }

    // ==========================
    //          ELIMINAR
    // ==========================

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

    // ==========================
    //       TRANSACCIONES
    // ==========================

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
