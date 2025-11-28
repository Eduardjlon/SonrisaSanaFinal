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
    private List<Tratamiento> tratamientos;   // 🔹 TODOS los tratamientos

    // ==========================================================
    // INICIALIZACIÓN
    // ==========================================================
    @PostConstruct
    public void init() {
        cargarListas();
        cargarEventos();
        nuevaCita();
    }

    private void cargarListas() {
        pacientes = pacienteServicio.listar();
        odontologos = usuarioServicio.listarOdontologos();
        tratamientos = tratamientoServicio.listar();   // 🔹 se cargan todos
    }

    // ==========================================================
    // NUEVA CITA
    // ==========================================================
    public void nuevaCita() {
        cita = new Cita();
    }

    // ==========================================================
    // CARGAR EVENTOS AL CALENDARIO
    // ==========================================================
    private void cargarEventos() {
        eventModel = new DefaultScheduleModel();

        List<Cita> lista = citaServicio.listarTodas();

        for (Cita c : lista) {
            String titulo = c.getPaciente().getNombreCompleto()
                    + " - " + (c.getTratamiento() != null ? c.getTratamiento().getNombre() : "");

            DefaultScheduleEvent<?> ev = DefaultScheduleEvent.builder()
                    .title(titulo)
                    .startDate(c.getFechaInicio())
                    .endDate(c.getFechaFin())
                    .data(c)
                    .build();

            eventModel.addEvent(ev);
        }
    }

    // ==========================================================
    // SELECCIONAR FECHA VACÍA
    // ==========================================================
    public void onDateSelect(SelectEvent<LocalDateTime> event) {
        nuevaCita();
        cita.setFechaInicio(event.getObject());
    }

    // ==========================================================
    // SELECCIONAR EVENTO EXISTENTE
    // ==========================================================
    public void onEventSelect(SelectEvent<ScheduleEvent<?>> ev) {
        evento = ev.getObject();
        cita = (Cita) evento.getData();
    }

    // ==========================================================
    // ACTUALIZAR PRECIOS
    // ==========================================================
    public void actualizarPrecios() {

        if (cita.getTratamiento() == null) {
            cita.setPrecioTratamiento(null);
            cita.setTotal(cita.getPrecioBase());
            return;
        }

        cita.setPrecioTratamiento(cita.getTratamiento().getCosto());
        cita.setTotal(cita.getPrecioBase().add(cita.getTratamiento().getCosto()));
    }

    // ==========================================================
    // GUARDAR
    // ==========================================================
    public void guardar() {
        try {

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
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, msg, null));
    }

    public ScheduleModel getEventModel() { return eventModel; }
    public Cita getCita() { return cita; }
    public List<Paciente> getPacientes() { return pacientes; }
    public List<Usuario> getOdontologos() { return odontologos; }
    public List<Tratamiento> getTratamientos() { return tratamientos; }
}
