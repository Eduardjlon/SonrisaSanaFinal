package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Seguro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class SeguroServicio extends GenericService<Seguro> {

    public SeguroServicio() {
        super(Seguro.class);
    }
}

