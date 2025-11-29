package com.uspgdevteam.sonrisasana.servicio;

import com.uspgdevteam.sonrisasana.entidad.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Named
@ApplicationScoped
public class FacturaServicio extends GenericService<Factura> {

    public FacturaServicio() {
        super(Factura.class);
    }

    // ==========================================================
    // CREAR FACTURA
    // ==========================================================
    public Factura crearFactura(Factura factura, Cupon cupon, Seguro seguro, BigDecimal montoSeguro) {
        return executeInTx(em -> {
            // Generar número de factura
            if (factura.getNumero() == null || factura.getNumero().isEmpty()) {
                factura.setNumero(generarNumeroFactura(em));
            }

            // Calcular subtotal desde la cita si existe
            if (factura.getCita() != null && factura.getSubtotal() == null) {
                Cita cita = em.find(Cita.class, factura.getCita().getId());
                if (cita != null) {
                    factura.setSubtotal(cita.getTotal());
                }
            }

            // Aplicar cupón si existe
            if (cupon != null && cupon.isActivo()) {
                BigDecimal descuento = factura.getSubtotal()
                        .multiply(new BigDecimal(cupon.getPorcentaje()))
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                factura.setDescuento(descuento);
                factura.setCupon(cupon);
            }

            // Aplicar seguro si existe
            if (seguro != null && montoSeguro != null && montoSeguro.compareTo(BigDecimal.ZERO) > 0) {
                factura.setCoberturaSeguro(montoSeguro);
                factura.setSeguro(seguro);
            }

            // Calcular total
            factura.calcularTotal();

            // Estado inicial
            EstadoFactura estadoInicial = em.find(EstadoFactura.class, "PENDIENTE");
            if (estadoInicial == null) {
                estadoInicial = new EstadoFactura("PENDIENTE");
                em.persist(estadoInicial);
            }
            factura.setEstado(estadoInicial);

            em.persist(factura);
            em.flush();

            return factura;
        });
    }

    // ==========================================================
    // REGISTRAR PAGO
    // ==========================================================
    public Pago registrarPago(Long facturaId, BigDecimal monto, String metodo, String observaciones) {
        return executeInTx(em -> {
            Factura factura = em.find(Factura.class, facturaId);
            if (factura == null) {
                throw new RuntimeException("Factura no encontrada");
            }

            Pago pago = new Pago();
            pago.setFactura(factura);
            pago.setMonto(monto);
            pago.setMetodo(metodo);
            pago.setObservaciones(observaciones);

            em.persist(pago);
            em.flush();

            // Actualizar estado de la factura
            actualizarEstadoFactura(em, factura);

            return pago;
        });
    }

    // ==========================================================
    // ACTUALIZAR ESTADO DE FACTURA
    // ==========================================================
    private void actualizarEstadoFactura(EntityManager em, Factura factura) {
        BigDecimal saldoPendiente = factura.getSaldoPendiente();

        EstadoFactura nuevoEstado;
        if (saldoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            nuevoEstado = em.find(EstadoFactura.class, "PAGADO");
            if (nuevoEstado == null) {
                nuevoEstado = new EstadoFactura("PAGADO");
                em.persist(nuevoEstado);
            }
        } else if (factura.getPagos() != null && !factura.getPagos().isEmpty()) {
            nuevoEstado = em.find(EstadoFactura.class, "PARCIALMENTE_PAGADO");
            if (nuevoEstado == null) {
                nuevoEstado = new EstadoFactura("PARCIALMENTE_PAGADO");
                em.persist(nuevoEstado);
            }
        } else {
            nuevoEstado = em.find(EstadoFactura.class, "PENDIENTE");
            if (nuevoEstado == null) {
                nuevoEstado = new EstadoFactura("PENDIENTE");
                em.persist(nuevoEstado);
            }
        }

        factura.setEstado(nuevoEstado);
        em.merge(factura);
    }

    // ==========================================================
    // GENERAR NÚMERO DE FACTURA
    // ==========================================================
    private String generarNumeroFactura(EntityManager em) {
        String prefijo = "FAC-";
        int año = LocalDateTime.now().getYear();
        String añoStr = String.valueOf(año);

        // Buscar el último número del año
        String jpql = "SELECT f.numero FROM Factura f WHERE f.numero LIKE :patron ORDER BY f.numero DESC";
        List<String> numeros = em.createQuery(jpql, String.class)
                .setParameter("patron", prefijo + añoStr + "-%")
                .setMaxResults(1)
                .getResultList();

        int siguiente = 1;
        if (!numeros.isEmpty()) {
            String ultimoNumero = numeros.get(0);
            try {
                String[] partes = ultimoNumero.split("-");
                siguiente = Integer.parseInt(partes[partes.length - 1]) + 1;
            } catch (Exception e) {
                siguiente = 1;
            }
        }

        return String.format("%s%s-%05d", prefijo, añoStr, siguiente);
    }

    // ==========================================================
    // LISTADOS
    // ==========================================================
    public List<Factura> listarPorPaciente(Paciente paciente) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT f FROM Factura f WHERE f.paciente = :paciente ORDER BY f.fechaEmision DESC",
                                Factura.class)
                        .setParameter("paciente", paciente)
                        .getResultList()
        );
    }

    public List<Factura> listarPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em ->
                em.createQuery(
                                "SELECT f FROM Factura f WHERE f.fechaEmision BETWEEN :inicio AND :fin ORDER BY f.fechaEmision DESC",
                                Factura.class)
                        .setParameter("inicio", inicio)
                        .setParameter("fin", fin)
                        .getResultList()
        );
    }

    public BigDecimal obtenerIngresosPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return executeInTx(em -> {
            String jpql = "SELECT SUM(f.total) FROM Factura f " +
                    "WHERE f.fechaEmision BETWEEN :inicio AND :fin " +
                    "AND f.estado.nombre <> 'ANULADO'";
            BigDecimal resultado = em.createQuery(jpql, BigDecimal.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getSingleResult();
            return resultado != null ? resultado : BigDecimal.ZERO;
        });
    }
}

