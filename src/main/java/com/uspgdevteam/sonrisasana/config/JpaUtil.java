package com.uspgdevteam.sonrisasana.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utilidad centralizada para crear EntityManager usando RESOURCE_LOCAL.
 */
@Named
@ApplicationScoped
public class JpaUtil {

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        this.emf = Persistence.createEntityManagerFactory("SonrisaPU");
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @PreDestroy
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
