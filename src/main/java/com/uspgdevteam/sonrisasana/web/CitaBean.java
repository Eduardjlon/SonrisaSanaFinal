package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.CitaServicio;
import com.uspgdevteam.sonrisasana.servicio.PacienteServicio;
import com.uspgdevteam.sonrisasana.servicio.TratamientoServicio;
import com.uspgdevteam.sonrisasana.servicio.UsuarioServicio;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
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

    private ScheduleModel eventModel;

    private Cita cita;
    private ScheduleEvent<?> evento;

    private List<Paciente> pacientes;
    private List<Usuario> odontologos;
    private List<Tratamiento> tratamientos;

    // IDs para evitar entidades detached
    private Long pacienteId;
    private Long odontologoId;
    private Long tratamientoId;

    @PostConstruct
    public void init() {
        cargarListas();
        cargarEventos();
        nuevaCita();
    }

    private void cargarListas() {
        pacientes = pacienteServicio.listar();
        odontologos = usuarioServicio.listarOdontologos();
        tratamientos = tratamientoServicio.listar();
    }

    public void nuevaCita() {
        cita = new Cita();
        pacienteId = null;
        odontologoId = null;
        tratamientoId = null;
    }

    private void cargarEventos() {
        eventModel = new DefaultScheduleModel();

        List<Cita> lista = citaServicio.listarTodas();
        for (Cita c : lista) {

            String titulo = c.getPaciente().getNombreCompleto()
                    + " - " + c.getTratamiento().getNombre();

            DefaultScheduleEvent<?> ev = DefaultScheduleEvent.builder()
                    .title(titulo)
                    .startDate(c.getFechaInicio())
                    .endDate(c.getFechaFin())
                    .data(c)
                    .build();

            eventModel.addEvent(ev);
        }
    }

    // Seleccionar fecha vacía
    public void onDateSelect(SelectEvent<LocalDateTime> event) {
        nuevaCita();
        LocalDateTime inicio = event.getObject();
        cita.setFechaInicio(inicio);
    }

    // Seleccionar cita existente
    public void onEventSelect(SelectEvent<ScheduleEvent<?>> ev) {
        evento = ev.getObject();
        cita = (Cita) evento.getData();

        pacienteId = cita.getPaciente().getId();
        odontologoId = cita.getOdontologo().getId();
        tratamientoId = cita.getTratamiento().getId();
    }

    // Actualizar precios
    public void actualizarPrecios() {
        if (tratamientoId == null) {
            cita.setPrecioTratamiento(null);
            cita.setTotal(cita.getPrecioBase());
            return;
        }

        Tratamiento t = tratamientoServicio.findById(tratamientoId);

        cita.setPrecioTratamiento(t.getCosto());
        cita.setTotal(cita.getPrecioBase().add(t.getCosto()));
    }

    // GUARDAR — CORREGIDO
    public void guardar() {
        try {

            // Validaciones
            if (pacienteId == null) {
                mensaje("Debe seleccionar un paciente.", FacesMessage.SEVERITY_ERROR);
                return;
            }
            if (odontologoId == null) {
                mensaje("Debe seleccionar un odontólogo.", FacesMessage.SEVERITY_ERROR);
                return;
            }
            if (tratamientoId == null) {
                mensaje("Debe seleccionar un tratamiento.", FacesMessage.SEVERITY_ERROR);
                return;
            }
            if (cita.getFechaInicio() == null) {
                mensaje("Debe seleccionar fecha y hora.", FacesMessage.SEVERITY_ERROR);
                return;
            }

            // Recuperar entidades manejadas
            Paciente pac = pacienteServicio.findById(pacienteId);
            Usuario odo = usuarioServicio.findById(odontologoId);
            Tratamiento trat = tratamientoServicio.findById(tratamientoId);

            cita.setPaciente(pac);
            cita.setOdontologo(odo);
            cita.setTratamiento(trat);

            // Calcular fecha fin REAL
            cita.setFechaFin(cita.getFechaInicio().plusMinutes(trat.getDuracionMinutos()));

            // Costos
            cita.setPrecioTratamiento(trat.getCosto());
            cita.setTotal(cita.getPrecioBase().add(trat.getCosto()));

            // Crear o actualizar
            if (cita.getId() == null) {
                citaServicio.crear(cita);
                mensaje("Cita creada correctamente", FacesMessage.SEVERITY_INFO);
            } else {
                citaServicio.actualizar(cita);
                mensaje("Cita actualizada correctamente", FacesMessage.SEVERITY_INFO);
            }

            cargarEventos();
            nuevaCita();

        } catch (Exception e) {
            mensaje("Error: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    private void mensaje(String msg, FacesMessage.Severity sev) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, msg, null));
    }

    // GETTERS
    public ScheduleModel getEventModel() { return eventModel; }
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
}
