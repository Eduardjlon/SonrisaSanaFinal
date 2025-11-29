package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tipos_archivo")
public class TipoArchivoClinico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // IMAGEN, PDF, RADIOGRAFIA, DOCUMENTO

    @Column(length = 200)
    private String descripcion;

    public TipoArchivoClinico() {}

    public TipoArchivoClinico(String nombre) {
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
        if (!(o instanceof TipoArchivoClinico)) return false;
        TipoArchivoClinico tipo = (TipoArchivoClinico) o;
        return id != null && id.equals(tipo.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

