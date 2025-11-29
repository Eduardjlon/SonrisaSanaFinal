package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Especialidad;
import com.uspgdevteam.sonrisasana.entidad.Rol;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.EspecialidadServicio;
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

    @Inject
    private EspecialidadServicio especialidadServicio;

    private List<Usuario> usuarios;
    private List<Rol> roles;
    private List<Especialidad> especialidades;

    private Usuario seleccionado;
    private Long usuarioIdParaPassword;

    private String passwordActual;
    private String passwordNueva;
    private String passwordConfirmacion;

    /** Lista fija de días */
    private final List<String> diasSemana = Arrays.asList(
            "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    );

    // ===========================================================
    // INICIO
    // ===========================================================

    @PostConstruct
    public void init() {
        usuarios = usuarioServicio.listar();
        roles = rolServicio.listar();
        especialidades = especialidadServicio.listar();
    }

    // ===========================================================
    // NUEVO USUARIO
    // ===========================================================

    public void nuevo() {
        seleccionado = new Usuario();
        seleccionado.setActivo(true);
    }

    // ===========================================================
    // EDITAR
    // ===========================================================

    public void editar(Usuario u) {
        seleccionado = usuarioServicio.findById(u.getId());

        if (seleccionado.esOdontologo()) {

            if (seleccionado.getDiaInicio() == null) seleccionado.setDiaInicio("LUNES");
            if (seleccionado.getDiaFin() == null) seleccionado.setDiaFin("VIERNES");

            if (seleccionado.getHoraInicio() == null) seleccionado.setHoraInicio(LocalTime.of(9, 0));
            if (seleccionado.getHoraFin() == null) seleccionado.setHoraFin(LocalTime.of(13, 0));
        }
    }

    // ===========================================================
    // GUARDAR USUARIO
    // ===========================================================

    public void guardar() {

        // VALIDAR ROL
        if (seleccionado.getRol() == null) {
            mensajeError("Debe seleccionar un rol para el usuario");
            return;
        }

        // VALIDACIÓN EMAIL ÚNICO
        if (usuarioServicio.emailExiste(seleccionado.getEmail(), seleccionado.getId())) {
            mensajeError("El correo ya está registrado en otro usuario");
            return;
        }

        // VALIDACIÓN USERNAME ÚNICO
        if (usuarioServicio.usernameExiste(seleccionado.getUsername(), seleccionado.getId())) {
            mensajeError("El nombre de usuario ya está registrado");
            return;
        }

        // VALIDACIONES PARA ODONTÓLOGOS
        if (seleccionado.esOdontologo()) {

            if (seleccionado.getDiaInicio() == null || seleccionado.getDiaFin() == null) {
                mensajeError("Debe definir el rango de días laborales");
                return;
            }

            if (seleccionado.getHoraInicio() == null || seleccionado.getHoraFin() == null) {
                mensajeError("Debe definir el horario laboral completo");
                return;
            }

            if (seleccionado.getHoraFin().isBefore(seleccionado.getHoraInicio())) {
                mensajeError("La hora de fin no puede ser antes de la de inicio");
                return;
            }

            // VALIDAR ESPECIALIDAD
            if (seleccionado.getEspecialidad() == null) {
                mensajeError("Debe seleccionar la especialidad del odontólogo");
                return;
            }

        } else {
            // NO odontólogo → limpiar horarios y especialidad
            seleccionado.setDiaInicio(null);
            seleccionado.setDiaFin(null);
            seleccionado.setHoraInicio(null);
            seleccionado.setHoraFin(null);
            seleccionado.setEspecialidad(null);
        }

        try {
            // 🔹 CONTRASEÑA EN TEXTO PLANO (solo pruebas)
            if (seleccionado.getId() == null && (seleccionado.getPasswordHash() == null || seleccionado.getPasswordHash().isEmpty())) {
                seleccionado.setPasswordHash("123456"); // contraseña inicial por defecto
            }

            usuarioServicio.save(seleccionado);

            usuarios = usuarioServicio.listar();
            seleccionado = null;

            mensajeInfo("Usuario guardado correctamente");

        } catch (Exception e) {
            mensajeError("Error al guardar el usuario");
        }
    }

    // ===========================================================
    // ELIMINAR
    // ===========================================================

    public void eliminar(Usuario u) {
        try {
            usuarioServicio.delete(u.getId());
            usuarios = usuarioServicio.listar();

            mensajeInfo("Usuario eliminado correctamente");

        } catch (Exception e) {
            mensajeError("No se pudo eliminar el usuario. ¿Tiene citas registradas?");
        }
    }

    // ===========================================================
    // CAMBIO DE CONTRASEÑA
    // ===========================================================

    public void prepararCambioPassword(Usuario u) {
        usuarioIdParaPassword = u.getId();
        passwordActual = "";
        passwordNueva = "";
        passwordConfirmacion = "";
    }

    public void cambiarPassword() {

        Usuario u = usuarioServicio.findById(usuarioIdParaPassword);

        if (!u.getPasswordHash().equals(passwordActual)) {
            mensajeError("La contraseña actual es incorrecta");
            return;
        }

        if (!passwordNueva.equals(passwordConfirmacion)) {
            mensajeError("La confirmación no coincide");
            return;
        }

        u.setPasswordHash(passwordNueva); // texto plano
        usuarioServicio.save(u);

        mensajeInfo("Contraseña actualizada correctamente");
    }

    // ===========================================================
    // UTILIDADES MENSAJES
    // ===========================================================

    private void mensajeInfo(String msj) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msj, null));
    }

    private void mensajeError(String msj) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msj, null));
    }

    // ===========================================================
    // GETTERS / SETTERS
    // ===========================================================

    public List<String> getDiasSemana() { return diasSemana; }

    public List<Usuario> getUsuarios() { return usuarios; }

    public Usuario getSeleccionado() { return seleccionado; }
    public void setSeleccionado(Usuario seleccionado) { this.seleccionado = seleccionado; }

    public List<Rol> getRoles() { return roles; }

    public List<Especialidad> getEspecialidades() { return especialidades; }

    public String getPasswordActual() { return passwordActual; }
    public void setPasswordActual(String passwordActual) { this.passwordActual = passwordActual; }

    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }

    public String getPasswordConfirmacion() { return passwordConfirmacion; }
    public void setPasswordConfirmacion(String passwordConfirmacion) { this.passwordConfirmacion = passwordConfirmacion; }
}
