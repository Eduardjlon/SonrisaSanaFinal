package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "estado_cita")
public class EstadoCita implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo que funciona como clave lógica (FK en citas)
    @Column(name = "nombre", length = 30, nullable = false, unique = true)
    private String nombre;

    // ===========================================
    // CONSTRUCTORES
    // ===========================================
    public EstadoCita() {}

    public EstadoCita(String nombre) {
        this.nombre = nombre;
    }

    // ===========================================
    // GETTERS / SETTERS
    // ===========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // ===========================================
    // EQUALS & HASHCODE (por nombre)
    // ===========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EstadoCita)) return false;
        EstadoCita est = (EstadoCita) o;
        return nombre != null && nombre.equalsIgnoreCase(est.nombre);
    }

    @Override
    public int hashCode() {
        return nombre == null ? 0 : nombre.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
