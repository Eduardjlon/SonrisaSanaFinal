package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Tratamiento;
import com.uspgdevteam.sonrisasana.entidad.EspecialidadOdontologica;
import com.uspgdevteam.sonrisasana.servicio.TratamientoServicio;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Named
@ViewScoped
public class TratamientoBean implements Serializable {

    @Inject
    private TratamientoServicio tratamientoServicio;

    private List<Tratamiento> tratamientos;
    private Tratamiento seleccionado = new Tratamiento();

    @PostConstruct
    public void init() {
        tratamientos = tratamientoServicio.findAll();
    }

    /** Crear nuevo tratamiento */
    public void nuevo() {
        seleccionado = new Tratamiento();
        seleccionado.setDuracionMinutos(30); // valor por defecto
        seleccionado.setCosto(BigDecimal.ZERO);
    }

    /** Editar tratamiento existente */
    public void editar(Tratamiento t) {
        seleccionado = t;
    }

    /** Guardar o actualizar */
    public void guardar() {
        try {
            tratamientoServicio.save(seleccionado);
            tratamientos = tratamientoServicio.findAll();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Tratamiento guardado correctamente", null));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar: " + e.getMessage(), null));
        }
    }

    /** Eliminar tratamiento */
    public void eliminar(Tratamiento t) {
        try {
            tratamientoServicio.delete(t.getId());
            tratamientos.remove(t);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Tratamiento eliminado", null));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se puede eliminar el tratamiento", null));
        }
    }

    /** Limpiar formulario */
    public void cancelar() {
        seleccionado = new Tratamiento();
    }

    // GETTERS / SETTERS

    public List<Tratamiento> getTratamientos() {
        return tratamientos;
    }

    public Tratamiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Tratamiento seleccionado) {
        this.seleccionado = seleccionado;
    }

    /** Para llenar el select de especialidades en el formulario */
    public EspecialidadOdontologica[] getEspecialidades() {
        return EspecialidadOdontologica.values();
    }
}
