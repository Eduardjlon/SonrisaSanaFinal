package com.uspgdevteam.sonrisasana.entidad;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "facturas")
public class Factura implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @OneToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "cobertura_seguro", precision = 10, scale = 2)
    private BigDecimal coberturaSeguro = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "cupon_id")
    private Cupon cupon;

    @ManyToOne
    @JoinColumn(name = "seguro_id")
    private Seguro seguro;

    @ManyToOne
    @JoinColumn(name = "estado", referencedColumnName = "nombre")
    private EstadoFactura estado;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    public Factura() {
    }

    @PrePersist
    public void prePersist() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
        calcularTotal();
    }

    @PreUpdate
    public void preUpdate() {
        calcularTotal();
    }

    public void calcularTotal() {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }
        if (coberturaSeguro == null) {
            coberturaSeguro = BigDecimal.ZERO;
        }
        total = subtotal.subtract(descuento).subtract(coberturaSeguro);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getCoberturaSeguro() {
        return coberturaSeguro;
    }

    public void setCoberturaSeguro(BigDecimal coberturaSeguro) {
        this.coberturaSeguro = coberturaSeguro;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Cupon getCupon() {
        return cupon;
    }

    public void setCupon(Cupon cupon) {
        this.cupon = cupon;
    }

    public Seguro getSeguro() {
        return seguro;
    }

    public void setSeguro(Seguro seguro) {
        this.seguro = seguro;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    // Método auxiliar para calcular el saldo pendiente
    public BigDecimal getSaldoPendiente() {
        if (pagos == null || pagos.isEmpty()) {
            return total != null ? total : BigDecimal.ZERO;
        }
        BigDecimal pagado = pagos.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = total.subtract(pagado);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    // Método auxiliar para verificar si está pagada
    public boolean isPagada() {
        return getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        Factura factura = (Factura) o;
        return id != null && id.equals(factura.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
