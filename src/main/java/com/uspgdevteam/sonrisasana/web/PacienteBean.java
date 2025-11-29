package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.HistorialExpediente;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.servicio.HistorialExpedienteServicio;
import com.uspgdevteam.sonrisasana.servicio.PacienteServicio;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Named
@ViewScoped
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
        cargarPacientes();
    }

    // =====================================================
    // CARGAR LISTA
    // =====================================================
    private void cargarPacientes() {
        try {
            pacientes = pacienteServicio.listarOrdenados();
        } catch (Exception e) {
            pacientes = Collections.emptyList();
            mostrarError("Error", "No se pudieron cargar los pacientes.");
        }
    }

    // =====================================================
    // NUEVO
    // =====================================================
    public void nuevo() {
        paciente = new Paciente();
    }

    // =====================================================
    // EDITAR
    // =====================================================
    public void editar(Paciente p) {
        paciente = pacienteServicio.buscarPorId(p.getId());
    }

    // =====================================================
    // GUARDAR
    // =====================================================
    public void guardar() {

        boolean esNuevo = (paciente.getId() == null);

        // Validación fecha
        if (paciente.getFechaNacimiento() == null) {
            mostrarError("Fecha inválida", "Debe ingresar una fecha de nacimiento.");
            return;
        }

        if (paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
            mostrarError("Fecha inválida", "La fecha de nacimiento no puede ser futura.");
            return;
        }

        try {
            pacienteServicio.save(paciente);

        } catch (RuntimeException ex) {
            mostrarError("Error al guardar", ex.getMessage());
            return;
        }

        // Registrar historial
        try {
            String descripcion = esNuevo
                    ? "Se creó el expediente"
                    : "Se actualizó información del paciente";

            historialServicio.guardar(new HistorialExpediente(paciente, descripcion));

        } catch (Exception e) {
            mostrarError("Advertencia", "Paciente guardado, pero no se pudo registrar historial.");
        }

        cargarPacientes();
        paciente = null;

        mostrarOk("Paciente guardado correctamente");
    }

    // =====================================================
    // ELIMINAR
    // =====================================================
    public void eliminar(Paciente p) {
        try {
            pacienteServicio.delete(p.getId());

            filtroBusqueda = null; // 🔥 Importante: evitar búsquedas inválidas
            cargarPacientes();

            mostrarOk("Paciente eliminado");

        } catch (Exception e) {
            mostrarError("Error", "No se pudo eliminar el paciente.");
        }
    }

    // =====================================================
    // BUSCAR
    // =====================================================
    public void buscar() {
        try {
            if (filtroBusqueda == null || filtroBusqueda.isBlank()) {
                cargarPacientes();
            } else {
                pacientes = pacienteServicio.buscar(filtroBusqueda.trim());
            }
        } catch (Exception e) {
            pacientes = Collections.emptyList();
        }
    }

    // =====================================================
    // HISTORIAL
    // =====================================================
    public List<HistorialExpediente> obtenerHistorial(Paciente p) {
        try {
            return historialServicio.obtenerHistorial(p);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // =====================================================
    // UTILIDADES
    // =====================================================
    private void mostrarError(String titulo, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, titulo, detalle));
    }

    private void mostrarOk(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================
    public List<Paciente> getPacientes() { return pacientes; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public String getFiltroBusqueda() { return filtroBusqueda; }
    public void setFiltroBusqueda(String filtroBusqueda) { this.filtroBusqueda = filtroBusqueda; }
}
