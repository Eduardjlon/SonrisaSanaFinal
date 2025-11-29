package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.CitaServicio;
import com.uspgdevteam.sonrisasana.servicio.PacienteServicio;
import com.uspgdevteam.sonrisasana.servicio.TratamientoServicio;
import com.uspgdevteam.sonrisasana.servicio.UsuarioServicio;
import com.uspgdevteam.sonrisasana.servicio.FacturaServicio;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Named
@SessionScoped
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
    private FacturaServicio facturaServicio;

    @Inject
    private LoginBean loginBean;

    private ScheduleModel eventModel;
    private Cita cita;
    private ScheduleEvent<?> evento;

    private List<Paciente> pacientes;
    private List<Usuario> odontologos;
    private List<Tratamiento> tratamientos;

    private Long pacienteId;
    private Long odontologoId;
    private Long tratamientoId;

    private List<String> estadosDisponibles = Arrays.asList("PENDIENTE", "CONFIRMADA", "CANCELADA");
    private String estadoNuevo;

    @PostConstruct
    public void init() {
        cargarListas();
        cargarEventos();
        nuevaCita();
    }

    public void cargarListas() {
        pacientes = pacienteServicio.listar();
        odontologos = usuarioServicio.listarOdontologos();
        tratamientos = tratamientoServicio.listar();
    }

    public void nuevaCita() {
        cita = new Cita();
        pacienteId = null;
        odontologoId = null;
        tratamientoId = null;
        estadoNuevo = null;
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

    public void onDateSelect(SelectEvent<LocalDateTime> event) {
        nuevaCita();
        cita.setFechaInicio(event.getObject());
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<?>> ev) {
        evento = ev.getObject();
        cita = (Cita) evento.getData();

        pacienteId = cita.getPaciente().getId();
        odontologoId = cita.getOdontologo().getId();
        tratamientoId = cita.getTratamiento().getId();

        estadoNuevo = cita.getEstado();
    }

    // Confirmar cita: cambia estado y genera factura automáticamente
    public void confirmarCita() {
        if (cita != null && cita.getId() != null) {
            citaServicio.actualizarEstado(cita.getId(), "CONFIRMADA");
            facturaServicio.crearFacturaDesdeCita(cita);
            mensaje("Cita confirmada y factura generada automáticamente", FacesMessage.SEVERITY_INFO);
            cargarEventos();
        }
    }

    public void cancelarCita() {
        if (cita != null && cita.getId() != null) {
            citaServicio.actualizarEstado(cita.getId(), "CANCELADA");
            mensaje("Cita cancelada", FacesMessage.SEVERITY_WARN);
            cargarEventos();
        }
    }

    public void guardarEstado() {
        if (cita != null && estadoNuevo != null && !estadoNuevo.isEmpty()) {
            citaServicio.actualizarEstado(cita.getId(), estadoNuevo);
            mensaje("Estado actualizado a " + estadoNuevo, FacesMessage.SEVERITY_INFO);
            cargarEventos();
            estadoNuevo = null;
        }
    }

    private void mensaje(String msg, FacesMessage.Severity sev) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, msg, null));
    }

    // GETTERS / SETTERS
    public ScheduleModel getEventModel() { return eventModel; }
    public Cita getCita() { return cita; }
    public List<Paciente> getPacientes() { return pacientes; }
    public List<Usuario> getOdontologos() { return odontologos; }
    public List<Tratamiento> getTratamientos() { return tratamientos; }
    public List<String> getEstadosDisponibles() { return estadosDisponibles; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public Long getOdontologoId() { return odontologoId; }
    public void setOdontologoId(Long odontologoId) { this.odontologoId = odontologoId; }
    public Long getTratamientoId() { return tratamientoId; }
    public void setTratamientoId(Long tratamientoId) { this.tratamientoId = tratamientoId; }

    public boolean isOdonto() { return loginBean != null && loginBean.isOdontologo(); }
    public boolean isRecepcionista() { return loginBean != null && loginBean.isRecepcionista(); }

    public String getFechaCompleta() {
        if (cita != null && cita.getFechaInicio() != null) {
            return cita.getFechaInicio().toString();
        }
        return "";
    }
}
