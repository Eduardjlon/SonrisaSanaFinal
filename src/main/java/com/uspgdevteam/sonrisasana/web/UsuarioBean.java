package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Especialidad;
import com.uspgdevteam.sonrisasana.entidad.Rol;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.EspecialidadServicio;
import com.uspgdevteam.sonrisasana.servicio.RolServicio;
import com.uspgdevteam.sonrisasana.servicio.UsuarioServicio;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
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

    /** Lista de días */
    private final List<String> diasSemana = Arrays.asList(
            "LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","SABADO","DOMINGO"
    );

    @PostConstruct
    public void init() {
        usuarios = usuarioServicio.listar();
        roles = rolServicio.listar();
        especialidades = especialidadServicio.listar();
    }

    // ===================================================
    // NUEVO USUARIO
    // ===================================================
    public void nuevo() {
        seleccionado = new Usuario();
        seleccionado.setActivo(true);          // por defecto activo
    }

    // ===================================================
    // EDITAR USUARIO
    // ===================================================
    public void editar(Usuario u) {
        seleccionado = usuarioServicio.findById(u.getId());
        if (seleccionado == null) {
            mensajeError("No se encontró el usuario a editar");
            return;
        }

        if (seleccionado.esOdontologo()) {
            if (seleccionado.getDiaInicio() == null) seleccionado.setDiaInicio("LUNES");
            if (seleccionado.getDiaFin() == null)    seleccionado.setDiaFin("VIERNES");
            if (seleccionado.getHoraInicio() == null) seleccionado.setHoraInicio(LocalTime.of(9,0));
            if (seleccionado.getHoraFin() == null)    seleccionado.setHoraFin(LocalTime.of(13,0));
        }
    }

    // ===================================================
    // GUARDAR USUARIO
    // ===================================================
    public void guardar() {

        if (seleccionado == null) {
            mensajeError("No hay usuario seleccionado");
            return;
        }

        // Validar rol
        if (seleccionado.getRol() == null) {
            mensajeError("Debe seleccionar un rol para el usuario");
            return;
        }

        // Validar email único
        if (usuarioServicio.emailExiste(seleccionado.getEmail(), seleccionado.getId())) {
            mensajeError("El correo ya está registrado en otro usuario");
            return;
        }

        // Validar username único
        if (usuarioServicio.usernameExiste(seleccionado.getUsername(), seleccionado.getId())) {
            mensajeError("El nombre de usuario ya está registrado");
            return;
        }

        // Validaciones específicas para odontólogo
        if (seleccionado.esOdontologo()) {
            if (seleccionado.getDiaInicio() == null || seleccionado.getDiaFin() == null ||
                    seleccionado.getHoraInicio() == null || seleccionado.getHoraFin() == null) {
                mensajeError("Debe definir el horario laboral completo");
                return;
            }
            if (seleccionado.getHoraFin().isBefore(seleccionado.getHoraInicio())) {
                mensajeError("La hora fin no puede ser antes del inicio");
                return;
            }
            if (seleccionado.getEspecialidad() == null) {
                mensajeError("Debe seleccionar la especialidad");
                return;
            }
        } else {
            // Si no es odontólogo, limpiar campos de odontólogo
            seleccionado.setDiaInicio(null);
            seleccionado.setDiaFin(null);
            seleccionado.setHoraInicio(null);
            seleccionado.setHoraFin(null);
            seleccionado.setEspecialidad(null);
        }

        try {
            // Si es nuevo y no se ingresó password (por si en algún flujo viene vacío)
            if (seleccionado.getId() == null &&
                    (seleccionado.getPasswordHash() == null || seleccionado.getPasswordHash().isEmpty())) {
                seleccionado.setPasswordHash("123456");
            }

            usuarioServicio.save(seleccionado);

            // Recargar lista (solo usuarios activos)
            usuarios = usuarioServicio.listar();
            seleccionado = null;

            mensajeInfo("Usuario guardado correctamente");

        } catch (Exception e) {
            mensajeError("Error al guardar el usuario");
        }
    }

    // ===================================================
    // ELIMINAR USUARIO (BORRADO LÓGICO)
    // ===================================================
    public void eliminar(Usuario u) {
        try {
            if (u == null || u.getId() == null) {
                mensajeError("Usuario inválido");
                return;
            }

            usuarioServicio.desactivar(u);  // 🔹 nuevo método de servicio: setActivo(false)

            usuarios = usuarioServicio.listar(); // solo activos
            mensajeInfo("Usuario desactivado correctamente");

        } catch (Exception e) {
            mensajeError("No se pudo eliminar el usuario. ¿Tiene citas registradas?");
        }
    }

    // ===================================================
    // CAMBIAR CONTRASEÑA
    // ===================================================
    public void prepararCambioPassword(Usuario u) {
        if (u == null || u.getId() == null) {
            mensajeError("Usuario inválido para cambio de contraseña");
            return;
        }
        usuarioIdParaPassword = u.getId();
        passwordActual = "";
        passwordNueva = "";
        passwordConfirmacion = "";
    }

    public void cambiarPassword() {
        if (usuarioIdParaPassword == null) {
            mensajeError("No se ha seleccionado un usuario");
            return;
        }

        Usuario u = usuarioServicio.findById(usuarioIdParaPassword);
        if (u == null) {
            mensajeError("Usuario no encontrado");
            return;
        }

        if (!u.getPasswordHash().equals(passwordActual)) {
            mensajeError("La contraseña actual es incorrecta");
            return;
        }

        if (passwordNueva == null || passwordNueva.isBlank()) {
            mensajeError("La nueva contraseña no puede estar vacía");
            return;
        }

        if (!passwordNueva.equals(passwordConfirmacion)) {
            mensajeError("La confirmación no coincide");
            return;
        }

        u.setPasswordHash(passwordNueva);
        usuarioServicio.save(u);
        mensajeInfo("Contraseña actualizada correctamente");
    }

    // ===================================================
    // MENSAJES
    // ===================================================
    private void mensajeInfo(String msj) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msj, null));
    }

    private void mensajeError(String msj) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msj, null));
    }

    // ===================================================
    // GETTERS / SETTERS
    // ===================================================
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
