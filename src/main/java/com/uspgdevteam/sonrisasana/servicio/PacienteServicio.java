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

    // =====================================================
    // LISTADOS
    // =====================================================

    public List<Paciente> listar() {
        return findAll();
    }

    public List<Paciente> listarOrdenados() {
        return executeInTx(em -> em.createQuery(
                        "SELECT p FROM Paciente p ORDER BY p.nombreCompleto",
                        Paciente.class)
                .getResultList());
    }

    // =====================================================
    // BÚSQUEDAS
    // =====================================================

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
                .getResultList());
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    public boolean dpiExiste(String dpi, Long excluirId) {

        if (dpi == null || dpi.isBlank()) return false;

        return executeInTx(em -> em.createQuery(
                        "SELECT COUNT(p) FROM Paciente p " +
                                "WHERE p.dpi = :dpi AND p.id <> :id",
                        Long.class)
                .setParameter("dpi", dpi)
                .setParameter("id", excluirId == null ? -1L : excluirId)
                .getSingleResult() > 0);
    }

    // =====================================================
    // GUARDAR (VALIDACIÓN + DATOS AUTOMÁTICOS)
    // =====================================================

    @Override
    public Paciente save(Paciente p) {

        // Validar DPI duplicado
        if (dpiExiste(p.getDpi(), p.getId())) {
            throw new RuntimeException("El DPI ya está registrado.");
        }

        // Actualizar fecha de última modificación
        p.setUltimaActualizacion(LocalDateTime.now());

        return super.save(p);
    }

    // =====================================================
    // FUNCIONES ÚTILES PARA DASHBOARD
    // =====================================================

    public List<Paciente> listarEnRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em -> em.createQuery(
                        "SELECT p FROM Paciente p " +
                                "WHERE p.fechaCreacionExpediente BETWEEN :ini AND :fin",
                        Paciente.class)
                .setParameter("ini", inicio)
                .setParameter("fin", fin)
                .getResultList());
    }

    public long contarPacientes() {
        return executeInTx(em -> em.createQuery(
                        "SELECT COUNT(p) FROM Paciente p", Long.class)
                .getSingleResult());
    }
}
