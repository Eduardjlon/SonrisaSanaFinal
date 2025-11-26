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
        Usuario u = usuarioServicio.buscarPorUsername(username);
        if (u == null || !passwordValida(password, u.getPasswordHash())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Credenciales inválidas",
                            "Usuario o contraseña incorrectos"));
            return;
        }
        this.usuarioLogueado = u;
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("dashboard.xhtml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void logout() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
    }

    private boolean passwordValida(String raw, String hash) {
        // Por simplicidad, texto plano. En producción usar BCrypt / Argon2.
        return raw != null && raw.equals(hash);
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
