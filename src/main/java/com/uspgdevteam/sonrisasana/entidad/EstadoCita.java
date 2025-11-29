package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "estado_cita")
public class EstadoCita implements Serializable {

    @Id
    @Column(name = "nombre", length = 30)
    private String nombre;

    public EstadoCita() {}

    public EstadoCita(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
