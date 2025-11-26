package com.example.facturacion.servicio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.NotaCredito;
import com.example.facturacion.modelo.Pago;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.repositorio.RepositorioFactura;
import com.example.facturacion.repositorio.RepositorioNotaCredito;
import com.example.facturacion.repositorio.RepositorioPago;

/**
 * Servicio para manejar la lógica de negocio relacionada con las facturas.
 * HU-11: Registrar Pago Total
 * HU-12: Registrar Pago Parcial
 * HU-09: Anulación con Nota de Crédito
 */
@Service
public class ServicioFactura {
    
    @Autowired
    private RepositorioFactura repositorioFactura;
    
    @Autowired
    private RepositorioPago repositorioPago;
    
    @Autowired
    private RepositorioNotaCredito repositorioNotaCredito;

    // ==================== HU-11: REGISTRAR PAGO TOTAL ====================
    
    /**
     * HU-11: Registra el pago total de una factura.
     *
     * Validaciones:
     * - La factura debe existir
     * - Debe estar en estado EMITIDA o PENDIENTE_PAGO
     * - No puede estar PAGADA o ANULADA
     *
     * @param facturaId ID de la factura a pagar
     * @param usuarioPago Usuario que registra el pago (opcional, puede ser "Sistema")
     * @return Factura actualizada
     * @throws IllegalArgumentException si la factura no existe o no puede pagarse
     */
    @Transactional
    public Factura registrarPagoTotal(Long facturaId, String usuarioPago) {
        // Validar parámetros
        Objects.requireNonNull(facturaId, "El ID de la factura no puede ser nulo");
        
        if (usuarioPago == null || usuarioPago.trim().isEmpty()) {
            usuarioPago = "Sistema"; // Usuario por defecto
        }
        
        // Buscar la factura con sus detalles
        Factura factura = repositorioFactura.findByIdWithDetalles(facturaId)
            .orElseThrow(() -> new IllegalArgumentException(
                "La factura con ID " + facturaId + " no existe"));
        
        // Validar que puede pagarse
        if (!factura.puedePagarse()) {
            throw new IllegalArgumentException(
                String.format("La factura %s no puede pagarse. Estado actual: %s",
                    factura.getNumeroFactura(),
                    factura.getEstado().getDescripcion()));
        }
        
        // Registrar el pago (el método de la entidad hace las validaciones)
        try {
            factura.registrarPagoTotal(usuarioPago);
            
            // Guardar en base de datos
            Factura facturaPagada = repositorioFactura.save(factura);
            
            // Log para auditoría
            System.out.println(String.format(
                "PAGO REGISTRADO - Factura: %s, Monto: $%.2f, Usuario: %s, Fecha: %s",
                facturaPagada.getNumeroFactura(),
                facturaPagada.getMontoTotal(),
                usuarioPago,
                facturaPagada.getFechaPago()
            ));
            
            return facturaPagada;
            
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("Error al registrar pago: " + ex.getMessage());
        }
    }

    // ==================== CONSULTAS ====================
    
    /**
     * HU-11: Obtiene todas las facturas pendientes de pago.
     * Retorna facturas en estado EMITIDA o PENDIENTE_PAGO.
     */
    @Transactional(readOnly = true)
    public List<Factura> obtenerFacturasPendientes() {
        return repositorioFactura.findFacturasPendientesPago();
    }
    
