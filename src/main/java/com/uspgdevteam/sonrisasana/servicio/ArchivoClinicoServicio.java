package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.ArchivoClinico;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class ArchivoClinicoServicio extends GenericService<ArchivoClinico> {

    public ArchivoClinicoServicio() {
        super(ArchivoClinico.class);
    }

    public List<ArchivoClinico> listarPorPaciente(Long pacienteId) {
        return executeInTx(em -> em.createQuery(
                        "SELECT a FROM ArchivoClinico a WHERE a.paciente.id = :pid ORDER BY a.fechaCarga DESC",
                        ArchivoClinico.class)
                .setParameter("pid", pacienteId)
                .getResultList()
        );
    }
}
