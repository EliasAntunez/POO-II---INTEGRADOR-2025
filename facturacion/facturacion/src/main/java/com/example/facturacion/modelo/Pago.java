package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.facturacion.modelo.enums.MedioPago;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Entidad Pago.
 * HU-06: Registro de Pagos (efectivo, transferencia, cheque, etc.)
 */
@Entity
@Table(name = "pago")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"factura", "cliente"})
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente que realizó el pago.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Factura a la que pertenece este pago.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    /**
     * Monto del pago.
     */
    @NotNull
    @Positive
    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    /**
     * Medio de pago utilizado.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 20)
    @Builder.Default
    private MedioPago medioPago = MedioPago.EFECTIVO;

    /**
     * Referencia del pago (número de cheque, transferencia, etc).
     */
    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago;

    /**
     * Fecha en que se realizó el pago.
     */
    @NotNull
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    /**
     * Fecha y hora de registro del pago (timestamp).
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Usuario que registró el pago.
     */
    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    /**
     * Observaciones sobre el pago (opcional).
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Indica si es un pago total (true) o parcial (false).
     */
    @Column(name = "es_pago_total", nullable = false)
    @Builder.Default
    private boolean esPagoTotal = false;

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDate.now();
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (cliente == null && factura != null) {
            cliente = factura.getCliente();
        }
    }

    public void validar() {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es requerido");
        }
    }
}