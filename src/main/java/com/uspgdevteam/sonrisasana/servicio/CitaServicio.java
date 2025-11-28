package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import com.uspgdevteam.sonrisasana.entidad.Usuario;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class CitaServicio {

    @PersistenceUnit(unitName = "SonrisaPU")
    private EntityManagerFactory emf;

    private EntityManager em() {
        return emf.createEntityManager();
    }

    private static final BigDecimal PRECIO_BASE = new BigDecimal("300.00");


    // =====================================================
    // CREAR
    // =====================================================
    public Cita crear(Cita cita) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Tratamiento t = em.find(Tratamiento.class, cita.getTratamiento().getId());

            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(t.getDuracionMinutos());
            cita.setFechaFin(fin);

            cita.setEstado(Cita.EstadoCita.CONFIRMADA);

            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(t.getCosto());
            cita.setTotal(PRECIO_BASE.add(t.getCosto()));

            if (!estaDisponibleInternal(em, cita.getOdontologo(), inicio, fin)) {
                throw new RuntimeException("El odontólogo NO está disponible en ese horario.");
            }

            em.persist(cita);

            tx.commit();
            return cita;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } finally {
            em.close();
        }
    }

    // =====================================================
    // ACTUALIZAR
    // =====================================================
    public Cita actualizar(Cita cita) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Tratamiento t = em.find(Tratamiento.class, cita.getTratamiento().getId());

            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(t.getDuracionMinutos());
            cita.setFechaFin(fin);

            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(t.getCosto());
            cita.setTotal(PRECIO_BASE.add(t.getCosto()));

            if (!estaDisponibleActualizarInternal(em, cita, inicio, fin)) {
                throw new RuntimeException("El odontólogo NO está disponible en ese horario (actualización).");
            }

            Cita merged = em.merge(cita);

            tx.commit();
            return merged;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } finally {
            em.close();
        }
    }

    // =====================================================
    // DISPONIBILIDAD (CREAR)
    // =====================================================
    private boolean estaDisponibleInternal(EntityManager em, Usuario odontologo, LocalDateTime inicio, LocalDateTime fin) {
        String jpql =
                "SELECT COUNT(c) FROM Cita c " +
                        "WHERE c.odontologo = :o " +
                        "AND c.estado <> :cancelada " +
                        "AND (c.fechaInicio < :fin AND c.fechaFin > :inicio)";

        Long count = em.createQuery(jpql, Long.class)
                .setParameter("o", odontologo)
                .setParameter("cancelada", Cita.EstadoCita.CANCELADA)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getSingleResult();

        return count == 0;
    }

    // =====================================================
    // DISPONIBILIDAD (ACTUALIZAR)
    // =====================================================
    private boolean estaDisponibleActualizarInternal(EntityManager em, Cita cita, LocalDateTime inicio, LocalDateTime fin) {

        String jpql =
                "SELECT COUNT(c) FROM Cita c " +
                        "WHERE c.odontologo = :o " +
                        "AND c.id <> :id " +
                        "AND c.estado <> :cancelada " +
                        "AND (c.fechaInicio < :fin AND c.fechaFin > :inicio)";

        Long count = em.createQuery(jpql, Long.class)
                .setParameter("o", cita.getOdontologo())
                .setParameter("id", cita.getId())
                .setParameter("cancelada", Cita.EstadoCita.CANCELADA)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getSingleResult();

        return count == 0;
    }

    // =====================================================
    // BUSCAR POR ID
    // =====================================================
    public Cita buscarPorId(Long id) {
        EntityManager em = em();
        try {
            return em.find(Cita.class, id);
        } finally {
            em.close();
        }
    }


    // =====================================================
    // CANCELAR
    // =====================================================
    public void cancelar(Long citaId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Cita cita = em.find(Cita.class, citaId);
            cita.setEstado(Cita.EstadoCita.CANCELADA);

            em.merge(cita);
            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } finally {
            em.close();
        }
    }

    // =====================================================
    // REPROGRAMAR
    // =====================================================
    public void reprogramar(Long citaId, LocalDateTime nuevaFecha) {

        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Cita cita = em.find(Cita.class, citaId);
            Tratamiento t = cita.getTratamiento();

            LocalDateTime nuevaFechaFin = nuevaFecha.plusMinutes(t.getDuracionMinutos());

            if (!estaDisponibleInternal(em, cita.getOdontologo(), nuevaFecha, nuevaFechaFin)) {
                throw new RuntimeException("El odontólogo NO está disponible en la nueva fecha.");
            }

            cita.setFechaInicio(nuevaFecha);
            cita.setFechaFin(nuevaFechaFin);
            cita.setEstado(Cita.EstadoCita.REPROGRAMADA);

            em.merge(cita);
            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;

        } finally {
            em.close();
        }
    }

    // =====================================================
    // LISTAR TODAS
    // =====================================================
    public List<Cita> listarTodas() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT c FROM Cita c ORDER BY c.fechaInicio DESC", Cita.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // =====================================================
    // LISTAR POR ODONTOLOGO
    // =====================================================
    public List<Cita> listarPorOdontologo(Usuario odontologo) {
        EntityManager em = em();
        try {
            return em.createQuery(
                            "SELECT c FROM Cita c WHERE c.odontologo = :o ORDER BY c.fechaInicio DESC",
                            Cita.class)
                    .setParameter("o", odontologo)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // =====================================================
    // LISTAR POR PACIENTE
    // =====================================================
    public List<Cita> listarPorPaciente(Paciente paciente) {
        EntityManager em = em();
        try {
            return em.createQuery(
                            "SELECT c FROM Cita c WHERE c.paciente = :p ORDER BY c.fechaInicio DESC",
                            Cita.class)
                    .setParameter("p", paciente)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // =====================================================
    // LISTAR POR RANGO
    // =====================================================
    public List<Cita> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        EntityManager em = em();
        try {
            return em.createQuery(
                            "SELECT c FROM Cita c WHERE c.fechaInicio BETWEEN :ini AND :fin ORDER BY c.fechaInicio ASC",
                            Cita.class)
                    .setParameter("ini", inicio)
                    .setParameter("fin", fin)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
