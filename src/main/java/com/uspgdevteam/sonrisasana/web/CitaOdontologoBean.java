package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.CitaServicio;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Named
@ViewScoped
public class CitaOdontologoBean implements Serializable {

    @Inject
    private LoginBean loginBean;

    @Inject
    private CitaServicio citaServicio;

    private ScheduleModel eventModel;
    private Cita cita;
    private String estadoNuevo;

    // Lista fija de estados posibles
    private final List<String> estadosDisponibles = Arrays.asList("CONFIRMADA", "CANCELADA", "REALIZADA");

    @PostConstruct
    public void init() {
        cargarAgenda();
    }

    private void cargarAgenda() {
        eventModel = new DefaultScheduleModel();

        Usuario odontologo = loginBean.getUsuarioLogueado();
        List<Cita> citas = citaServicio.listarPorOdontologo(odontologo);

        for (Cita c : citas) {
            DefaultScheduleEvent<?> ev = DefaultScheduleEvent.builder()
                    .title(c.getPaciente().getNombreCompleto() + " - " + c.getTratamiento().getNombre())
                    .startDate(c.getFechaInicio())
                    .endDate(c.getFechaFin())
                    .data(c)
                    .build();
            eventModel.addEvent(ev);
        }
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<?>> ev) {
        cita = (Cita) ev.getObject().getData();
        estadoNuevo = cita.getEstado();
    }

    public void guardarEstado() {
        if (cita != null && estadoNuevo != null) {
            cita.setEstado(estadoNuevo);
            citaServicio.actualizar(cita);
        }
    }

    public String getFechaCompleta() {
        return cita != null ? cita.getFechaInicio().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "";
    }

    // =========================
    // GETTERS / SETTERS
    // =========================
    public ScheduleModel getEventModel() { return eventModel; }
    public Cita getCita() { return cita; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public List<String> getEstadosDisponibles() { return estadosDisponibles; }
}
