package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "citas")
public class Cita implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===========================================
    // RELACIONES (EAGER PARA EVITAR ERRORES LAZY)
    // ===========================================

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "odontologo_id", nullable = false)
    private Usuario odontologo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    // AHORA "estado" es un VARCHAR normal (no entidad)
    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    // ===========================================
    // FECHAS
    // ===========================================
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(length = 500)
    private String notas;

    // ===========================================
    // COSTOS
    // ===========================================
    @Column(name = "precio_base", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioBase = new BigDecimal("300.00");

    @Column(name = "precio_tratamiento", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioTratamiento = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total = new BigDecimal("300.00");

    // ===========================================
    // HISTORIAL
    // ===========================================
    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialReprogramacion> historial;

    // ===========================================
    // PRE-PERSIST
    // ===========================================
    @PrePersist
    public void prePersist() {
        if (estado == null) estado = "CONFIRMADA";
        if (precioBase == null) precioBase = new BigDecimal("300.00");
        if (precioTratamiento == null) precioTratamiento = BigDecimal.ZERO;
        if (total == null) total = precioBase;
    }

    // ===========================================
    // GETTERS / SETTERS
    // ===========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getOdontologo() { return odontologo; }
    public void setOdontologo(Usuario odontologo) { this.odontologo = odontologo; }

    public Tratamiento getTratamiento() { return tratamiento; }
    public void setTratamiento(Tratamiento tratamiento) { this.tratamiento = tratamiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public BigDecimal getPrecioTratamiento() { return precioTratamiento; }
    public void setPrecioTratamiento(BigDecimal precioTratamiento) { this.precioTratamiento = precioTratamiento; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<HistorialReprogramacion> getHistorial() { return historial; }
    public void setHistorial(List<HistorialReprogramacion> historial) { this.historial = historial; }
}
