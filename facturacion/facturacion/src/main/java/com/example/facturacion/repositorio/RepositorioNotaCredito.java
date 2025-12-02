package com.example.facturacion.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.NotaCredito;

@Repository
public interface RepositorioNotaCredito extends JpaRepository<NotaCredito, Long> {
    
    // Buscar la NC asociada a una factura (para el botón "Ver Nota de Crédito")
    Optional<NotaCredito> findByFactura(Factura factura);

    // Buscar por ID de factura directamente (más robusto)
    Optional<NotaCredito> findByFacturaId(Long facturaId);
}