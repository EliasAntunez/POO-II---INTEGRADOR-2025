package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.facturacion.modelo.enums.EstadoFactura;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad Factura.
 * HU-11: Registrar Pago Total
 */
@Entity
@Table(name = "factura")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"cliente", "detalles"})
public class Factura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente asociado a la factura.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Número de factura (generado automáticamente).
     */
    @Column(name = "numero_factura", unique = true, nullable = false, length = 20)
    private String numeroFactura;

    /**
     * Fecha de emisión de la factura.
     */
    @NotNull
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /**
     * Fecha de pago (solo si está pagada).
     */
    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    /**
     * Estado de la factura.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoFactura estado = EstadoFactura.EMITIDA;

    /**
     * Subtotal (suma de todos los detalles sin IVA).
     */
    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Total de IVA.
     */
    @NotNull
    @Column(name = "total_iva", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalIva = BigDecimal.ZERO;

    /**
     * Monto total de la factura (subtotal + IVA).
     */
    @NotNull
    @Column(name = "monto_total", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal montoTotal = BigDecimal.ZERO;

    /**
     * Usuario que registró el pago (puede ser null si no está pagada).
     */
    @Column(name = "usuario_pago", length = 100)
    private String usuarioPago;

    /**
     * Observaciones adicionales.
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Detalles de la factura (líneas de servicios).
     */
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetalleFactura> detalles = new ArrayList<>();

    /**
     * Establece la fecha de emisión al momento de crear la factura.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaEmision == null) {
            fechaEmision = LocalDate.now();
        }
        if (estado == null) {
            estado = EstadoFactura.EMITIDA;
        }
    }

    // ==================== Métodos de Negocio ====================

    /**
     * Agrega un detalle a la factura.
     */
    public void agregarDetalle(DetalleFactura detalle) {
        detalles.add(detalle);
        detalle.setFactura(this);
    }

    /**
     * Remueve un detalle de la factura.
     */
    public void removerDetalle(DetalleFactura detalle) {
        detalles.remove(detalle);
        detalle.setFactura(null);
    }

    /**
     * Calcula y actualiza los totales de la factura.
     */
    public void calcularTotales() {
        subtotal = BigDecimal.ZERO;
        totalIva = BigDecimal.ZERO;

        for (DetalleFactura detalle : detalles) {
            detalle.calcularTotales();
            subtotal = subtotal.add(detalle.getSubtotal());
            totalIva = totalIva.add(detalle.getTotalIva());
        }

        montoTotal = subtotal.add(totalIva);
    }

    /**
     * HU-11: Registra el pago total de la factura.
     * @param usuarioPago Usuario que registra el pago
     * @throws IllegalStateException si la factura no puede pagarse
     */
    public void registrarPagoTotal(String usuarioPago) {
        if (!estado.puedePagarse()) {
            throw new IllegalStateException(
                "La factura no puede pagarse. Estado actual: " + estado.getDescripcion());
        }

        this.estado = EstadoFactura.PAGADA;
        this.fechaPago = LocalDate.now();
        this.usuarioPago = usuarioPago;
    }

    /**
     * Verifica si la factura puede pagarse.
     */
    public boolean puedePagarse() {
        return estado.puedePagarse();
    }

    /**
     * Anula la factura.
     */
    public void anular() {
        if (estado == EstadoFactura.PAGADA) {
            throw new IllegalStateException("No se puede anular una factura pagada");
        }
        this.estado = EstadoFactura.ANULADA;
    }

    /**
     * Verifica si la factura está pagada.
     */
    public boolean estaPagada() {
        return estado == EstadoFactura.PAGADA;
    }

    /**
     * Genera el número de factura basado en el ID.
     * Formato: FACT-YYYYMMDD-00000ID
     */
    public void generarNumeroFactura() {
        if (numeroFactura == null && id != null) {
            String fecha = fechaEmision.toString().replace("-", "");
            numeroFactura = String.format("FACT-%s-%05d", fecha, id);
        }
    }
}