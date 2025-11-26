package com.example.facturacion.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Pago;

/**
 * Repositorio para la entidad Pago.
 * HU-12: Registrar Pago Parcial
 */
@Repository
public interface RepositorioPago extends JpaRepository<Pago, Long> {
    
    /**
     * Obtiene todos los pagos de una factura.
     */
    List<Pago> findByFacturaId(Long facturaId);
    
    /**
     * Obtiene todos los pagos de una factura ordenados por fecha.
     */
    List<Pago> findByFacturaIdOrderByFechaPagoDesc(Long facturaId);
    
    /**
     * Obtiene pagos realizados en un rango de fechas.
     */
    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fechaPago DESC")
    List<Pago> findPagosEntreFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    /**
     * Obtiene pagos realizados por un usuario.
     */
    List<Pago> findByUsuario(String usuario);
    
    /**
     * Cuenta pagos de una factura.
     */
    long countByFacturaId(Long facturaId);
    
    /**
     * Verifica si una factura tiene pagos.
     */
    boolean existsByFacturaId(Long facturaId);
}