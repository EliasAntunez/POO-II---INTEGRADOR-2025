package com.example.facturacion.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.example.facturacion.modelo.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad MovimientoCuentaCorriente.
 * Registra todos los movimientos (cargos, créditos, facturas, pagos, anulaciones).
 */
@Entity
@Table(name = "movimiento_cuenta_corriente", indexes = {
    @Index(name = "idx_movimiento_cliente", columnList = "cliente_id"),
    @Index(name = "idx_movimiento_fecha", columnList = "fecha_movimiento")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"cliente", "factura", "pago", "notaCredito"})
public class MovimientoCuentaCorriente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente dueño de la cuenta corriente.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Tipo de movimiento (CARGO, CREDITO, PAGO, ANULACION).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    /**
     * Monto del movimiento (siempre positivo).
     */
    @NotNull
    @Column(name = "monto", nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    /**
     * Fecha del movimiento.
     */
    @NotNull
    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    /**
     * Fecha y hora de registro.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Descripción del movimiento.
     */
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Usuario que registró el movimiento.
     */
    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    // ==================== Referencias opcionales ====================

    /**
     * Factura asociada (si el movimiento es por una factura).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    /**
     * Pago asociado (si el movimiento es por un pago).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    /**
     * Nota de crédito asociada (si el movimiento es por anulación).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id")
    private NotaCredito notaCredito;

    /**
     * Movimiento inverso (para anulaciones).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_inverso_id")
    private MovimientoCuentaCorriente movimientoInverso;

    @PrePersist
    protected void onCreate() {
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDate.now();
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    // ==================== Métodos de Negocio ====================

    /**
     * Calcula el impacto de este movimiento en el saldo.
     * CARGO/FACTURA: negativo (aumenta deuda)
     * CREDITO/PAGO: positivo (reduce deuda)
     */
    public BigDecimal calcularImpactoEnSaldo() {
        switch (tipoMovimiento) {
            case CARGO:
            case FACTURA:
                return monto.negate(); // Aumenta la deuda (saldo negativo)
            case CREDITO:
            case PAGO:
            case ANULACION:
                return monto; // Reduce la deuda (saldo positivo)
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Crea un movimiento por factura.
     */
    public static MovimientoCuentaCorriente porFactura(Cliente cliente, Factura factura, String usuario) {
        return MovimientoCuentaCorriente.builder()
            .cliente(cliente)
            .tipoMovimiento(TipoMovimiento.FACTURA)
            .monto(factura.getMontoTotal())
            .factura(factura)
            .descripcion("Factura " + factura.getNumeroFactura())
            .usuarioRegistro(usuario)
            .build();
    }

    /**
     * Crea un movimiento por pago.
     */
    public static MovimientoCuentaCorriente porPago(Cliente cliente, Pago pago, String usuario) {
        return MovimientoCuentaCorriente.builder()
            .cliente(cliente)
            .tipoMovimiento(TipoMovimiento.PAGO)
            .monto(pago.getMonto())
            .pago(pago)
            .descripcion("Pago de factura " + pago.getFactura().getNumeroFactura())
            .usuarioRegistro(usuario)
            .build();
    }

    /**
     * Crea un movimiento por anulación (nota de crédito).
     */
    public static MovimientoCuentaCorriente porAnulacion(Cliente cliente, NotaCredito nc, String usuario) {
        return MovimientoCuentaCorriente.builder()
            .cliente(cliente)
            .tipoMovimiento(TipoMovimiento.ANULACION)
            .monto(nc.getMonto())
            .notaCredito(nc)
            .descripcion("Anulación de factura " + nc.getFactura().getNumeroFactura() + " - NC " + nc.getNumero())
            .usuarioRegistro(usuario)
            .build();
    }
}