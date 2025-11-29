package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Usuario;
import com.uspgdevteam.sonrisasana.entidad.Cita;

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

    // ===========================================================
    // LISTADOS GENERALES
    // ===========================================================

    public List<Usuario> listar() {
        return findAll();
    }

    public Usuario buscarPorUsername(String username) {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u " +
                                "WHERE u.username = :username AND u.activo = true",
                        Usuario.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    // ===========================================================
    // LOGIN
    // ===========================================================

    /** ✔ Login correcto alineado a SQL (password_hash) */
    public Usuario login(String username, String password) {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u " +
                                "WHERE u.username = :user " +
                                "AND u.passwordHash = :pass " +
                                "AND u.activo = true",
                        Usuario.class)
                .setParameter("user", username)
                .setParameter("pass", password)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    // ===========================================================
    // VALIDACIÓN DE DATOS
    // ===========================================================

    public boolean emailExiste(String email, Long excluirId) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT COUNT(u) FROM Usuario u " +
                                        "WHERE u.email = :email AND u.id <> :id",
                                Long.class)
                        .setParameter("email", email)
                        .setParameter("id", excluirId == null ? -1L : excluirId)
                        .getSingleResult() > 0
        );
    }

    /** ✔ Listar únicamente odontólogos */
    public List<Usuario> listarOdontologos() {
        return executeInTx(em -> em.createQuery(
                        "SELECT u FROM Usuario u " +
                                "WHERE u.rol.nombre = 'ODONTOLOGO' AND u.activo = true",
                        Usuario.class)
                .getResultList()
        );
    }

    // ===========================================================
    // HORARIOS
    // ===========================================================

    /** ✔ Valida si un odontólogo tiene horario disponible */
    public boolean estaDentroDeHorario(Usuario odo, LocalDateTime inicio, LocalDateTime fin) {

        // Si no tiene horario asignado → se permite
        if (odo.getDiaInicio() == null || odo.getDiaFin() == null ||
                odo.getHoraInicio() == null || odo.getHoraFin() == null) {
            return true;
        }

        int dia = inicio.getDayOfWeek().getValue(); // 1 = Lunes ... 7 = Domingo

        int inicioDia = convertirDia(odo.getDiaInicio());
        int finDia    = convertirDia(odo.getDiaFin());

        boolean dentroDia = dia >= inicioDia && dia <= finDia;
        if (!dentroDia) return false;

        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();

        return !horaInicio.isBefore(odo.getHoraInicio()) &&
                !horaFin.isAfter(odo.getHoraFin());
    }

    private int convertirDia(String dia) {
        return switch (dia.toUpperCase()) {
            case "LUNES" -> 1;
            case "MARTES" -> 2;
            case "MIERCOLES" -> 3;
            case "JUEVES" -> 4;
            case "VIERNES" -> 5;
            case "SABADO" -> 6;
            default -> 7; // DOMINGO
        };
    }

    // ===========================================================
    // TRASLAPE DE CITAS PARA UN ODONTÓLOGO
    // ===========================================================

    public List<Cita> listarCitasDeOdontologoEnRango(Long odontologoId,
                                                     LocalDateTime inicio,
                                                     LocalDateTime fin) {

        return executeInTx(em -> em.createQuery(
                        "SELECT c FROM Cita c " +
                                "WHERE c.odontologo.id = :odo " +
                                "AND c.fechaFin > :inicio " +
                                "AND c.fechaInicio < :fin",
                        Cita.class)
                .setParameter("odo", odontologoId)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList()
        );
    }
}
