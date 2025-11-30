package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.facturacion.modelo.enums.MedioPago;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un pago realizado por un cliente.
 * Regla AFIP: "Si el pago se realiza en forma parcial o total antes del vencimiento,
 * la factura debe emitirse en el momento del pago."
 */
@Entity
@Table(name = "pago")
@Getter @Setter @NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Un pago aplica a una única factura en este alcance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false)
    private MedioPago medioPago; // EFECTIVO, TRANSFERENCIA, ETC.

    @Column(length = 500)
    private String observaciones;

    // === CAMPO AGREGADO PARA COINCIDIR CON SQL ===
    @Column(name = "usuario_registro", nullable = false)
    private String usuarioRegistro;

    @PrePersist
    public void prePersist() {
        if (this.fechaPago == null) {
            this.fechaPago = LocalDateTime.now();
        }
        // Asignamos valor por defecto para evitar error NOT NULL
        if (this.usuarioRegistro == null) {
            this.usuarioRegistro = "SISTEMA";
        }
    }
}