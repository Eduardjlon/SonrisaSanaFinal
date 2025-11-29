package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.*;
import jakarta.persistence.EntityManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class CitaServicio extends GenericService<Cita> {

    private static final BigDecimal PRECIO_BASE = new BigDecimal("300.00");

    public CitaServicio() {
        super(Cita.class);
    }

    // ==========================================================
    // CREAR CITA
    // ==========================================================
    public Cita crear(Cita cita) {
        return executeInTx(em -> {

            // Tratamiento
            Tratamiento t = em.find(Tratamiento.class, cita.getTratamiento().getId());

            // Fecha fin automática
            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(t.getDuracionMinutos());
            cita.setFechaFin(fin);

            // Estado → ENTIDAD REAL
            EstadoCita estado = em.find(EstadoCita.class, "CONFIRMADA");
            cita.setEstado(estado);

            // Costos
            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(t.getCosto());
            cita.setTotal(PRECIO_BASE.add(t.getCosto()));

            // Validar disponibilidad
            if (!estaDisponibleInternal(em, cita.getOdontologo(), inicio, fin)) {
                throw new RuntimeException("El odontólogo NO está disponible en ese horario.");
            }

            em.persist(cita);
            em.flush();

            return cita;
        });
    }

    // ==========================================================
    // ACTUALIZAR CITA
    // ==========================================================
    public Cita actualizar(Cita cita) {

        return executeInTx(em -> {

            Tratamiento t = em.find(Tratamiento.class, cita.getTratamiento().getId());

            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(t.getDuracionMinutos());
            cita.setFechaFin(fin);

            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(t.getCosto());
            cita.setTotal(PRECIO_BASE.add(t.getCosto()));

            if (!estaDisponibleActualizarInternal(em, cita, inicio, fin)) {
                throw new RuntimeException("El odontólogo NO está disponible en el nuevo horario.");
            }

            Cita merged = em.merge(cita);
            em.flush();

            return merged;
        });
    }

    // ==========================================================
    // DISPONIBILIDAD (CREAR)
    // ==========================================================
    private boolean estaDisponibleInternal(EntityManager em, Usuario odontologo,
                                           LocalDateTime inicio, LocalDateTime fin) {

        String jpql =
                "SELECT COUNT(c) FROM Cita c " +
                        "WHERE c.odontologo = :odo " +
                        "AND c.estado.nombre <> 'CANCELADA' " +
                        "AND (c.fechaInicio < :fin AND c.fechaFin > :inicio)";

        Long count = em.createQuery(jpql, Long.class)
                .setParameter("odo", odontologo)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getSingleResult();

        return count == 0;
    }

    // ==========================================================
    // DISPONIBILIDAD (ACTUALIZAR)
    // ==========================================================
    private boolean estaDisponibleActualizarInternal(EntityManager em, Cita cita,
                                                     LocalDateTime inicio, LocalDateTime fin) {

        String jpql =
                "SELECT COUNT(c) FROM Cita c " +
                        "WHERE c.odontologo = :odo " +
                        "AND c.id <> :id " +
                        "AND c.estado.nombre <> 'CANCELADA' " +
                        "AND (c.fechaInicio < :fin AND c.fechaFin > :inicio)";

        Long count = em.createQuery(jpql, Long.class)
                .setParameter("odo", cita.getOdontologo())
                .setParameter("id", cita.getId())
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getSingleResult();

        return count == 0;
    }

    // ==========================================================
    // CANCELAR CITA
    // ==========================================================
    public void cancelar(Long citaId) {
        executeInTxVoid(em -> {
            Cita cita = em.find(Cita.class, citaId);

            EstadoCita cancelada = em.find(EstadoCita.class, "CANCELADA");
            cita.setEstado(cancelada);

            em.merge(cita);
        });
    }

    // ==========================================================
    // REPROGRAMAR CITA
    // ==========================================================
    public void reprogramar(Long citaId, LocalDateTime nuevaFecha) {

        executeInTxVoid(em -> {

            Cita cita = em.find(Cita.class, citaId);
            Tratamiento t = cita.getTratamiento();

            LocalDateTime nuevaFin = nuevaFecha.plusMinutes(t.getDuracionMinutos());

            if (!estaDisponibleInternal(em, cita.getOdontologo(), nuevaFecha, nuevaFin)) {
                throw new RuntimeException("El odontólogo NO está disponible en la nueva fecha.");
            }

            // Registrar historial
            HistorialReprogramacion hist = new HistorialReprogramacion();
            hist.setCita(cita);
            hist.setFechaAnteriorInicio(cita.getFechaInicio());
            hist.setFechaAnteriorFin(cita.getFechaFin());
            hist.setFechaNuevaInicio(nuevaFecha);
            hist.setFechaNuevaFin(nuevaFin);
            hist.setMotivo("Reprogramación desde el sistema");

            em.persist(hist);

            // Actualizar cita
            EstadoCita repro = em.find(EstadoCita.class, "REPROGRAMADA");
            cita.setEstado(repro);
            cita.setFechaInicio(nuevaFecha);
            cita.setFechaFin(nuevaFin);

            em.merge(cita);
        });
    }

    // ==========================================================
    // LISTADOS
    // ==========================================================

    public List<Cita> listarTodas() {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cita c ORDER BY c.fechaInicio DESC", Cita.class)
                        .getResultList());
    }

    public List<Cita> listarPorOdontologo(Usuario odontologo) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT c FROM Cita c WHERE c.odontologo = :odo ORDER BY c.fechaInicio DESC",
                                Cita.class)
                        .setParameter("odo", odontologo)
                        .getResultList()
        );
    }

    public List<Cita> listarPorPaciente(Paciente paciente) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT c FROM Cita c WHERE c.paciente = :p ORDER BY c.fechaInicio DESC",
                                Cita.class)
                        .setParameter("p", paciente)
                        .getResultList()
        );
    }

    public List<Cita> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT c FROM Cita c WHERE c.fechaInicio BETWEEN :ini AND :fin ORDER BY c.fechaInicio ASC",
                                Cita.class)
                        .setParameter("ini", inicio)
                        .setParameter("fin", fin)
                        .getResultList()
        );
    }
}
