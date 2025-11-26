package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad Pago.
 * HU-12: Registrar Pago Parcial
 * Representa un pago (total o parcial) realizado sobre una factura.
 */
@Entity
@Table(name = "pago")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "factura")
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    /**
     * Establece la fecha de pago y registro al momento de crear.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDate.now();
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    // ==================== Métodos de Negocio ====================

    /**
     * Valida que el pago sea válido.
     */
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