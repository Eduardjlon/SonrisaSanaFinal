package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita implements Serializable {

    // ===========================================
    // ENUM DEL ESTADO DE LA CITA
    // ===========================================
    public enum EstadoCita {
        PENDIENTE,
        CONFIRMADA,
        CANCELADA,
        REPROGRAMADA,
        ATENDIDA
    }

    // ===========================================
    // CAMPOS PRINCIPALES
    // ===========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "odontologo_id", nullable = false)
    private Usuario odontologo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.CONFIRMADA;

    @Column(length = 500)
    private String notas;

    // ===========================================
    // CAMPOS ECONÓMICOS
    // ===========================================
    @Column(name = "precio_base", nullable = false)
    private BigDecimal precioBase = new BigDecimal("300.00");

    @Column(name = "precio_tratamiento", nullable = false)
    private BigDecimal precioTratamiento = BigDecimal.ZERO;

    @Column(name = "total", nullable = false)
    private BigDecimal total = new BigDecimal("300.00");

    // ===========================================
    // GETTERS Y SETTERS
    // ===========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }  // ← recomendado para JPA

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getOdontologo() { return odontologo; }
    public void setOdontologo(Usuario odontologo) { this.odontologo = odontologo; }

    public Tratamiento getTratamiento() { return tratamiento; }
    public void setTratamiento(Tratamiento tratamiento) { this.tratamiento = tratamiento; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public BigDecimal getPrecioTratamiento() { return precioTratamiento; }
    public void setPrecioTratamiento(BigDecimal precioTratamiento) { this.precioTratamiento = precioTratamiento; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
