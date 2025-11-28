package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.ArchivoClinico;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import com.uspgdevteam.sonrisasana.servicio.ArchivoClinicoServicio;
import com.uspgdevteam.sonrisasana.servicio.CitaServicio;
import com.uspgdevteam.sonrisasana.servicio.PacienteServicio;
import com.uspgdevteam.sonrisasana.servicio.FileStorageService;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ArchivoBean implements Serializable {

    // ============================
    // PROPIEDADES
    // ============================
    private String filtro;                    // para búsqueda
    private List<Paciente> pacientes;
    private List<Paciente> pacientesFiltrados;

    private Paciente pacienteSeleccionado;
    private List<ArchivoClinico> archivosPaciente;

    private UploadedFile archivo;             // archivo a subir
    private String tipoArchivo;               // pdf, imagen, etc.

    // PREVIEW
    private String previewTipo;               // "imagen" o "pdf"
    private String previewContenidoBase64;    // contenido en base64

    // ============================
    // SERVICES
    // ============================
    @Inject
    private PacienteServicio pacienteServicio;

    @Inject
    private ArchivoClinicoServicio archivoServicio;

    @Inject
    private FileStorageService fileStorage;

    @Inject
    private CitaServicio citaServicio;

    @PostConstruct
    public void init() {
        pacientes = pacienteServicio.listar();
    }

    public List<Paciente> getPacientesFiltrados() {
        if (filtro == null || filtro.isBlank()) {
            return pacientes;
        }

        String f = filtro.toLowerCase();

        return pacientes.stream()
                .filter(p -> p.getNombreCompleto().toLowerCase().contains(f)
                        || p.getDpi().contains(f))
                .collect(Collectors.toList());
    }


    public void seleccionarPaciente(Paciente p) {
        this.pacienteSeleccionado = p;
        this.archivosPaciente = archivoServicio.listarPorPaciente(p.getId());
    }


    public void subirArchivo() {
        try {

            if (archivo == null) {
                mostrar("Debe seleccionar un archivo.");
                return;
            }

            if (pacienteSeleccionado == null) {
                mostrar("Debe seleccionar un paciente.");
                return;
            }

            String ruta = fileStorage.guardarArchivo(
                    archivo.getInputStream(),
                    archivo.getFileName()
            );

            ArchivoClinico ac = new ArchivoClinico();
            ac.setPaciente(pacienteSeleccionado);
            ac.setTipo(tipoArchivo == null ? "Archivo" : tipoArchivo);
            ac.setNombreArchivo(archivo.getFileName());
            ac.setRutaFisica(ruta);
            ac.setUrlPublica(null);

            archivoServicio.guardar(ac);

            archivosPaciente = archivoServicio.listarPorPaciente(pacienteSeleccionado.getId());
            mostrar("Archivo subido correctamente.");

        } catch (IOException ex) {
            mostrar("Error al guardar archivo: " + ex.getMessage());
        }
    }


    public void descargar(ArchivoClinico a) {
        fileStorage.descargarArchivo(a.getRutaFisica());
    }


    public void eliminar(ArchivoClinico a) {
        archivoServicio.eliminar(a);
        archivosPaciente = archivoServicio.listarPorPaciente(pacienteSeleccionado.getId());
        mostrar("Archivo eliminado.");
    }


    public boolean esVisualizable(ArchivoClinico a) {
        String n = a.getNombreArchivo().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".gif")
                || n.endsWith(".pdf");
    }

    public String icono(ArchivoClinico a) {
        String n = a.getNombreArchivo().toLowerCase();

        if (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")) return "pi-image";
        if (n.endsWith(".pdf")) return "pi-file-pdf";
        if (n.endsWith(".doc") || n.endsWith(".docx")) return "pi-file-word";
        if (n.endsWith(".xls") || n.endsWith(".xlsx")) return "pi-file-excel";

        return "pi-file";
    }

    public void previsualizar(ArchivoClinico a) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(a.getRutaFisica()));
            String nombre = a.getNombreArchivo().toLowerCase();

            if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") || nombre.endsWith(".png")) {
                previewTipo = "imagen";
                previewContenidoBase64 =
                        "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
            }
            else if (nombre.endsWith(".pdf")) {
                previewTipo = "pdf";
                previewContenidoBase64 =
                        "data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes);
            }
            else {
                previewTipo = null;
                previewContenidoBase64 = null;
                mostrar("El archivo no puede ser previsualizado.");
            }

        } catch (Exception e) {
            mostrar("Error al previsualizar: " + e.getMessage());
        }
    }


    private void mostrar(String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }


    public List<Paciente> getPacientes() { return pacientes; }
    public Paciente getPacienteSeleccionado() { return pacienteSeleccionado; }
    public List<ArchivoClinico> getArchivosPaciente() { return archivosPaciente; }

    public UploadedFile getArchivo() { return archivo; }
    public void setArchivo(UploadedFile archivo) { this.archivo = archivo; }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }

    public String getPreviewTipo() { return previewTipo; }
    public String getPreviewContenidoBase64() { return previewContenidoBase64; }

    public void setPacientesFiltrados(List<Paciente> pacientesFiltrados) {
        this.pacientesFiltrados = pacientesFiltrados;
    }
}
