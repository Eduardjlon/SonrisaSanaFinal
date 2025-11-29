package com.uspgdevteam.sonrisasana.web;

import com.uspgdevteam.sonrisasana.entidad.*;
import com.uspgdevteam.sonrisasana.servicio.*;
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
public class FacturaBean implements Serializable {

    @Inject
    private FacturaServicio facturaServicio;

    @Inject
    private PacienteServicio pacienteServicio;

    @Inject
    private CitaServicio citaServicio;

    @Inject
    private CuponServicio cuponServicio;

    @Inject
    private SeguroServicio seguroServicio;


    private Factura factura;
    private List<Factura> facturas;
    private List<Paciente> pacientes;
    private List<Cita> citas;
    private List<Cupon> cupones;
    private List<Seguro> seguros;

    // Campos para formulario
    private Long pacienteId;
    private Long citaId;
    private String codigoCupon;
    private Long seguroId;
    private BigDecimal montoSeguro;
    private BigDecimal subtotalManual;

    // Pago
    private Pago pago;
    private BigDecimal montoPago;
    private String metodoPago;
    private String observacionesPago;

    @PostConstruct
    public void init() {
        nuevaFactura();
        cargarListas();
        cargarFacturas();
    }

    private void cargarListas() {
        pacientes = pacienteServicio.listar();
        citas = citaServicio.listarTodas();
        cupones = cuponServicio.findAll();
        seguros = seguroServicio.findAll();
    }

    private void cargarFacturas() {
        facturas = facturaServicio.findAll();
    }

    public void nuevaFactura() {
        factura = new Factura();
        pacienteId = null;
        citaId = null;
        codigoCupon = null;
        seguroId = null;
        montoSeguro = BigDecimal.ZERO;
        subtotalManual = null;
    }

    // ==========================================================
    // SELECCIONAR CITA
    // ==========================================================
    public void onCitaSelect() {
        if (citaId != null) {
            Cita cita = citaServicio.findById(citaId);
            if (cita != null) {
                factura.setCita(cita);
                factura.setPaciente(cita.getPaciente());
                factura.setSubtotal(cita.getTotal());
                pacienteId = cita.getPaciente().getId();
                calcularTotal();
            }
        }
    }

    // ==========================================================
    // APLICAR CUPÓN
    // ==========================================================
    public void aplicarCupon() {
        if (codigoCupon == null || codigoCupon.trim().isEmpty()) {
            mensaje("Ingrese un código de cupón", FacesMessage.SEVERITY_WARN);
            return;
        }

        Cupon cupon = cuponServicio.buscarPorCodigo(codigoCupon.trim().toUpperCase());
        if (cupon == null) {
            mensaje("Cupón no encontrado o inactivo", FacesMessage.SEVERITY_ERROR);
            return;
        }

        if (factura.getSubtotal() == null || factura.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
            mensaje("Debe tener un subtotal para aplicar el cupón", FacesMessage.SEVERITY_WARN);
            return;
        }

        BigDecimal descuento = factura.getSubtotal()
                .multiply(new BigDecimal(cupon.getPorcentaje()))
                .divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);

