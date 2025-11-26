package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.HistorialExpediente;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.servicio.HistorialExpedienteServicio;
import com.uspgdevteam.sonrisasana.servicio.PacienteServicio;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named
@SessionScoped
public class PacienteBean implements Serializable {

    @Inject
    private PacienteServicio pacienteServicio;

    @Inject
    private HistorialExpedienteServicio historialServicio;

    private List<Paciente> pacientes;
    private Paciente paciente;
    private String filtroBusqueda;

    @PostConstruct
    public void init() {
        try {
            pacientes = pacienteServicio.listar();

            if (pacientes == null) {
                pacientes = Collections.emptyList();
            }

        } catch (Exception e) {
            e.printStackTrace();
            pacientes = Collections.emptyList();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudieron cargar los pacientes."));
        }
    }

    public void nuevo() {
        paciente = new Paciente();
    }

    public void editar(Paciente p) {
        this.paciente = p;
    }

    public void guardar() {
        try {

            boolean esNuevo = (paciente.getId() == null);

            // Validación DPI
            if (pacienteServicio.dpiExiste(paciente.getDpi(), paciente.getId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "El DPI ya está registrado."));
                return;
            }

            // Guardar paciente
            pacienteServicio.guardar(paciente);

            // Registrar historial
            if (esNuevo) {
                historialServicio.guardar(new HistorialExpediente(
                        paciente,
                        "Se creó el expediente"
                ));
            } else {
                historialServicio.guardar(new HistorialExpediente(
                        paciente,
                        "Se actualizó información del paciente"
                ));
            }

            // Recargar lista
            pacientes = pacienteServicio.listar();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Paciente guardado correctamente"));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo guardar el paciente."));
        }
    }

    public void eliminar(Paciente p) {
        try {
            pacienteServicio.eliminar(p);
            pacientes = pacienteServicio.listar();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Paciente eliminado"));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo eliminar el paciente."));
        }
    }

    public void buscar() {
        try {
            if (filtroBusqueda == null || filtroBusqueda.isBlank()) {
                pacientes = pacienteServicio.listar();
            } else {
                pacientes = pacienteServicio.buscar(filtroBusqueda.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            pacientes = Collections.emptyList();
        }
    }

    public void cancelar() {
        paciente = null;
    }

    // ================================
    //      HISTORIAL
    // ================================
    public List<HistorialExpediente> obtenerHistorial(Paciente p) {
        try {
            return historialServicio.obtenerHistorial(p);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    // GETTERS / SETTERS
    public List<Paciente> getPacientes() { return pacientes; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getFiltroBusqueda() { return filtroBusqueda; }
    public void setFiltroBusqueda(String filtroBusqueda) { this.filtroBusqueda = filtroBusqueda; }
}