    /**
     * HU-11: Obtiene facturas pendientes con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Factura> obtenerFacturasPendientesPaginadas(int page, int size) {
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, Math.min(size, 100)); // Máximo 100 por página
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return repositorioFactura.findFacturasPendientesPago(pageable);
    }
    
    /**
     * Obtiene todas las facturas con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Factura> obtenerFacturasPaginadas(int page, int size) {
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage, safeSize,
            Sort.by(Sort.Direction.DESC, "fechaEmision"));
        return repositorioFactura.findAllByOrderByFechaEmisionDesc(pageable);
    }
    
    /**
     * Obtiene facturas por estado con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Factura> obtenerFacturasPorEstado(EstadoFactura estado, int page, int size) {
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage, safeSize,
            Sort.by(Sort.Direction.DESC, "fechaEmision"));
        return repositorioFactura.findByEstado(estado, pageable);
    }
    
    /**
     * HU-11: Obtiene una factura por ID con sus detalles.
     */
    @Transactional(readOnly = true)
    public Factura obtenerFacturaPorId(Long id) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        return repositorioFactura.findByIdWithDetalles(id).orElse(null);
    }
    
    /**
     * Obtiene una factura por su número.
     */
    @Transactional(readOnly = true)
    public Factura obtenerFacturaPorNumero(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de factura no puede estar vacío");
        }
        return repositorioFactura.findByNumeroFactura(numeroFactura).orElse(null);
    }
    
    /**
     * Obtiene todas las facturas de un cliente.
     */
    @Transactional(readOnly = true)
    public List<Factura> obtenerFacturasPorCliente(Long clienteId) {
        Objects.requireNonNull(clienteId, "El ID del cliente no puede ser nulo");
        return repositorioFactura.findByClienteId(clienteId);
    }
    
    /**
     * Obtiene facturas de un cliente con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Factura> obtenerFacturasPorClientePaginadas(Long clienteId, int page, int size) {
        Objects.requireNonNull(clienteId, "El ID del cliente no puede ser nulo");
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage, safeSize,
            Sort.by(Sort.Direction.DESC, "fechaEmision"));
        return repositorioFactura.findByClienteId(clienteId, pageable);
    }

    // ==================== HU-12: REGISTRAR PAGO PARCIAL ====================
    
/**
 * HU-12: Registra un pago parcial de una factura.
 *
 * Validaciones:
 * - La factura debe existir
 * - Debe poder recibir pagos (EMITIDA o PENDIENTE_PAGO)
 * - El monto debe ser > 0
 * - El monto no debe exceder el saldo pendiente
 * - Si el saldo llega a 0, cambia a PAGADA
 *
 * @param facturaId ID de la factura
 * @param montoPago Monto a pagar
 * @param usuario Usuario que registra el pago
 * @param observaciones Observaciones opcionales
 * @return El pago registrado
 */
