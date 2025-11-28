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

    // Listar todos (para ArchivoBean)
    public List<Paciente> listarTodos() {
        return findAll();
    }

    // Buscar por ID (para ArchivoBean)
    public Paciente buscarPorId(Long id) {
        return findById(id);
    }

    public List<Paciente> listar() {
        return findAll();
    }

    // Búsqueda por nombre o DPI
    public List<Paciente> buscar(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();

        return executeInTx(em -> em.createQuery(
                                "SELECT p FROM Paciente p WHERE " +
                                        "LOWER(p.nombreCompleto) LIKE :f " +
                                        "OR p.dpi LIKE :f",
                                Paciente.class
                        )
                        .setParameter("f", "%" + f + "%")
                        .getResultList()
        );
    }

    // Validación DPI duplicado
    public boolean dpiExiste(String dpi, Long excluirId) {

        if (dpi == null || dpi.isBlank()) return false;

        return executeInTx(em -> em.createQuery(
                                "SELECT COUNT(p) FROM Paciente p WHERE p.dpi = :dpi AND p.id <> :id",
                                Long.class
                        )
                        .setParameter("dpi", dpi)
                        .setParameter("id", excluirId == null ? -1L : excluirId)
                        .getSingleResult() > 0
        );
    }

    @Override
    public Paciente guardar(Paciente p) {

        if (dpiExiste(p.getDpi(), p.getId())) {
            throw new RuntimeException("El DPI ya está registrado.");
        }

        return super.guardar(p);
    }
}
