package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Rol;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.RolServicio;
import com.uspgdevteam.sonrisasana.servicio.UsuarioServicio;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Named
@ViewScoped
public class UsuarioBean implements Serializable {

    @Inject
    private UsuarioServicio usuarioServicio;

    @Inject
    private RolServicio rolServicio;

    private List<Usuario> usuarios;
    private List<Rol> roles;

    private Usuario seleccionado;
    private Long usuarioIdParaPassword;

    private String passwordActual;
    private String passwordNueva;
    private String passwordConfirmacion;

    /** Lista fija de días */
    private final List<String> diasSemana = Arrays.asList(
            "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    );

    @PostConstruct
    public void init() {
        usuarios = usuarioServicio.listar();
        roles = rolServicio.listar();
    }

    // ===============================
    // NUEVO USUARIO
    // ===============================
    public void nuevo() {
        seleccionado = new Usuario();
        seleccionado.setActivo(true);
    }

    // ===============================
    // EDITAR USUARIO
    // ===============================
    public void editar(Usuario u) {
        seleccionado = usuarioServicio.findById(u.getId());

        // Si es odontólogo y no tiene horarios, setear valores por defecto
        if (seleccionado.esOdontologo()) {

            if (seleccionado.getDiaInicio() == null) seleccionado.setDiaInicio("LUNES");
            if (seleccionado.getDiaFin() == null) seleccionado.setDiaFin("VIERNES");

            if (seleccionado.getHoraInicio() == null) seleccionado.setHoraInicio(LocalTime.of(9, 0));
            if (seleccionado.getHoraFin() == null) seleccionado.setHoraFin(LocalTime.of(13, 0));
        }
    }

    // ===============================
    // GUARDAR
    // ===============================
    public void guardar() {

        // VALIDACIÓN DE CORREO EXISTENTE
        if (usuarioServicio.emailExiste(seleccionado.getEmail(), seleccionado.getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "El correo ya está registrado en otro usuario", null));
            return;
        }

        // Validaciones para odontólogos
        if (seleccionado.esOdontologo()) {

            if (seleccionado.getDiaInicio() == null || seleccionado.getDiaFin() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Debe seleccionar el rango de días de trabajo", null));
                return;
            }

            if (seleccionado.getHoraInicio() == null || seleccionado.getHoraFin() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Debe establecer el horario laboral completo", null));
                return;
            }

            if (seleccionado.getHoraFin().isBefore(seleccionado.getHoraInicio())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "La hora de fin no puede ser antes de la hora de inicio", null));
                return;
            }

        } else {
            // Si NO es odontólogo → limpiar horarios
            seleccionado.setDiaInicio(null);
            seleccionado.setDiaFin(null);
            seleccionado.setHoraInicio(null);
            seleccionado.setHoraFin(null);
        }

        usuarioServicio.save(seleccionado);

        usuarios = usuarioServicio.listar();
        seleccionado = null;

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Usuario guardado correctamente", null));
    }

    // ===============================
    // ELIMINAR
    // ===============================
    public void eliminar(Usuario u) {
        try {
            usuarioServicio.delete(u.getId());
            usuarios = usuarioServicio.listar();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Usuario eliminado correctamente", null));

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se pudo eliminar el usuario", null));
        }
    }

    // ===============================
    // PASSWORD
    // ===============================
    public void prepararCambioPassword(Usuario u) {
        usuarioIdParaPassword = u.getId();
        passwordActual = "";
        passwordNueva = "";
        passwordConfirmacion = "";
    }

    public void cambiarPassword() {

        Usuario u = usuarioServicio.findById(usuarioIdParaPassword);

        if (!u.getPasswordHash().equals(passwordActual)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "La contraseña actual es incorrecta", null));
            return;
        }

        if (!passwordNueva.equals(passwordConfirmacion)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "La confirmación no coincide", null));
            return;
        }

        u.setPasswordHash(passwordNueva);
        usuarioServicio.save(u);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Contraseña actualizada correctamente", null));
    }

    // ===============================
    // GETTERS / SETTERS
    // ===============================

    public List<String> getDiasSemana() { return diasSemana; }

    public List<Usuario> getUsuarios() { return usuarios; }

    public Usuario getSeleccionado() { return seleccionado; }
    public void setSeleccionado(Usuario seleccionado) { this.seleccionado = seleccionado; }

    public List<Rol> getRoles() { return roles; }

    public String getPasswordActual() { return passwordActual; }
    public void setPasswordActual(String passwordActual) { this.passwordActual = passwordActual; }

    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }

    public String getPasswordConfirmacion() { return passwordConfirmacion; }
    public void setPasswordConfirmacion(String passwordConfirmacion) { this.passwordConfirmacion = passwordConfirmacion; }
}
