package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.DiaSemana;
import com.uspgdevteam.sonrisasana.entidad.HorarioOdontologo;
import com.uspgdevteam.sonrisasana.entidad.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

@Named
@ApplicationScoped
public class HorarioOdontologoServicio extends GenericService<HorarioOdontologo> {

    public HorarioOdontologoServicio() {
        super(HorarioOdontologo.class);
    }

    public List<HorarioOdontologo> buscarPorOdontologoYDia(Usuario odontologo, DiaSemana dia) {
        return executeInTx(em -> em.createQuery(
                        "SELECT h FROM HorarioOdontologo h " +
                                "WHERE h.odontologo = :odo AND h.diaSemana = :dia",
                        HorarioOdontologo.class)
                .setParameter("odo", odontologo)
                .setParameter("dia", dia)
                .getResultList()
        );
    }
}
