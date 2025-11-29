package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.modelo.enums.TipoComprobante;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "factura")
@Getter @Setter @NoArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean anulada = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false)
    private TipoComprobante tipoComprobante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFactura estado = EstadoFactura.EMITIDA; // Valor por defecto para evitar null

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DetalleFactura> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleFactura detalle) {
        this.detalles.add(detalle);
        detalle.setFactura(this);
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) this.fechaEmision = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoFactura.EMITIDA;
        
        // CALCULAR LETRA AUTOMÁTICAMENTE AL GUARDAR
        if (this.tipoComprobante == null && this.cliente != null) {
            this.tipoComprobante = TipoComprobante.getTipoFactura(this.cliente.getCondicionFiscal());
        }
    }
    
    /**
     * Calcula el total de IVA de toda la factura.
     */
    public BigDecimal getMontoTotalIVA() {
        return detalles.stream()
                .map(DetalleFactura::getMontoIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el total neto (sin IVA) de toda la factura.
     */
    public BigDecimal getMontoTotalNeto() {
        // Total Neto = Total Final - Total IVA
        return this.total.subtract(getMontoTotalIVA());
    }

    // Métodos de compatibilidad para MovimientoCuentaCorriente
    public BigDecimal getMontoTotal() { return this.total; }
    public Long getNumeroFactura() { return this.id; }
}