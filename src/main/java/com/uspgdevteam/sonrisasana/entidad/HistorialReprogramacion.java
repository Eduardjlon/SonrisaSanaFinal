package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_reprogramacion")
public class HistorialReprogramacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con cita
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(name = "fecha_anterior_inicio")
    private LocalDateTime fechaAnteriorInicio;

    @Column(name = "fecha_anterior_fin")
    private LocalDateTime fechaAnteriorFin;

    @Column(name = "fecha_nueva_inicio")
    private LocalDateTime fechaNuevaInicio;

    @Column(name = "fecha_nueva_fin")
    private LocalDateTime fechaNuevaFin;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public Long getId() { return id; }

    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }

    public LocalDateTime getFechaAnteriorInicio() { return fechaAnteriorInicio; }
    public void setFechaAnteriorInicio(LocalDateTime fechaAnteriorInicio) { this.fechaAnteriorInicio = fechaAnteriorInicio; }

    public LocalDateTime getFechaAnteriorFin() { return fechaAnteriorFin; }
    public void setFechaAnteriorFin(LocalDateTime fechaAnteriorFin) { this.fechaAnteriorFin = fechaAnteriorFin; }

    public LocalDateTime getFechaNuevaInicio() { return fechaNuevaInicio; }
    public void setFechaNuevaInicio(LocalDateTime fechaNuevaInicio) { this.fechaNuevaInicio = fechaNuevaInicio; }

    public LocalDateTime getFechaNuevaFin() { return fechaNuevaFin; }
    public void setFechaNuevaFin(LocalDateTime fechaNuevaFin) { this.fechaNuevaFin = fechaNuevaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
