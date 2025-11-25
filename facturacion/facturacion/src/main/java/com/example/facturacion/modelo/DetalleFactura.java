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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad DetalleFactura (línea de factura).
 * Representa cada servicio incluido en una factura.
 */
@Entity
@Table(name = "detalle_factura")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"factura", "servicio"})
public class DetalleFactura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Factura a la que pertenece este detalle.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    /**
     * Servicio facturado.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    /**
     * Descripción del servicio (copiada al momento de facturar).
     */
    @NotNull
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    /**
     * Cantidad de unidades.
     */
    @NotNull
    @Positive
    @Column(name = "cantidad", nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    /**
     * Precio unitario (copiado del servicio al momento de facturar).
     */
    @NotNull
    @Positive
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Porcentaje de alícuota IVA (copiado del servicio).
     */
    @NotNull
    @Column(name = "porcentaje_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIva;

    /**
     * Subtotal (cantidad * precio unitario).
     */
    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Total de IVA de esta línea.
     */
    @NotNull
    @Column(name = "total_iva", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalIva = BigDecimal.ZERO;

    /**
     * Total de la línea (subtotal + IVA).
     */
    @NotNull
    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    // ==================== Métodos de Negocio ====================

    /**
     * Crea un detalle a partir de un servicio.
     * @param servicio Servicio a facturar
     * @param cantidad Cantidad de unidades
     * @return DetalleFactura creado
     */
    public static DetalleFactura desdeServicio(Servicio servicio, Integer cantidad) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        DetalleFactura detalle = DetalleFactura.builder()
            .servicio(servicio)
            .descripcion(servicio.getNombre() + " - " + servicio.getDescripcion())
            .cantidad(cantidad)
            .precioUnitario(BigDecimal.valueOf(servicio.getPrecio()))
            .porcentajeIva(BigDecimal.valueOf(servicio.getAlicuota().getValor()))
            .build();

        detalle.calcularTotales();
        return detalle;
    }

    /**
     * Calcula los totales de esta línea de factura.
     */
    public void calcularTotales() {
        if (precioUnitario == null || cantidad == null || porcentajeIva == null) {
            throw new IllegalStateException("Faltan datos para calcular totales");
        }

        // Subtotal = cantidad * precio unitario
        subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad))
                                .setScale(2, RoundingMode.HALF_UP);

        // IVA = subtotal * (porcentaje / 100)
        totalIva = subtotal.multiply(porcentajeIva)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Total = subtotal + IVA
        total = subtotal.add(totalIva);
    }

    /**
     * Actualiza la cantidad y recalcula totales.
     */
    public void actualizarCantidad(Integer nuevaCantidad) {
        if (nuevaCantidad == null || nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        this.cantidad = nuevaCantidad;
        calcularTotales();
    }
}