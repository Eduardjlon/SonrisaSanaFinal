package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.HistorialReprogramacion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class HistorialReprogramacionServicio extends GenericService<HistorialReprogramacion> {

    public HistorialReprogramacionServicio() {
        super(HistorialReprogramacion.class);
    }

    public List<HistorialReprogramacion> obtenerHistorial(Cita cita) {
        return executeInTx(em -> em.createQuery(
                        "SELECT h FROM HistorialReprogramacion h WHERE h.cita = :c ORDER BY h.fecha DESC",
                        HistorialReprogramacion.class)
                .setParameter("c", cita)
                .getResultList()
        );
    }
}
