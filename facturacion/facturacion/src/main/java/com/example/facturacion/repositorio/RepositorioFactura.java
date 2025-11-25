package com.example.facturacion.repositorio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.enums.EstadoFactura;

/**
 * Repositorio para la entidad Factura.
 * HU-11: Registrar Pago Total
 */
@Repository
public interface RepositorioFactura extends JpaRepository<Factura, Long> {
    
    /**
     * Busca una factura por su número.
     */
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    
    /**
     * Verifica si existe una factura con el número dado.
     */
    boolean existsByNumeroFactura(String numeroFactura);
    
    /**
     * Obtiene todas las facturas de un cliente.
     */
    List<Factura> findByClienteId(Long clienteId);
    
    /**
     * Obtiene todas las facturas de un cliente con paginación.
     */
    Page<Factura> findByClienteId(Long clienteId, Pageable pageable);
    
    /**
     * HU-11: Obtiene facturas por estado.
     */
    List<Factura> findByEstado(EstadoFactura estado);
    
    /**
     * HU-11: Obtiene facturas por estado con paginación.
     */
    Page<Factura> findByEstado(EstadoFactura estado, Pageable pageable);
    
    /**
     * HU-11: Obtiene facturas pendientes de pago (EMITIDA o PENDIENTE_PAGO).
     */
    @Query("SELECT f FROM Factura f WHERE f.estado IN (com.example.facturacion.modelo.enums.EstadoFactura.EMITIDA, com.example.facturacion.modelo.enums.EstadoFactura.PENDIENTE_PAGO) ORDER BY f.fechaEmision DESC")
    List<Factura> findFacturasPendientesPago();
    
    /**
     * HU-11: Obtiene facturas pendientes de pago con paginación.
     */
    @Query("SELECT f FROM Factura f WHERE f.estado IN (com.example.facturacion.modelo.enums.EstadoFactura.EMITIDA, com.example.facturacion.modelo.enums.EstadoFactura.PENDIENTE_PAGO) ORDER BY f.fechaEmision DESC")
    Page<Factura> findFacturasPendientesPago(Pageable pageable);
    
    /**
     * Obtiene facturas de un cliente por estado.
     */
    List<Factura> findByClienteIdAndEstado(Long clienteId, EstadoFactura estado);
    
    /**
     * Obtiene facturas pagadas en un rango de fechas.
     */
    @Query("SELECT f FROM Factura f WHERE f.estado = com.example.facturacion.modelo.enums.EstadoFactura.PAGADA AND f.fechaPago BETWEEN :fechaInicio AND :fechaFin ORDER BY f.fechaPago DESC")
    List<Factura> findFacturasPagadasEntreFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    /**
     * Obtiene facturas emitidas en un rango de fechas.
     */
    @Query("SELECT f FROM Factura f WHERE f.fechaEmision BETWEEN :fechaInicio AND :fechaFin ORDER BY f.fechaEmision DESC")
    List<Factura> findFacturasEmitidasEntreFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    /**
     * Cuenta facturas por estado.
     */
    long countByEstado(EstadoFactura estado);
    
    /**
     * HU-11: Obtiene facturas con detalles cargados (evita N+1).
     */
    @Query("SELECT DISTINCT f FROM Factura f LEFT JOIN FETCH f.detalles LEFT JOIN FETCH f.cliente WHERE f.id = :id")
    Optional<Factura> findByIdWithDetalles(@Param("id") Long id);
    
    /**
     * Obtiene todas las facturas ordenadas por fecha de emisión descendente.
     */
    Page<Factura> findAllByOrderByFechaEmisionDesc(Pageable pageable);
}