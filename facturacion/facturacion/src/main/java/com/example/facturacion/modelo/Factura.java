package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // REGLA AFIP: "La factura debe emitirse antes o en la fecha del primer vencimiento"
    // REGLA AFIP: "Plazo de emisión: hasta 10 días corridos posteriores"
    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    // REGLA AFIP: Define el límite para la facturación de servicios continuos
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento; 

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false)
    private TipoComprobante tipoComprobante;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    // Control de saldo para pagos parciales
    @Column(name = "monto_pagado", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFactura estado = EstadoFactura.PENDIENTE_PAGO;

    @Column(nullable = false)
    private boolean anulada = false;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DetalleFactura> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleFactura detalle) {
        this.detalles.add(detalle);
        detalle.setFactura(this);
    }

    /**
     * Suma el IVA de todos los detalles.
     * Thymeleaf lo llama como: factura.montoTotalIVA
     */
    public BigDecimal getMontoTotalIVA() {
        if (detalles == null || detalles.isEmpty()) return BigDecimal.ZERO;
        
        return detalles.stream()
                .map(d -> d.getMontoIva() != null ? d.getMontoIva() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el Neto (Total - IVA).
     * Thymeleaf lo llama como: factura.montoTotalNeto
     */
    public BigDecimal getMontoTotalNeto() {
        // Si total es null, devolvemos 0
        BigDecimal totalSeguro = this.total != null ? this.total : BigDecimal.ZERO;
        return totalSeguro.subtract(getMontoTotalIVA());
    }

    /**
     * Filtra y suma el IVA por alícuota (para el desglose del footer).
     * Thymeleaf lo llama como: factura.getIvaPorAlicuota(21.0)
     */
    public BigDecimal getIvaPorAlicuota(double valorAlicuota) {
        if (detalles == null || detalles.isEmpty()) return BigDecimal.ZERO;

        BigDecimal target = BigDecimal.valueOf(valorAlicuota);
        
        return detalles.stream()
                .filter(d -> d.getAlicuotaIva() != null && 
                             d.getAlicuotaIva().stripTrailingZeros().compareTo(target.stripTrailingZeros()) == 0)
                .map(DetalleFactura::getMontoIva)
                .filter(val -> val != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) {
            this.fechaEmision = LocalDateTime.now();
        }
        // Por defecto, vencimiento a 10 días si no se especifica (Regla general de puesta a disposición)
        if (this.fechaVencimiento == null) {
            this.fechaVencimiento = LocalDate.now().plusDays(10);
        }
        if (this.estado == null) {
            this.estado = EstadoFactura.PENDIENTE_PAGO;
        }
        // Regla AFIP: Almacenamiento obligatorio por 10 años (Esto se gestiona a nivel Backup/DB)
    }

    // Helpers
    public BigDecimal getSaldoPendiente() {
        return total.subtract(montoPagado);
    }
    
    public BigDecimal getMontoTotal() { return this.total; }
    public Long getNumeroFactura() { return this.id; }
}