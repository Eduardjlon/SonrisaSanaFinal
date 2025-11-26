package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.config.JpaUtil;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

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

    /**
     * Alias para save() pero usando "guardar" como en UsuarioServicio y PacienteServicio.
     */
    public T guardar(T entity) {
        return save(entity);
    }

    public T save(T entity) {
        return executeInTx(em -> em.merge(entity));
    }


    // ==========================
    //          ELIMINAR
    // ==========================

    /**
     * Elimina usando el objeto (como en UsuarioBean y PacienteBean).
     */
    public void eliminar(T entity) {
        executeInTxVoid(em -> {
            T ref = em.contains(entity) ? entity : em.merge(entity);
            em.remove(ref);
        });
    }

    /**
     * Elimina usando el ID (método original).
     */
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
