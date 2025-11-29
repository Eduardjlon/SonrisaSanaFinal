package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.Cita;
import com.uspgdevteam.sonrisasana.entidad.Factura;
import com.uspgdevteam.sonrisasana.servicio.FacturaServicio;
import com.uspgdevteam.sonrisasana.servicio.CitaServicio;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class FacturaBean implements Serializable {

    @Inject
    private FacturaServicio facturaServicio;

    @Inject
    private CitaServicio citaServicio;

    private List<Factura> facturas;
    private Cita citaSeleccionada;

    @PostConstruct
    public void init() {
        cargarFacturas();
    }

    public void cargarFacturas() {
        try {
            facturas = facturaServicio.listarPorRango(
                    java.time.LocalDateTime.now().minusYears(1),
                    java.time.LocalDateTime.now()
            );
        } catch (Exception e) {
            facturas = List.of();
            e.printStackTrace();
        }
    }

    // Método para generar factura desde cita directamente
    public void generarFacturaDesdeCita(Cita cita) {
        if (cita == null) return;

        this.citaSeleccionada = cita;
        facturaServicio.crearFacturaDesdeCita(citaSeleccionada);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Factura generada correctamente", null));

        cargarFacturas();
    }

    // GETTERS
    public List<Factura> getFacturas() { return facturas; }
    public Cita getCitaSeleccionada() { return citaSeleccionada; }
}
