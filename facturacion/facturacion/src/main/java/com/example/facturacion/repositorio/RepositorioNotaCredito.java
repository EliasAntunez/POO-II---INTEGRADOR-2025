package com.example.facturacion.repositorio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.NotaCredito;

/**
 * Repositorio para la entidad NotaCredito.
 * HU-09: Anulación de Facturas
 */
@Repository
public interface RepositorioNotaCredito extends JpaRepository<NotaCredito, Long> {
    
    /**
     * Busca nota de crédito por número.
     */
    Optional<NotaCredito> findByNumero(String numero);
    
    /**
     * Verifica si existe una nota de crédito con el número dado.
     */
    boolean existsByNumero(String numero);
    
    /**
     * Obtiene la nota de crédito de una factura.
     */
    Optional<NotaCredito> findByFacturaId(Long facturaId);
    
    /**
     * Verifica si una factura tiene nota de crédito.
     */
    boolean existsByFacturaId(Long facturaId);
    
    /**
     * Obtiene notas de crédito emitidas en un rango de fechas.
     */
    @Query("SELECT nc FROM NotaCredito nc WHERE nc.fechaEmision BETWEEN :fechaInicio AND :fechaFin ORDER BY nc.fechaEmision DESC")
    List<NotaCredito> findNotasCreditoEntreFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    /**
     * Obtiene notas de crédito por usuario responsable.
     */
    List<NotaCredito> findByUsuarioResponsable(String usuarioResponsable);
}