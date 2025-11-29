package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Seguro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class SeguroServicio extends GenericService<Seguro> {

    public SeguroServicio() {
        super(Seguro.class);
    }

    public List<Seguro> listar() {
        return executeInTx(em ->
                em.createQuery("SELECT s FROM Seguro s", Seguro.class)
                        .getResultList()
        );
    }
}
