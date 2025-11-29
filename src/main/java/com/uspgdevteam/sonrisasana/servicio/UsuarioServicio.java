package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Especialidad;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Named
@ApplicationScoped
public class UsuarioServicio extends GenericService<Usuario> {

    public UsuarioServicio() {
        super(Usuario.class);
    }

    // ============================================================
    // LISTADOS
    // ============================================================

    public List<Usuario> listar() {
        // 🔹 Solo usuarios activos
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.activo = true",
                        Usuario.class)
                .getResultList());
    }

    public Usuario buscarPorUsername(String username) {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.username = :username AND u.activo = true",
                        Usuario.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    public Usuario buscarPorEmail(String email) {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.email = :email AND u.activo = true",
                        Usuario.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    // ============================================================
    // LOGIN (contraseña en texto plano)
    // ============================================================

    public Usuario login(String username, String password) {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.username = :user AND u.passwordHash = :pass AND u.activo = true",
                        Usuario.class)
                .setParameter("user", username)
                .setParameter("pass", password)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================

    public boolean emailExiste(String email, Long excluirId) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email AND u.id <> :id",
                                Long.class)
                        .setParameter("email", email)
                        .setParameter("id", excluirId == null ? -1L : excluirId)
                        .getSingleResult() > 0);
    }

    public boolean usernameExiste(String username, Long excluirId) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT COUNT(u) FROM Usuario u WHERE u.username = :username AND u.id <> :id",
                                Long.class)
                        .setParameter("username", username)
                        .setParameter("id", excluirId == null ? -1L : excluirId)
                        .getSingleResult() > 0);
    }

    // ============================================================
    // ODONTOLOGOS
    // ============================================================

    public List<Usuario> listarOdontologos() {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE UPPER(u.rol.nombre) = 'ODONTOLOGO' AND u.activo = true",
                        Usuario.class)
                .getResultList());
    }

    public List<Usuario> listarOdontologosPorEspecialidad(Especialidad esp) {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.rol.nombre = 'ODONTOLOGO' AND u.especialidad = :esp AND u.activo = true",
                        Usuario.class)
                .setParameter("esp", esp)
                .getResultList());
    }

    // ============================================================
    // HORARIOS
    // ============================================================

    public boolean estaDentroDeHorario(Usuario odo, LocalDateTime inicio, LocalDateTime fin) {
        if (!odo.esOdontologo()) return true;
        if (odo.getDiaInicio() == null || odo.getDiaFin() == null ||
                odo.getHoraInicio() == null || odo.getHoraFin() == null) return true;

        int dia = inicio.getDayOfWeek().getValue(); // 1 = lunes ... 7 = domingo
        int inicioDia = convertirDia(odo.getDiaInicio());
        int finDia = convertirDia(odo.getDiaFin());

        boolean dentroDia = dia >= inicioDia && dia <= finDia;
        if (!dentroDia) return false;

        LocalTime horaI = inicio.toLocalTime();
        LocalTime horaF = fin.toLocalTime();

        return !horaI.isBefore(odo.getHoraInicio()) && !horaF.isAfter(odo.getHoraFin());
    }

    private int convertirDia(String dia) {
        return switch (dia.toUpperCase()) {
            case "LUNES" -> 1;
            case "MARTES" -> 2;
            case "MIERCOLES" -> 3;
            case "JUEVES" -> 4;
            case "VIERNES" -> 5;
            case "SABADO" -> 6;
            default -> 7; // domingo
        };
    }

    // ============================================================
    // TRASLAPE DE CITAS
    // ============================================================

    public List<Cita> listarCitasDeOdontologoEnRango(Long odontologoId,
                                                     LocalDateTime inicio,
                                                     LocalDateTime fin) {
        return executeInTx(em -> em.createQuery(
                        "SELECT c FROM Cita c WHERE c.odontologo.id = :odo AND c.fechaFin > :inicio AND c.fechaInicio < :fin",
                        Cita.class)
                .setParameter("odo", odontologoId)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList());
    }

    // ============================================================
    // GUARDAR USUARIO (nueva o actualización)
    // ============================================================

    @Override
    public Usuario save(Usuario u) {
        if (u.getId() == null) {
            // Nuevo usuario
            if (u.getPasswordHash() == null || u.getPasswordHash().isEmpty()) {
                u.setPasswordHash("123456");
            }
            u.setActivo(true); // por si acaso
            return executeInTx(em -> {
                em.persist(u);
                em.flush();
                return u;
            });
        } else {
            // Actualización
            return executeInTx(em -> em.merge(u));
        }
    }

    // ============================================================
    // ELIMINAR USUARIO FÍSICO (no usado en el bean)
    // ============================================================

    @Override
    public void delete(Long id) {
        executeInTxVoid(em -> {
            Usuario u = em.find(Usuario.class, id);
            if (u != null) em.remove(u);
        });
    }

    // ============================================================
    // DESACTIVAR USUARIO (BORRADO LÓGICO)
    // ============================================================

    public void desactivar(Usuario u) {
        executeInTxVoid(em -> {
            Usuario managed = em.find(Usuario.class, u.getId());
            if (managed != null) {
                managed.setActivo(false);
            }
        });
    }
}
