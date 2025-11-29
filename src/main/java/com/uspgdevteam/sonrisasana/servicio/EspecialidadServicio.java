package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Especialidad;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.List;

@Named
@ApplicationScoped
public class EspecialidadServicio extends GenericService<Especialidad> {

    public EspecialidadServicio() {
        super(Especialidad.class);
    }

    public List<Especialidad> listar() {
        return findAll();
    }
}
