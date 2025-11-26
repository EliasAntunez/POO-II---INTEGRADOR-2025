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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad NotaCredito.
 * HU-09: Anulación de Facturas mediante Nota de Crédito
 */
@Entity
@Table(name = "nota_credito")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "factura")
public class NotaCredito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número de la nota de crédito (generado automáticamente).
     */
    @Column(name = "numero", unique = true, nullable = false, length = 20)
    private String numero;

    /**
     * Factura que se anula con esta nota de crédito.
     * Relación 1-a-1: Una factura puede tener una sola nota de crédito.
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false, unique = true)
    private Factura factura;

    /**
     * Monto de la nota de crédito (debe coincidir con el total de la factura).
     */
    @NotNull
    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    /**
     * Fecha de emisión de la nota de crédito.
     */
    @NotNull
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /**
     * Fecha y hora de registro.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Motivo de la anulación (requerido por HU-09).
     */
    @NotBlank
    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    /**
     * Usuario responsable de la anulación (requerido por HU-09).
     */
    @NotBlank
    @Column(name = "usuario_responsable", nullable = false, length = 100)
    private String usuarioResponsable;

    /**
     * Observaciones adicionales.
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        if (fechaEmision == null) {
            fechaEmision = LocalDate.now();
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    /**
     * Genera el número de nota de crédito basado en el ID.
     * Formato: NC-YYYYMMDD-00000ID
     */
    public void generarNumero() {
        if (numero == null && id != null) {
            String fecha = fechaEmision.toString().replace("-", "");
            numero = String.format("NC-%s-%05d", fecha, id);
        }
    }

    /**
     * Valida que la nota de crédito sea válida.
     */
    public void validar() {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo es requerido");
        }
        if (usuarioResponsable == null || usuarioResponsable.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario responsable es requerido");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }
}