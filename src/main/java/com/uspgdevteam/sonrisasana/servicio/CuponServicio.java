package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cupon;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class CuponServicio extends GenericService<Cupon> {

    public CuponServicio() {
        super(Cupon.class);
    }

    public List<Cupon> listar() {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cupon c", Cupon.class)
                        .getResultList()
        );
    }
}
