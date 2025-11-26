package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Paciente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class PacienteServicio extends GenericService<Paciente> {

    public PacienteServicio() {
        super(Paciente.class);
    }

    public List<Paciente> listar() {
        return findAll();
    }

    public List<Paciente> buscar(String filtro) {
        return executeInTx(em -> em.createQuery(
                        "SELECT p FROM Paciente p " +
                                "WHERE LOWER(p.nombreCompleto) LIKE LOWER(:filtro) " +
                                "OR p.dpi LIKE :filtro",
                        Paciente.class)
                .setParameter("filtro", "%" + filtro + "%")
                .getResultList()
        );
    }

    public boolean dpiExiste(String dpi, Long excluirId) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT COUNT(p) FROM Paciente p WHERE p.dpi = :dpi AND p.id <> :id",
                                Long.class)
                        .setParameter("dpi", dpi)
                        .setParameter("id", excluirId == null ? -1L : excluirId)
                        .getSingleResult() > 0
        );
    }
}
