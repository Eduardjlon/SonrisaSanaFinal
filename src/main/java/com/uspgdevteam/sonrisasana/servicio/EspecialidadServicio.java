package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.Especialidad;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.List;

@Named
@ApplicationScoped
public class EspecialidadServicio extends GenericService<Especialidad> {

    public EspecialidadServicio() {
        super(Especialidad.class);
    }


    public List<Especialidad> listar() {
        return findAll();
    }


    public Especialidad buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;

        return executeInTx(em -> em.createQuery(
                        "SELECT e FROM Especialidad e WHERE UPPER(e.nombre) = :nombre",
                        Especialidad.class)
                .setParameter("nombre", nombre.toUpperCase())
                .getResultStream()
                .findFirst()
                .orElse(null));
    }


    @Override
    public Especialidad save(Especialidad e) {

        Especialidad existente = buscarPorNombre(e.getNombre());
        if (existente != null && (e.getId() == null || !existente.getId().equals(e.getId()))) {
            throw new RuntimeException("La especialidad ya existe: " + e.getNombre());
        }

        return super.save(e);
    }

    @Override
    public void delete(Long id) {
        executeInTxVoid(em -> {
            Especialidad e = em.find(Especialidad.class, id);
            if (e != null) {
                em.remove(e);
            }
        });
    }
}
