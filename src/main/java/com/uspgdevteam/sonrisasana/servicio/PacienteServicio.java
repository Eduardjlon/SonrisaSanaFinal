package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Paciente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class PacienteServicio extends GenericService<Paciente> {

    public PacienteServicio() {
        super(Paciente.class);
    }

    // ===============================
    // LISTADOS
    // ===============================

    public List<Paciente> listar() {
        return findAll();
    }

    public List<Paciente> listarOrdenados() {
        return executeInTx(em -> em.createQuery(
                        "SELECT p FROM Paciente p ORDER BY p.nombreCompleto",
                        Paciente.class)
                .getResultList()
        );
    }

    // ===============================
    // BUSCAR
    // ===============================

    public Paciente buscarPorId(Long id) {
        return findById(id);
    }

    public List<Paciente> buscar(String filtro) {
        String f = (filtro == null ? "" : filtro.trim().toLowerCase());

        return executeInTx(em -> em.createQuery(
                        "SELECT p FROM Paciente p " +
                                "WHERE LOWER(p.nombreCompleto) LIKE :f " +
                                "OR p.dpi LIKE :f",
                        Paciente.class)
                .setParameter("f", "%" + f + "%")
                .getResultList()
        );
    }

    // ===============================
    // VALIDACIÓN DE DPI
    // ===============================

    public boolean dpiExiste(String dpi, Long excluirId) {

        if (dpi == null || dpi.isBlank()) return false;

        return executeInTx(em -> em.createQuery(
                        "SELECT COUNT(p) FROM Paciente p " +
                                "WHERE p.dpi = :dpi AND p.id <> :id",
                        Long.class)
                .setParameter("dpi", dpi)
                .setParameter("id", excluirId == null ? -1L : excluirId)
                .getSingleResult() > 0
        );
    }

    // ===============================
    // GUARDAR (OVERRIDE)
    // ===============================

    @Override
    public Paciente save(Paciente p) {

        // Validar DPI
        if (dpiExiste(p.getDpi(), p.getId())) {
            throw new RuntimeException("El DPI ya está registrado.");
        }

        // Fecha de creación del expediente SOLO si es nuevo
        if (p.getId() == null && p.getFechaCreacionExpediente() == null) {
            p.setFechaCreacionExpediente(LocalDateTime.now());
        }

        // Actualizar fecha de modificación
        p.setUltimaActualizacion(LocalDateTime.now());

        return super.save(p);
    }
}
