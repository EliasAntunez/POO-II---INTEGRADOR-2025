package com.example.facturacion.repositorio;

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
    List<Factura> findByCliente(Cliente cliente);
    Page<Factura> findByCliente(Cliente cliente, Pageable pageable);
    Page<Factura> findAllByOrderByFechaEmisionDesc(Pageable pageable);

    @Query("SELECT f FROM Factura f WHERE " +
           "(:busqueda IS NULL OR LOWER(f.cliente.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "f.cliente.cuit LIKE CONCAT('%', :busqueda, '%') OR " +
           "f.cliente.nombre LIKE CONCAT('%', :busqueda, '%')) AND " +
           "(:estado IS NULL OR f.estado = :estado)")
    Page<Factura> buscarConFiltrosTexto(
            @Param("busqueda") String busqueda,
            @Param("estado") EstadoFactura estado,
            Pageable pageable
    );
}