        factura.setDescuento(descuento);
        factura.setCupon(cupon);
        calcularTotal();
        mensaje("Cupón aplicado: " + cupon.getPorcentaje() + "%", FacesMessage.SEVERITY_INFO);
    }

    // ==========================================================
    // APLICAR SEGURO
    // ==========================================================
    public void aplicarSeguro() {
        if (seguroId == null) {
            mensaje("Seleccione un seguro", FacesMessage.SEVERITY_WARN);
            return;
        }

        if (montoSeguro == null || montoSeguro.compareTo(BigDecimal.ZERO) <= 0) {
            mensaje("Ingrese el monto de cobertura del seguro", FacesMessage.SEVERITY_WARN);
            return;
        }

        Seguro seguro = seguroServicio.findById(seguroId);
        if (seguro == null) {
            mensaje("Seguro no encontrado", FacesMessage.SEVERITY_ERROR);
            return;
        }

        factura.setSeguro(seguro);
        factura.setCoberturaSeguro(montoSeguro);
        calcularTotal();
        mensaje("Seguro aplicado: " + seguro.getNombre(), FacesMessage.SEVERITY_INFO);
    }

    // ==========================================================
    // CALCULAR TOTAL
    // ==========================================================
    public void calcularTotal() {
        if (factura.getSubtotal() == null) {
            if (subtotalManual != null) {
                factura.setSubtotal(subtotalManual);
            } else {
                factura.setSubtotal(BigDecimal.ZERO);
            }
        }

        factura.calcularTotal();
    }

    // ==========================================================
    // GUARDAR FACTURA
    // ==========================================================
    public void guardar() {
        try {
            if (factura.getPaciente() == null && pacienteId != null) {
                factura.setPaciente(pacienteServicio.findById(pacienteId));
            }

            if (factura.getSubtotal() == null) {
                if (subtotalManual != null) {
                    factura.setSubtotal(subtotalManual);
                } else {
                    mensaje("Debe ingresar un subtotal", FacesMessage.SEVERITY_ERROR);
                    return;
                }
            }

            calcularTotal();

            Cupon cupon = factura.getCupon();
            Seguro seguro = factura.getSeguro();
            BigDecimal montoSeg = factura.getCoberturaSeguro();

            facturaServicio.crearFactura(factura, cupon, seguro, montoSeg);
            mensaje("Factura creada correctamente: " + factura.getNumero(), FacesMessage.SEVERITY_INFO);

            cargarFacturas();
            nuevaFactura();

        } catch (Exception e) {
            mensaje("Error al crear factura: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }
    }

    // ==========================================================
    // REGISTRAR PAGO
    // ==========================================================
    public void prepararPago(Factura facturaSeleccionada) {
        factura = facturaSeleccionada;
        pago = new Pago();
        montoPago = factura.getSaldoPendiente();
        metodoPago = "EFECTIVO";
        observacionesPago = null;
    }

    public void registrarPago() {
        try {
            if (montoPago == null || montoPago.compareTo(BigDecimal.ZERO) <= 0) {
                mensaje("El monto del pago debe ser mayor a cero", FacesMessage.SEVERITY_ERROR);
                return;
            }

            if (montoPago.compareTo(factura.getSaldoPendiente()) > 0) {
                mensaje("El monto excede el saldo pendiente", FacesMessage.SEVERITY_ERROR);
                return;
            }

            facturaServicio.registrarPago(factura.getId(), montoPago, metodoPago, observacionesPago);
            mensaje("Pago registrado correctamente", FacesMessage.SEVERITY_INFO);

            cargarFacturas();
            factura = facturaServicio.findById(factura.getId());

        } catch (Exception e) {
            mensaje("Error al registrar pago: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }
    }

    // ==========================================================
    // UTILIDADES
    // ==========================================================
    private void mensaje(String msg, FacesMessage.Severity sev) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(sev, msg, null));
    }

    // ==========================================================
    // GETTERS / SETTERS
    // ==========================================================
    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }

    public List<Factura> getFacturas() { return facturas; }
    public void setFacturas(List<Factura> facturas) { this.facturas = facturas; }

    public List<Paciente> getPacientes() { return pacientes; }
    public void setPacientes(List<Paciente> pacientes) { this.pacientes = pacientes; }

    public List<Cita> getCitas() { return citas; }
    public void setCitas(List<Cita> citas) { this.citas = citas; }

    public List<Cupon> getCupones() { return cupones; }
    public void setCupones(List<Cupon> cupones) { this.cupones = cupones; }

    public List<Seguro> getSeguros() { return seguros; }
    public void setSeguros(List<Seguro> seguros) { this.seguros = seguros; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getCitaId() { return citaId; }
    public void setCitaId(Long citaId) { 
        this.citaId = citaId;
        onCitaSelect();
    }

    public String getCodigoCupon() { return codigoCupon; }
    public void setCodigoCupon(String codigoCupon) { this.codigoCupon = codigoCupon; }

    public Long getSeguroId() { return seguroId; }
    public void setSeguroId(Long seguroId) { this.seguroId = seguroId; }

    public BigDecimal getMontoSeguro() { return montoSeguro; }
    public void setMontoSeguro(BigDecimal montoSeguro) { this.montoSeguro = montoSeguro; }

    public BigDecimal getSubtotalManual() { return subtotalManual; }
    public void setSubtotalManual(BigDecimal subtotalManual) { 
        this.subtotalManual = subtotalManual;
        if (subtotalManual != null) {
            factura.setSubtotal(subtotalManual);
            calcularTotal();
        }
    }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public BigDecimal getMontoPago() { return montoPago; }
    public void setMontoPago(BigDecimal montoPago) { this.montoPago = montoPago; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getObservacionesPago() { return observacionesPago; }
    public void setObservacionesPago(String observacionesPago) { this.observacionesPago = observacionesPago; }
}

