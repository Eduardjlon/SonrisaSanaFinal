package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.*;
import com.uspgdevteam.sonrisasana.servicio.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Named
@ViewScoped
public class CitaBean implements Serializable {

    @Inject
    private CitaServicio citaServicio;

    @Inject
    private PacienteServicio pacienteServicio;

    @Inject
    private UsuarioServicio usuarioServicio;

    @Inject
    private TratamientoServicio tratamientoServicio;

    @Inject
    private HistorialReprogramacionServicio historialServicio;

    private ScheduleModel modelo;
    private Cita cita; // 🔥 NUNCA puede quedar null

    private List<Paciente> pacientes;
    private List<Usuario> odontologos;
    private List<Tratamiento> tratamientos;

    private Long pacienteId;
    private Long odontologoId;
    private Long tratamientoId;

    private final EstadoCita[] estados = EstadoCita.values();

    @PostConstruct
    public void init() {
        pacientes = pacienteServicio.listar();
        odontologos = usuarioServicio.listarOdontologos();
        tratamientos = tratamientoServicio.listar();

        modelo = new DefaultScheduleModel();
        cargarCitasEnModelo();

        // 🔥 FIX PRINCIPAL: evita Target Unreachable al cargar la página
        prepararNuevaCita();
    }

    /** 🔹 Inicializa cita para evitar null */
    public void prepararNuevaCita() {
        cita = new Cita();
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setFechaInicio(LocalDateTime.now());
        cita.setFechaFin(LocalDateTime.now().plusMinutes(30));

        pacienteId = null;
        odontologoId = null;
        tratamientoId = null;
    }

    private void cargarCitasEnModelo() {
        modelo = new DefaultScheduleModel();

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.minusMonths(1).atStartOfDay();
        LocalDateTime fin = hoy.plusMonths(1).atTime(23, 59);

        List<Cita> citas = citaServicio.listarPorRango(inicio, fin);

        for (Cita c : citas) {
            String titulo = c.getPaciente().getNombreCompleto() +
                    " - " + c.getTratamiento().getNombre();

            DefaultScheduleEvent<Cita> ev =
                    DefaultScheduleEvent.<Cita>builder()
                            .title(titulo)
                            .startDate(c.getFechaInicio())
                            .endDate(c.getFechaFin())
                            .data(c)
                            .build();

            modelo.addEvent(ev);
        }
    }

    /** 🔹 Clic en un día vacío */
    public void onDateSelect(SelectEvent<LocalDateTime> event) {
        prepararNuevaCita();

        LocalDateTime inicio = event.getObject();
        cita.setFechaInicio(inicio);
        cita.setFechaFin(inicio.plusMinutes(30));
    }

    /** 🔹 Clic en evento existente */
    public void onEventSelect(SelectEvent<ScheduleEvent<Cita>> event) {
        Cita c = event.getObject().getData();
        this.cita = c;

        pacienteId = c.getPaciente() != null ? c.getPaciente().getId() : null;
        odontologoId = c.getOdontologo() != null ? c.getOdontologo().getId() : null;
        tratamientoId = c.getTratamiento() != null ? c.getTratamiento().getId() : null;
    }

    public void guardarCita() {
        try {

            if (pacienteId == null || odontologoId == null || tratamientoId == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Datos incompletos",
                                "Debe seleccionar paciente, odontólogo y tratamiento."));
                return;
            }

            Paciente pac = pacienteServicio.findById(pacienteId);
            Usuario odo = usuarioServicio.findById(odontologoId);
            Tratamiento tra = tratamientoServicio.findById(tratamientoId);

            cita.setPaciente(pac);
            cita.setOdontologo(odo);
            cita.setTratamiento(tra);

            // Validar horario laboral
            if (!citaServicio.esHorarioValido(cita)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Fuera de horario",
                                "El odontólogo no atiende en ese horario."));
                return;
            }

            // Validar traslape
            if (citaServicio.hayTraslape(cita)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Conflicto de agenda",
                                "Ya existe otra cita asignada en este horario."));
                return;
            }

            boolean esNueva = (cita.getId() == null);

            citaServicio.guardar(cita);

            if (esNueva) {
                historialServicio.guardar(
                        new HistorialReprogramacion(cita, "Cita creada"));
            } else {
                historialServicio.guardar(
                        new HistorialReprogramacion(cita, "Cita actualizada / reprogramada"));
            }

            cargarCitasEnModelo();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Cita guardada correctamente"));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo guardar la cita."));
        }
    }

    // GETTERS PARA JSF
    public ScheduleModel getModelo() { return modelo; }
    public Cita getCita() { return cita; }

    public List<Paciente> getPacientes() { return pacientes; }
    public List<Usuario> getOdontologos() { return odontologos; }
    public List<Tratamiento> getTratamientos() { return tratamientos; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getOdontologoId() { return odontologoId; }
    public void setOdontologoId(Long odontologoId) { this.odontologoId = odontologoId; }

    public Long getTratamientoId() { return tratamientoId; }
    public void setTratamientoId(Long tratamientoId) { this.tratamientoId = tratamientoId; }

    public EstadoCita[] getEstados() { return estados; }
}
