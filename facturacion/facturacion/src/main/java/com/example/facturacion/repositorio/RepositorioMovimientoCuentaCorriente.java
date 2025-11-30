package com.example.facturacion.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.MovimientoCuentaCorriente;
import com.example.facturacion.modelo.enums.TipoMovimiento;

/**
 * Repositorio para MovimientoCuentaCorriente.
 */
@Repository
public interface RepositorioMovimientoCuentaCorriente 
        extends JpaRepository<MovimientoCuentaCorriente, Long> {
    
    /**
     * Obtiene todos los movimientos de un cliente.
     */
    List<MovimientoCuentaCorriente> findByClienteOrderByFechaMovimientoDesc(Cliente cliente);
    
    /**
     * Obtiene movimientos de un cliente paginados.
     */
    Page<MovimientoCuentaCorriente> findByCliente(Cliente cliente, Pageable pageable);
    
    /**
     * Obtiene movimientos por tipo.
     */
    List<MovimientoCuentaCorriente> findByClienteAndTipoMovimiento(
        Cliente cliente, TipoMovimiento tipo);
    
    /**
     * Obtiene movimientos en un rango de fechas.
     */
    @Query("SELECT m FROM MovimientoCuentaCorriente m " +
           "WHERE m.cliente = :cliente " +
           "AND m.fechaMovimiento BETWEEN :fechaDesde AND :fechaHasta " +
           "ORDER BY m.fechaMovimiento DESC")
    List<MovimientoCuentaCorriente> findMovimientosPorRangoFechas(
        @Param("cliente") Cliente cliente,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta);
}