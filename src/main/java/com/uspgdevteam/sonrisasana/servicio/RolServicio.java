package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Rol;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class RolServicio extends GenericService<Rol> {

    public RolServicio() {
        super(Rol.class);
    }

    public List<Rol> listar() {
        return findAll();
    }
}
