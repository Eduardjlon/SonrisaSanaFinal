package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "seguros")
public class Seguro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "cobertura", precision = 10, scale = 2, nullable = false)
    private BigDecimal cobertura;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public Seguro() {
    }

    // ===============================
    // GETTERS / SETTERS
    // ===============================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCobertura() { return cobertura; }
    public void setCobertura(BigDecimal cobertura) { this.cobertura = cobertura; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seguro)) return false;
        Seguro seguro = (Seguro) o;
        return id != null && id.equals(seguro.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
