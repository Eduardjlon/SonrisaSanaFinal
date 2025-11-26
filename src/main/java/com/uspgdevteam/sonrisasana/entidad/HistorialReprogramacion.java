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

    @ManyToOne(optional = false)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 300)
    private String descripcion;

    public HistorialReprogramacion() {}

    public HistorialReprogramacion(Cita cita, String descripcion) {
        this.cita = cita;
        this.fecha = LocalDateTime.now();
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public Cita getCita() { return cita; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
}
