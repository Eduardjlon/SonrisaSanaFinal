package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.EspecialidadOdontologica;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class EspecialidadBean {

    public EspecialidadOdontologica[] getLista() {
        return EspecialidadOdontologica.values();
    }
}
