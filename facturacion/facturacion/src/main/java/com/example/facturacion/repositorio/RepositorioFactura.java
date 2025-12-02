package com.example.facturacion.repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.enums.EstadoFactura;

@Repository
public interface RepositorioFactura extends JpaRepository<Factura, Long> {
    
    @Query("SELECT f FROM Factura f WHERE " +
           "(" +
               ":busqueda IS NULL OR :busqueda = '' OR " +
               "LOWER(f.cliente.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
               "LOWER(f.cliente.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
               "LOWER(f.cliente.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
               "f.cliente.cuit LIKE CONCAT('%', :busqueda, '%') OR " +
               "f.cliente.dni LIKE CONCAT('%', :busqueda, '%')" +
           ") " +
           "AND (:estado IS NULL OR f.estado = :estado) " +
           "AND (cast(:fechaDesde as timestamp) IS NULL OR f.fechaEmision >= :fechaDesde) " +
           "AND (cast(:fechaHasta as timestamp) IS NULL OR f.fechaEmision <= :fechaHasta)")
    Page<Factura> buscarConFiltros(
            @Param("busqueda") String busqueda,
            @Param("estado") EstadoFactura estado,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
    
    List<Factura> findByCliente(Cliente cliente);
    Page<Factura> findByCliente(Cliente cliente, Pageable pageable);
    Page<Factura> findAllByOrderByFechaEmisionDesc(Pageable pageable);
    
    /**
     * Verifica si existe una factura para un cliente en un período de fechas.
     * Usado para evitar doble facturación del mismo período.
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Factura f " +
           "WHERE f.cliente = :cliente " +
           "AND FUNCTION('DATE', f.fechaEmision) >= :inicio " +
           "AND FUNCTION('DATE', f.fechaEmision) <= :fin " +
           "AND f.anulada = false")
    boolean existsByClienteAndFechaEmisionBetween(
            @Param("cliente") Cliente cliente,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}