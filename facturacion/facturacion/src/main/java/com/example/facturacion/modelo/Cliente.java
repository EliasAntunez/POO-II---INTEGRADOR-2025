package com.example.facturacion.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.example.facturacion.modelo.enums.CondicionFiscal;
import com.example.facturacion.modelo.enums.EstadoCliente;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de entidad Cliente con Cuenta Corriente integrada.
 * HU-01: Alta de Cliente
 * HU-02: Modificación de Cliente
 * HU-03: Baja de Cliente
 */
@Entity
@Table(name = "cliente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"movimientos", "facturas", "pagos"})
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{7,8}", message = "DNI debe tener 7 u 8 dígitos")
    @Column(name = "dni", unique = true, nullable = false, length = 8)
    private String dni;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 255)
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "CUIT debe tener 11 dígitos")
    @Column(name = "cuit", unique = true, nullable = false, length = 11)
    private String cuit;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Teléfono inválido")
    @Column(name = "telefono", unique = true, nullable = false, length = 15)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    @Column(name = "direccion", nullable = false)
    private String direccion;

    @NotNull(message = "La condición fiscal es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_fiscal", nullable = false, length = 30)
    private CondicionFiscal condicionFiscal;

    /**
     * Estado del cliente (ACTIVO, SUSPENDIDO, DADO_DE_BAJA).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCliente estado = EstadoCliente.ACTIVO;

    /**
     * Compatible con el campo 'activo' anterior (se mantiene por compatibilidad).
     */
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    // ==================== CUENTA CORRIENTE ====================

    /**
     * Saldo actual de la cuenta corriente del cliente.
     * Positivo = a favor del cliente (crédito)
     * Negativo = deuda del cliente
     */
    @Column(name = "saldo_cuenta_corriente", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal saldoCuentaCorriente = BigDecimal.ZERO;

    /**
     * Movimientos de cuenta corriente asociados a este cliente.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MovimientoCuentaCorriente> movimientos = new ArrayList<>();

    /**
     * Facturas emitidas a este cliente.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Factura> facturas = new ArrayList<>();

    /**
     * Pagos realizados por este cliente.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

    // ==================== Métodos de Negocio ====================

    /**
     * Sincroniza el campo 'activo' con el estado.
     */
    @PrePersist
    @PreUpdate
    protected void sincronizarEstado() {
        this.activo = (this.estado == EstadoCliente.ACTIVO);
    }

    /**
     * Activa el cliente.
     */
    public void activar() {
        this.estado = EstadoCliente.ACTIVO;
        this.activo = true;
    }

    /**
     * Suspende el cliente.
     */
    public void suspender() {
        this.estado = EstadoCliente.SUSPENDIDO;
        this.activo = false;
    }

    /**
     * Da de baja al cliente (baja lógica).
     */
    public void darDeBaja() {
        this.estado = EstadoCliente.DADO_DE_BAJA;
        this.activo = false;
    }

    /**
     * Verifica si el cliente puede ser facturado.
     */
    public boolean puedeSerFacturado() {
        return this.estado == EstadoCliente.ACTIVO;
    }

    /**
     * Registra un movimiento en la cuenta corriente y actualiza el saldo.
     * @param movimiento Movimiento a registrar
     */
    public void registrarMovimiento(MovimientoCuentaCorriente movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }
        
        movimiento.setCliente(this);
        this.movimientos.add(movimiento);
        
        // Actualizar saldo según el tipo de movimiento
        this.saldoCuentaCorriente = this.saldoCuentaCorriente.add(movimiento.calcularImpactoEnSaldo());
    }

    /**
     * Calcula el saldo actual sumando todos los movimientos.
     */
    public BigDecimal calcularSaldoActual() {
        return movimientos.stream()
            .map(MovimientoCuentaCorriente::calcularImpactoEnSaldo)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Recalcula y actualiza el saldo de la cuenta corriente.
     */
    public void recalcularSaldo() {
        this.saldoCuentaCorriente = calcularSaldoActual();
    }

    /**
     * Obtiene el nombre completo del cliente.
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Verifica si el cliente tiene deuda.
     */
    public boolean tieneDeuda() {
        return saldoCuentaCorriente.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Obtiene el monto de la deuda (valor absoluto si es negativo).
     */
    public BigDecimal getMontoDeuda() {
        return tieneDeuda() ? saldoCuentaCorriente.abs() : BigDecimal.ZERO;
    }
}