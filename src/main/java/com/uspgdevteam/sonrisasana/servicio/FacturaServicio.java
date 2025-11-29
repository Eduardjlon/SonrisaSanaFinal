package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Factura;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class FacturaServicio extends GenericService<Factura> {

    public FacturaServicio() { super(Factura.class); }

    public Factura crearFacturaDesdeCita(Cita cita) {
        Factura factura = new Factura();
        factura.setCita(cita);
        factura.setPaciente(cita.getPaciente());
        factura.setSubtotal(cita.getTotal());
        factura.calcularTotal();

        return executeInTx(em -> {
            em.persist(factura);
            em.flush();
            return factura;
        });
    }

    public List<Factura> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT f FROM Factura f WHERE f.fechaEmision BETWEEN :inicio AND :fin ORDER BY f.fechaEmision DESC",
                                Factura.class)
                        .setParameter("inicio", inicio)
                        .setParameter("fin", fin)
                        .getResultList()
        );
    }
}
