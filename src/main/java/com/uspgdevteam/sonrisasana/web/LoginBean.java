package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.servicio.UsuarioServicio;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private String username;
    private String password;
    private Usuario usuarioLogueado;

    @Inject
    private UsuarioServicio usuarioServicio;

    public void login() {
        try {
            Usuario u = usuarioServicio.login(username, password);

            if (u == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Credenciales inválidas", "Usuario o contraseña incorrectos"));
                return;
            }

            this.usuarioLogueado = u;
            this.username = null;
            this.password = null;

            // Redirigir según rol
            if (u.esAdministrador()) {
                redirect("dashboard.xhtml");
            } else if (u.esOdontologo()) {
                redirect("citas.xhtml"); // agenda del odontólogo
            } else {
                redirect("pacientes.xhtml");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            redirect("login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redirect(String url) throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect(url);
    }

    // GETTERS / SETTERS
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Usuario getUsuarioLogueado() { return usuarioLogueado; }

    public boolean isAdministrador() {
        return usuarioLogueado != null && usuarioLogueado.esAdministrador();
    }

    public boolean isOdontologo() {
        return usuarioLogueado != null && usuarioLogueado.esOdontologo();
    }

    public boolean isRecepcionista() {
        return usuarioLogueado != null && usuarioLogueado.esRecepcionista();
    }
}
