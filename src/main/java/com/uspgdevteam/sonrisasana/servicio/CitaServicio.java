package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class CitaServicio extends GenericService<Cita> {

    @Inject
    private HorarioOdontologoServicio horarioServicio;

    public CitaServicio() {
        super(Cita.class);
    }

    public List<Cita> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em -> em.createQuery(
                        "SELECT c FROM Cita c WHERE c.fechaInicio < :fin AND c.fechaFin > :inicio",
                        Cita.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList()
        );
    }

    public boolean hayTraslape(Cita cita) {
        return executeInTx(em -> em.createQuery(
                        "SELECT COUNT(c) FROM Cita c " +
                                "WHERE c.odontologo = :odo " +
                                "AND c.id <> :id " +
                                "AND c.fechaInicio < :fin " +
                                "AND c.fechaFin > :inicio",
                        Long.class)
                .setParameter("odo", cita.getOdontologo())
                .setParameter("id", cita.getId() == null ? -1L : cita.getId())
                .setParameter("inicio", cita.getFechaInicio())
                .setParameter("fin", cita.getFechaFin())
                .getSingleResult() > 0
        );
    }

    public boolean esHorarioValido(Cita cita) {
        DiaSemana dia = mapearDia(cita.getFechaInicio().getDayOfWeek());

        List<HorarioOdontologo> horarios = horarioServicio.buscarPorOdontologoYDia(cita.getOdontologo(), dia);
        if (horarios.isEmpty()) {
            return false; // no atiende ese día
        }

        return horarios.stream().anyMatch(h ->
                !cita.getFechaInicio().toLocalTime().isBefore(h.getHoraInicio()) &&
                        !cita.getFechaFin().toLocalTime().isAfter(h.getHoraFin())
        );
    }

    private DiaSemana mapearDia(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}
