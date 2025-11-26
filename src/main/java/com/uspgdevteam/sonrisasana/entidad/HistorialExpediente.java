package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_expediente")
public class HistorialExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 300)
    private String descripcion;

    public HistorialExpediente() {}

    public HistorialExpediente(Paciente p, String descripcion) {
        this.paciente = p;
        this.descripcion = descripcion;
        this.fecha = LocalDateTime.now();
    }

    // GETTERS & SETTERS

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
}
