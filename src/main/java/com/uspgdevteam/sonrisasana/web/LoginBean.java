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

    @Inject
    private UsuarioServicio usuarioServicio;

    private Usuario usuarioLogueado;

    public void login() {

        Usuario u = usuarioServicio.login(username, password);

        if (u == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Credenciales inválidas",
                            "Usuario o contraseña incorrectos"));
            return;
        }

        this.usuarioLogueado = u;
        this.username = null;
        this.password = null;

        try {
            if (u.esAdministrador()) {
                redirect("dashboard.xhtml");
            } else if (u.esOdontologo()) {
                redirect("citas.xhtml");
            } else {
                redirect("citas.xhtml");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error al redirigir después del login", e);
        }
    }

    public void logout() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            redirect("login.xhtml");
        } catch (IOException e) {
            throw new RuntimeException("Error en logout", e);
        }
    }

    private void redirect(String url) throws IOException {
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .redirect(url);
    }

    public boolean isAdministrador() {
        return usuarioLogueado != null && usuarioLogueado.esAdministrador();
    }

    public boolean isOdontologo() {
        return usuarioLogueado != null && usuarioLogueado.esOdontologo();
    }

    public boolean isRecepcionista() {
        return usuarioLogueado != null && usuarioLogueado.esRecepcionista();
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public String getNombreUsuario() {
        return usuarioLogueado != null ? usuarioLogueado.getNombreCompleto() : "";
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