@Transactional
public Pago registrarPagoParcial(Long facturaId, BigDecimal montoPago,
                                String usuario, String observaciones) {
    // Validar parámetros
    Objects.requireNonNull(facturaId, "El ID de la factura no puede ser nulo");
    Objects.requireNonNull(montoPago, "El monto no puede ser nulo");
    
    // Hacer la variable efectivamente final
    final String usuarioFinal = (usuario == null || usuario.trim().isEmpty())
        ? "Sistema"
        : usuario.trim();
    
    // Buscar la factura con detalles y pagos
    Factura factura = repositorioFactura.findByIdWithDetalles(facturaId)
        .orElseThrow(() -> new IllegalArgumentException(
            "La factura con ID " + facturaId + " no existe"));
    
    // Validar monto
    if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("El monto debe ser mayor a cero");
    }
    
    BigDecimal saldoPendiente = factura.calcularSaldoPendiente();
    
    if (montoPago.compareTo(saldoPendiente) > 0) {
        throw new IllegalArgumentException(
            String.format("El monto ($%,.2f) excede el saldo pendiente ($%,.2f)", 
                montoPago, saldoPendiente));
    }
    
    // Registrar el pago (el método de la entidad valida el estado)
    try {
        Pago pago = factura.registrarPagoParcial(montoPago, usuarioFinal, observaciones);
        pago.validar();
        
        // Guardar la factura (cascade guardará el pago también)
        Factura facturaActualizada = repositorioFactura.save(factura);
        
        // Obtener el pago guardado - usar variables finales en el stream
        final BigDecimal montoFinal = montoPago;
        Pago pagoGuardado = facturaActualizada.getPagos()
            .stream()
            .filter(p -> p.getMonto().equals(montoFinal) &&
                        p.getUsuario().equals(usuarioFinal))
            .reduce((first, second) -> second) // Obtener el último
            .orElse(pago);
        
        // Log para auditoría
        BigDecimal nuevoSaldo = facturaActualizada.calcularSaldoPendiente();
        System.out.println(String.format(
            "PAGO PARCIAL REGISTRADO - Factura: %s, Monto: $%.2f, Saldo Pendiente: $%.2f, Usuario: %s",
            facturaActualizada.getNumeroFactura(),
            montoPago,
            nuevoSaldo,
            usuarioFinal
        ));
        
        return pagoGuardado;
        
    } catch (IllegalStateException | IllegalArgumentException ex) {
        throw new IllegalArgumentException("Error al registrar pago parcial: " + ex.getMessage());
    }
}

    // ==================== HU-09: ANULACIÓN CON NOTA DE CRÉDITO ====================
    
    /**
     * HU-09: Anula una factura mediante nota de crédito.
     * 
     * Validaciones:
     * - Solo facturas en estado EMITIDA pueden anularse
     * - No se puede anular una factura ya anulada (doble anulación)
     * - No se puede anular una factura pagada
     * - Requiere motivo y usuario responsable
     * 
     * @param facturaId ID de la factura
     * @param motivo Motivo de la anulación (requerido)
     * @param usuarioResponsable Usuario responsable (requerido)
     * @return La factura anulada con su nota de crédito
     */
    @Transactional
    public Factura anularFacturaConNotaCredito(Long facturaId, String motivo, 
                                                String usuarioResponsable) {
        Objects.requireNonNull(facturaId, "El ID de la factura no puede ser nulo");
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de anulación es requerido");
        }
        
        if (usuarioResponsable == null || usuarioResponsable.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario responsable es requerido");
        }
        
        // Buscar la factura
        Factura factura = repositorioFactura.findById(facturaId)
            .orElseThrow(() -> new IllegalArgumentException(
                "La factura con ID " + facturaId + " no existe"));
        
        // Validar que no esté ya anulada
        if (factura.getNotaCredito() != null) {
            throw new IllegalArgumentException(
                "La factura ya tiene una nota de crédito asociada. No se puede aplicar doble anulación");
        }
        
        try {
            // Anular la factura (crea la nota de crédito internamente)
            factura.anular(motivo, usuarioResponsable);
            
            // Guardar (cascade guardará la nota de crédito)
            Factura facturaAnulada = repositorioFactura.save(factura);
            
            // Generar número de nota de crédito
            if (facturaAnulada.getNotaCredito() != null) {
                facturaAnulada.getNotaCredito().generarNumero();
                repositorioNotaCredito.save(facturaAnulada.getNotaCredito());
            }
            
            // Log
            System.out.println(String.format(
                "FACTURA ANULADA - Número: %s, NC: %s, Motivo: %s, Responsable: %s",
                facturaAnulada.getNumeroFactura(),
                facturaAnulada.getNotaCredito().getNumero(),
                motivo,
                usuarioResponsable
            ));
            
            return facturaAnulada;
            
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("Error al anular factura: " + ex.getMessage());
        }
    }
    
    /**
     * Guarda una factura.
     */
    @Transactional
    public Factura guardarFactura(Factura factura) {
        Objects.requireNonNull(factura, "La factura no puede ser nula");
        
        // Calcular totales antes de guardar
        factura.calcularTotales();
        
        // Guardar
        Factura facturaGuardada = repositorioFactura.save(factura);
        
        // Generar número de factura si no existe
        if (facturaGuardada.getNumeroFactura() == null) {
            facturaGuardada.generarNumeroFactura();
            facturaGuardada = repositorioFactura.save(facturaGuardada);
        }
        
        return facturaGuardada;
    }
    
    /**
     * Anula una factura (solo si no está pagada).
     * DEPRECADO: Usar anularFacturaConNotaCredito() para cumplir HU-09
     */
    @Deprecated
    @Transactional
    public Factura anularFactura(Long facturaId) {
        throw new UnsupportedOperationException(
            "Método deprecado. Use anularFacturaConNotaCredito() que requiere motivo y usuario responsable según HU-09");
    }
    
    // ==================== CONSULTAS DE PAGOS ====================
    
    /**
     * HU-12: Obtiene el historial de pagos de una factura.
     */
    @Transactional(readOnly = true)
    public List<Pago> obtenerPagosDeFactura(Long facturaId) {
        Objects.requireNonNull(facturaId, "El ID de la factura no puede ser nulo");
        return repositorioPago.findByFacturaIdOrderByFechaPagoDesc(facturaId);
    }
    
    /**
     * HU-12: Obtiene el saldo pendiente de una factura.
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldoPendiente(Long facturaId) {
        Factura factura = obtenerFacturaPorId(facturaId);
        if (factura == null) {
            throw new IllegalArgumentException("Factura no encontrada");
        }
        return factura.calcularSaldoPendiente();
    }
    
    /**
     * HU-09: Obtiene la nota de crédito de una factura.
     */
    @Transactional(readOnly = true)
    public NotaCredito obtenerNotaCreditoDeFactura(Long facturaId) {
        Objects.requireNonNull(facturaId, "El ID de la factura no puede ser nulo");
        return repositorioNotaCredito.findByFacturaId(facturaId).orElse(null);
    }
    
    // ==================== OPERACIONES ADICIONALES ====================
    @Transactional(readOnly = true)
    public boolean existeFactura(Long id) {
        return id != null && repositorioFactura.existsById(id);
    }
    
    /**
     * Cuenta facturas por estado.
     */
    @Transactional(readOnly = true)
    public long contarFacturasPorEstado(EstadoFactura estado) {
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        return repositorioFactura.countByEstado(estado);
    }
}