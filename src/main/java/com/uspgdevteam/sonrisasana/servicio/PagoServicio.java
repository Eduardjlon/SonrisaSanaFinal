package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Pago;
import com.uspgdevteam.sonrisasana.entidad.Factura;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class PagoServicio extends GenericService<Pago> {

    public PagoServicio() {
        super(Pago.class);
    }

    public List<Pago> listarPorFactura(Factura factura) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT p FROM Pago p WHERE p.factura = :factura ORDER BY p.fecha DESC",
                                Pago.class)
                        .setParameter("factura", factura)
                        .getResultList()
        );
    }

    public List<Pago> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT p FROM Pago p WHERE p.fecha BETWEEN :inicio AND :fin ORDER BY p.fecha DESC",
                                Pago.class)
                        .setParameter("inicio", inicio)
                        .setParameter("fin", fin)
                        .getResultList()
        );
    }
}

