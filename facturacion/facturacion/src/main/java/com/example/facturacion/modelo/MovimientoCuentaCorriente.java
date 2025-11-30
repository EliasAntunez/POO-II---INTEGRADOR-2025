package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.facturacion.modelo.enums.TipoMovimiento;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimiento_cuenta_corriente")
@Getter @Setter @NoArgsConstructor
public class MovimientoCuentaCorriente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Opcional: Relaciones para navegación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id")
    private NotaCredito notaCredito;
    
    // Si tienes pagos:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "usuario_registro")
    private String usuarioRegistro;

    // === MÉTODO CLAVE PARA LA VISTA ===
    public BigDecimal calcularImpactoEnSaldo() {
        if (this.monto == null) return BigDecimal.ZERO;
        
        switch (this.tipoMovimiento) {
            case FACTURA:
            case CARGO:
                return this.monto; // Suman Deuda (Positivo)
            case PAGO:
            case CREDITO:
            case ANULACION:
                return this.monto.negate(); // Restan Deuda (Negativo)
            default:
                return BigDecimal.ZERO;
        }
    }
    
    // Métodos estáticos helpers (como los que tenías antes) se mantienen igual...
    public static MovimientoCuentaCorriente porFactura(Cliente cliente, Factura factura, String usuario) {
        MovimientoCuentaCorriente m = new MovimientoCuentaCorriente();
        m.setCliente(cliente);
        m.setFactura(factura);
        m.setTipoMovimiento(TipoMovimiento.FACTURA);
        m.setMonto(factura.getMontoTotal());
        m.setDescripcion("Factura N° " + factura.getNumeroFactura());
        m.setUsuarioRegistro(usuario);
        m.setFechaMovimiento(LocalDateTime.now());
        return m;
    }
    
    public static MovimientoCuentaCorriente porAnulacion(Cliente cliente, NotaCredito nc, String usuario) {
        MovimientoCuentaCorriente m = new MovimientoCuentaCorriente();
        m.setCliente(cliente);
        m.setNotaCredito(nc);
        m.setTipoMovimiento(TipoMovimiento.ANULACION);
        m.setMonto(nc.getMonto());
        m.setDescripcion("Nota de Crédito N° " + nc.getNumero());
        m.setUsuarioRegistro(usuario);
        m.setFechaMovimiento(LocalDateTime.now());
        return m;
    }
}