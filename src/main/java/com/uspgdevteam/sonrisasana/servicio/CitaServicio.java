package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import com.uspgdevteam.sonrisasana.entidad.Usuario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

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

            Paciente paciente = em.find(Paciente.class, cita.getPaciente().getId());
            Usuario odontologo = em.find(Usuario.class, cita.getOdontologo().getId());
            Tratamiento tratamiento = em.find(Tratamiento.class, cita.getTratamiento().getId());

            cita.setPaciente(paciente);
            cita.setOdontologo(odontologo);
            cita.setTratamiento(tratamiento);

            // Estado -> TEXTO (NO entidad)
            cita.setEstado("CONFIRMADA");

            // Fechas
            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(tratamiento.getDuracionMinutos());
            cita.setFechaFin(fin);

            // Precios
            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(tratamiento.getCosto());
            cita.setTotal(PRECIO_BASE.add(tratamiento.getCosto()));

            em.persist(cita);
            return cita;
        });
    }

    // ==========================================================
    // ACTUALIZAR CITA
    // ==========================================================
    public Cita actualizar(Cita cita) {
        return executeInTx(em -> {

            Paciente paciente = em.find(Paciente.class, cita.getPaciente().getId());
            Usuario odontologo = em.find(Usuario.class, cita.getOdontologo().getId());
            Tratamiento tratamiento = em.find(Tratamiento.class, cita.getTratamiento().getId());

            cita.setPaciente(paciente);
            cita.setOdontologo(odontologo);
            cita.setTratamiento(tratamiento);

            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(tratamiento.getDuracionMinutos());
            cita.setFechaFin(fin);

            // Precios
            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(tratamiento.getCosto());
            cita.setTotal(PRECIO_BASE.add(tratamiento.getCosto()));

            return em.merge(cita);
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
                        .getResultList());
    }

    public List<Cita> listarPorPaciente(Paciente paciente) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT c FROM Cita c WHERE c.paciente = :p ORDER BY c.fechaInicio DESC",
                                Cita.class)
                        .setParameter("p", paciente)
                        .getResultList());
    }

    public List<Cita> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT c FROM Cita c WHERE c.fechaInicio BETWEEN :ini AND :fin ORDER BY c.fechaInicio ASC",
                                Cita.class)
                        .setParameter("ini", inicio)
                        .setParameter("fin", fin)
                        .getResultList());
    }
}
