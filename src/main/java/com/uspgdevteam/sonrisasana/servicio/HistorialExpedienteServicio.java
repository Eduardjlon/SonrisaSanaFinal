package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.HistorialExpediente;
import com.uspgdevteam.sonrisasana.entidad.Paciente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class HistorialExpedienteServicio extends GenericService<HistorialExpediente> {

    public HistorialExpedienteServicio() {
        super(HistorialExpediente.class);
    }

    /**
     * Método público para obtener el historial ordenado por fecha
     */
    public List<HistorialExpediente> obtenerHistorial(Paciente paciente) {
        return executeInTx(em -> em.createQuery(
                        "SELECT h FROM HistorialExpediente h WHERE h.paciente = :p ORDER BY h.fecha DESC",
                        HistorialExpediente.class)
                .setParameter("p", paciente)
                .getResultList()
        );
    }
}
