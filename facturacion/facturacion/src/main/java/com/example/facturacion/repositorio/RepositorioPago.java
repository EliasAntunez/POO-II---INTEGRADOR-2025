package com.example.facturacion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Pago;

@Repository
public interface RepositorioPago extends JpaRepository<Pago, Long> {
    List<Pago> findByFacturaId(Long facturaId);
    List<Pago> findByClienteId(Long clienteId);
}