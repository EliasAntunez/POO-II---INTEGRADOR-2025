package com.example.facturacion.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "detalle_nota_credito")
@Getter @Setter @NoArgsConstructor
public class DetalleNotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id", nullable = false)
    private NotaCredito notaCredito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "alicuota_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal alicuotaIva;

    @Column(name = "monto_iva", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoIva;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;
}