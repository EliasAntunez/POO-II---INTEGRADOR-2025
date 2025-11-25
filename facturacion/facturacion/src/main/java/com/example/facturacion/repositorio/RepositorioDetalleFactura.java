package com.example.facturacion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.DetalleFactura;

/**
 * Repositorio para la entidad DetalleFactura.
 */
@Repository
public interface RepositorioDetalleFactura extends JpaRepository<DetalleFactura, Long> {
    
    /**
     * Obtiene todos los detalles de una factura.
     */
    List<DetalleFactura> findByFacturaId(Long facturaId);
    
    /**
     * Obtiene todos los detalles que incluyen un servicio específico.
     */
    List<DetalleFactura> findByServicioId(Long servicioId);
    
    /**
     * Cuenta cuántas veces se ha facturado un servicio.
     */
    long countByServicioId(Long servicioId);
    
    /**
     * Verifica si un servicio ha sido facturado.
     */
    boolean existsByServicioId(Long servicioId);
    
    /**
     * Obtiene detalles de facturas de un cliente que incluyen un servicio.
     */
    @Query("SELECT d FROM DetalleFactura d WHERE d.factura.cliente.id = :clienteId AND d.servicio.id = :servicioId")
    List<DetalleFactura> findByClienteIdAndServicioId(
        @Param("clienteId") Long clienteId,
        @Param("servicioId") Long servicioId
    );
}