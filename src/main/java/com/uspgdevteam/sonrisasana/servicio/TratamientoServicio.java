package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class TratamientoServicio extends GenericService<Tratamiento> {

    public TratamientoServicio() {
        super(Tratamiento.class);
    }

    public List<Tratamiento> listar() {
        return findAll();
    }
}
