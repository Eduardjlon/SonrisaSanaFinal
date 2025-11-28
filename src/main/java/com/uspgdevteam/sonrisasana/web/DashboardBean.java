package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Cita.EstadoCita;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.servicio.CitaServicio;
import com.uspgdevteam.sonrisasana.servicio.PacienteServicio;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Named
@RequestScoped
public class DashboardBean implements Serializable {

    @Inject
    private PacienteServicio pacienteServicio;

    @Inject
    private CitaServicio citaServicio;

    private long pacientesSemana;
    private long citasHoy;
    private long citasCanceladasHoy;
    private BigDecimal ingresosMes;
    private long tratamientosSemana;
    private long odontologosActivos;

    @PostConstruct
    public void init() {

        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioSemana = hoy.minusDays(7);
            LocalDateTime semanaInicio = inicioSemana.atStartOfDay();

            // ==========================
            // PACIENTES NUEVOS ESTA SEMANA
            // ==========================
            List<Paciente> pacientes = pacienteServicio.listar();
            if (pacientes == null) pacientes = Collections.emptyList();

            pacientesSemana = pacientes.stream()
                    .filter(p -> p.getFechaCreacionExpediente() != null &&
                            !p.getFechaCreacionExpediente().isBefore(semanaInicio))
                    .count();

            // ==========================
            // CITAS DEL DÍA
            // ==========================
            LocalDateTime hoyInicio = hoy.atStartOfDay();
            LocalDateTime hoyFin = hoy.plusDays(1).atStartOfDay().minusSeconds(1);

            List<Cita> citasHoyList = citaServicio.listarPorRango(hoyInicio, hoyFin);
            if (citasHoyList == null) citasHoyList = Collections.emptyList();

            citasHoy = citasHoyList.size();

            citasCanceladasHoy = citasHoyList.stream()
                    .filter(c -> c != null && c.getEstado() == EstadoCita.CANCELADA)
                    .count();

            // ==========================
            // INGRESOS DEL MES
            // (reemplazar cuando tengas módulo de pagos)
            // ==========================
            ingresosMes = citasHoyList.stream()
                    .map(Cita::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ==========================
            // TRATAMIENTOS EN SEMANA (placeholder)
            // ==========================
            tratamientosSemana = citasHoyList.size();

            // ==========================
            // ODONTÓLOGOS ACTIVOS HOY
            // ==========================
            odontologosActivos = citasHoyList.stream()
                    .filter(c -> c != null &&
                            c.getOdontologo() != null &&
                            c.getOdontologo().getId() != null)
                    .map(c -> c.getOdontologo().getId())
                    .distinct()
                    .count();

        } catch (Exception e) {
            e.printStackTrace();

            pacientesSemana = 0;
            citasHoy = 0;
            citasCanceladasHoy = 0;
            ingresosMes = BigDecimal.ZERO;
            tratamientosSemana = 0;
            odontologosActivos = 0;
        }
    }

    // GETTERS
    public long getPacientesSemana() { return pacientesSemana; }
    public long getCitasHoy() { return citasHoy; }
    public long getCitasCanceladasHoy() { return citasCanceladasHoy; }
    public BigDecimal getIngresosMes() { return ingresosMes; }
    public long getTratamientosSemana() { return tratamientosSemana; }
    public long getOdontologosActivos() { return odontologosActivos; }
}
