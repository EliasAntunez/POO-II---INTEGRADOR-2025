package com.example.facturacion.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
@Table(name = "detalle_factura")
@Getter @Setter @NoArgsConstructor
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(nullable = false)
    private Integer cantidad;

    // Precio unitario SIN IVA
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    // Porcentaje de IVA aplicado (ej: 21.00)
    @Column(name = "alicuota_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal alicuotaIva;

    // Monto calculado del IVA (ej: $100 * 21% = $21)
    @Column(name = "monto_iva", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoIva;

    // Subtotal FINAL con IVA incluido ( (Precio + IVA) * Cantidad )
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calcula IVA y Subtotal basado en precio unitario, alícuota y cantidad.
     */
    public void calcularMontos() {
        if (precioUnitario == null || cantidad == null || alicuotaIva == null) {
            this.subtotal = BigDecimal.ZERO;
            this.montoIva = BigDecimal.ZERO;
            return;
        }

        BigDecimal cantidadBD = BigDecimal.valueOf(cantidad);

        // 1. Calcular precio total neto (sin IVA) de este ítem
        BigDecimal totalNeto = precioUnitario.multiply(cantidadBD);

        // 2. Calcular monto de IVA
        // Fórmula: TotalNeto * (Alicuota / 100)
        BigDecimal porcentaje = alicuotaIva.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        this.montoIva = totalNeto.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);

        // 3. Subtotal Final = Neto + IVA
        this.subtotal = totalNeto.add(this.montoIva);
    }
}