package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    // ===============================
    // HORARIO LABORAL (solo odontólogos)
    // ===============================

    @Column(name = "dia_inicio", length = 20)
    private String diaInicio;

    @Column(name = "dia_fin", length = 20)
    private String diaFin;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    // ===============================
    // RELACIÓN CON CITAS
    // ===============================

    @OneToMany(mappedBy = "odontologo")
    private List<Cita> citas;

    public Usuario() {}

    // ======= GETTERS / SETTERS =======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getDiaInicio() { return diaInicio; }
    public void setDiaInicio(String diaInicio) { this.diaInicio = diaInicio; }

    public String getDiaFin() { return diaFin; }
    public void setDiaFin(String diaFin) { this.diaFin = diaFin; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public List<Cita> getCitas() { return citas; }
    public void setCitas(List<Cita> citas) { this.citas = citas; }

    // ======= MÉTODOS DE ROL =======
    public boolean esAdministrador() {
        return rol != null && "ADMINISTRADOR".equalsIgnoreCase(rol.getNombre());
    }

    public boolean esOdontologo() {
        return rol != null && "ODONTOLOGO".equalsIgnoreCase(rol.getNombre());
    }

    public boolean esRecepcionista() {
        return rol != null && "RECEPCIONISTA".equalsIgnoreCase(rol.getNombre());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
