package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Factura;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import com.uspgdevteam.sonrisasana.entidad.Usuario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class CitaServicio extends GenericService<Cita> {

    private static final BigDecimal PRECIO_BASE = new BigDecimal("300.00");

    @Inject
    private FacturaServicio facturaServicio;

    public CitaServicio() {
        super(Cita.class);
    }

    // Crear cita
    public Cita crear(Cita cita) {
        return executeInTx(em -> {
            Paciente paciente = em.find(Paciente.class, cita.getPaciente().getId());
            Usuario odontologo = em.find(Usuario.class, cita.getOdontologo().getId());
            Tratamiento tratamiento = em.find(Tratamiento.class, cita.getTratamiento().getId());

            cita.setPaciente(paciente);
            cita.setOdontologo(odontologo);
            cita.setTratamiento(tratamiento);

            if (cita.getEstado() == null || cita.getEstado().isBlank()) {
                cita.setEstado("PENDIENTE");
            }

            LocalDateTime inicio = cita.getFechaInicio();
            LocalDateTime fin = inicio.plusMinutes(tratamiento.getDuracionMinutos());
            cita.setFechaFin(fin);

            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(tratamiento.getCosto());
            cita.setTotal(PRECIO_BASE.add(tratamiento.getCosto()));

            em.persist(cita);
            return cita;
        });
    }

    // Actualizar cita
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

            cita.setPrecioBase(PRECIO_BASE);
            cita.setPrecioTratamiento(tratamiento.getCosto());
            cita.setTotal(PRECIO_BASE.add(tratamiento.getCosto()));

            return em.merge(cita);
        });
    }

    // Actualizar solo estado
    public void actualizarEstado(Long id, String nuevoEstado) {
        executeInTx(em -> {
            Cita c = em.find(Cita.class, id);
            if (c != null) {
                String estadoAnterior = c.getEstado();
                c.setEstado(nuevoEstado);
                em.merge(c);

                if (!"CONFIRMADA".equals(estadoAnterior) && "CONFIRMADA".equals(nuevoEstado)) {
                    List<Factura> existentes = em.createQuery(
                                    "SELECT f FROM Factura f WHERE f.cita = :cita",
                                    Factura.class)
                            .setParameter("cita", c)
                            .getResultList();

                    if (existentes.isEmpty()) {
                        Factura factura = new Factura();
                        factura.setCita(c);
                        factura.setPaciente(c.getPaciente());
                        factura.setSubtotal(c.getTotal());

                        facturaServicio.crearFacturaDesdeCita(c);

                    }
                }
            }
            return null;
        });
    }

    // Listados
    public List<Cita> listarTodas() {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cita c ORDER BY c.fechaInicio DESC", Cita.class)
                        .getResultList());
    }

    public List<Cita> listarPorOdontologo(Usuario odontologo) {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cita c WHERE c.odontologo = :odo ORDER BY c.fechaInicio DESC",
                                Cita.class)
                        .setParameter("odo", odontologo)
                        .getResultList());
    }

    public List<Cita> listarPorPaciente(Paciente paciente) {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cita c WHERE c.paciente = :p ORDER BY c.fechaInicio DESC",
                                Cita.class)
                        .setParameter("p", paciente)
                        .getResultList());
    }

    public List<Cita> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cita c WHERE c.fechaInicio BETWEEN :ini AND :fin ORDER BY c.fechaInicio ASC",
                                Cita.class)
                        .setParameter("ini", inicio)
                        .setParameter("fin", fin)
                        .getResultList());
    }
}
