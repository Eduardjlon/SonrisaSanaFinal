package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cupon;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class CuponServicio extends GenericService<Cupon> {

    public CuponServicio() {
        super(Cupon.class);
    }

    public Cupon buscarPorCodigo(String codigo) {
        return executeInTx(em ->
                em.createQuery("SELECT c FROM Cupon c WHERE c.codigo = :codigo AND c.activo = true", Cupon.class)
                        .setParameter("codigo", codigo)
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }
}

