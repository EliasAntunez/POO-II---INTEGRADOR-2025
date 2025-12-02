package com.example.facturacion.repositorio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.ClienteServicio;

@Repository
public interface RepositorioClienteServicio extends JpaRepository<ClienteServicio, Long> {

	List<ClienteServicio> findByActivoTrue();
    
    List<ClienteServicio> findByClienteAndActivoTrue(Cliente cliente);

	/**
	 * Obtiene una página de asignaciones cliente-servicio activas.
	 */
	org.springframework.data.domain.Page<ClienteServicio> findByActivoTrue(org.springframework.data.domain.Pageable pageable);

	Page<ClienteServicio> findByClienteIdAndActivoTrue(Long clienteId, Pageable pageable);

	boolean existsByClienteIdAndServicioIdAndActivoTrue(Long clienteId, Long servicioId);

	java.util.Optional<ClienteServicio> findByClienteIdAndServicioIdAndActivoTrue(Long clienteId, Long servicioId);

}
