package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "estado_factura")
public class EstadoFactura implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre; // PENDIENTE, PARCIALMENTE_PAGADO, PAGADO, ANULADO

    @Column(length = 200)
    private String descripcion;

    public EstadoFactura() {}

    public EstadoFactura(String nombre) {
        this.nombre = nombre;
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EstadoFactura)) return false;
        EstadoFactura est = (EstadoFactura) o;
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

