package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nota_credito")
@Getter @Setter @NoArgsConstructor
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación directa con la factura que anula
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false, unique = true)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    // Campo requerido por tu DB
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(length = 500)
    private String motivo;

    // === NUEVO CAMPO PARA SOLUCIONAR EL ERROR ===
    @Column(name = "usuario_responsable", nullable = false)
    private String usuarioResponsable;

    @OneToMany(mappedBy = "notaCredito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleNotaCredito> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleNotaCredito detalle) {
        this.detalles.add(detalle);
        detalle.setNotaCredito(this);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false)
    private TipoComprobante tipoComprobante;

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) {
            this.fechaEmision = LocalDateTime.now();
        }
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        // Asignar valor por defecto para evitar el error de NULL
        if (this.usuarioResponsable == null) {
            this.usuarioResponsable = "SISTEMA"; 
        }

        if (this.tipoComprobante == null && this.cliente != null) {
            this.tipoComprobante = TipoComprobante.getTipoNotaCredito(this.cliente.getCondicionFiscal());
        }
    }
    
    // Métodos puente
    public BigDecimal getMonto() { return this.total; }
    public Long getNumero() { return this.id; }

    // ... (resto de la clase igual) ...

    // =============================================================
    // MÉTODOS DE CÁLCULO PARA LA VISTA (AFIP STYLE)
    // =============================================================

    /**
     * Calcula el total de IVA de toda la NC.
     */
    public BigDecimal getTotalIva() {
        if (detalles == null) return BigDecimal.ZERO;
        return detalles.stream()
                .map(DetalleNotaCredito::getMontoIva)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    /**
     * Calcula el Importe Neto Gravado (Total Final - Total IVA).
     */
    public BigDecimal getImporteNetoGravado() {
        if (this.total == null) return BigDecimal.ZERO;
        return this.total.subtract(getTotalIva());
    }

    /**
     * Calcula el total de IVA para una alícuota específica (ej: 21.0).
     * Se usa para mostrar el desglose en el pie de página.
     */
    public BigDecimal getIvaPorAlicuota(double valorAlicuota) {
        if (detalles == null) return BigDecimal.ZERO;
        BigDecimal target = BigDecimal.valueOf(valorAlicuota);
        
        return detalles.stream()
                .filter(d -> d.getAlicuotaIva() != null && 
                             d.getAlicuotaIva().stripTrailingZeros().compareTo(target.stripTrailingZeros()) == 0)
                .map(DetalleNotaCredito::getMontoIva)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}