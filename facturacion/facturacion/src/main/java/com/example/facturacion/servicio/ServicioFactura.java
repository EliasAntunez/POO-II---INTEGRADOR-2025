package com.example.facturacion.servicio;

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
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.repositorio.RepositorioFactura;

/**
 * Servicio para manejar la lógica de negocio relacionada con las facturas.
 * HU-11: Registrar Pago Total
 */
@Service
public class ServicioFactura {
    
    @Autowired
    private RepositorioFactura repositorioFactura;

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

    // ==================== OPERACIONES ADICIONALES ====================
    
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
     */
    @Transactional
    public Factura anularFactura(Long facturaId) {
        Objects.requireNonNull(facturaId, "El ID de la factura no puede ser nulo");
        
        Factura factura = repositorioFactura.findById(facturaId)
            .orElseThrow(() -> new IllegalArgumentException(
                "La factura con ID " + facturaId + " no existe"));
        
        try {
            factura.anular();
            return repositorioFactura.save(factura);
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("Error al anular factura: " + ex.getMessage());
        }
    }
    
    /**
     * Verifica si una factura existe.
     */
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