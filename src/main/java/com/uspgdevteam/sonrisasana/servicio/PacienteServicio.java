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

    // ================================
    // LISTAR TODOS
    // ================================
    public List<Paciente> listar() {
        return findAll();
    }

    // ================================
    // BUSCAR POR NOMBRE O DPI
    // ================================
    public List<Paciente> buscar(String filtro) {
        String f = filtro == null ? "" : filtro.trim();

        return executeInTx(em -> em.createQuery(
                        "SELECT p FROM Paciente p " +
                                "WHERE LOWER(p.nombreCompleto) LIKE :filtro " +
                                "OR p.dpi LIKE :filtro",
                        Paciente.class)
                .setParameter("filtro", "%" + f.toLowerCase() + "%")
                .getResultList()
        );
    }

    // ================================
    // VALIDAR DPI DUPLICADO
    // ================================
    public boolean dpiExiste(String dpi, Long excluirId) {

        if (dpi == null || dpi.isBlank()) return false;

        return executeInTx(em ->
                em.createQuery(
                                "SELECT COUNT(p) FROM Paciente p " +
                                        "WHERE p.dpi = :dpi AND p.id <> :id",
                                Long.class)
                        .setParameter("dpi", dpi)
                        .setParameter("id", excluirId == null ? -1L : excluirId)
                        .getSingleResult() > 0
        );
    }

    // ================================
    // GUARDAR PACIENTE (opcional pero recomendado)
    // ================================
    /**
     * Sobrescribimos guardar() para asegurar validaciones
     * y tener un punto centralizado de reglas futuras.
     */
    @Override
    public Paciente guardar(Paciente p) {

        // Validación DPI única antes de persistir
        if (dpiExiste(p.getDpi(), p.getId())) {
            throw new RuntimeException("El DPI ya está registrado.");
        }

        return super.guardar(p);
    }
}
