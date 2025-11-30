package com.example.facturacion.repositorio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.enums.EstadoCliente;

/**
 * Repositorio para la entidad Cliente.
 * Proporciona métodos para operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {
    
    // ==================== Validaciones de unicidad ====================
    
    /**
     * Verifica si existe un cliente con el DNI dado.
     */
    boolean existsByDni(String dni);
    
    /**
     * Verifica si existe un cliente con el CUIT dado.
     */
    boolean existsByCuit(String cuit);
    
    /**
     * Verifica si existe un cliente con el email dado.
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un cliente con el teléfono dado.
     */
    boolean existsByTelefono(String telefono);
    
    // ==================== Métodos para actualización (exclusión por ID) ====================
    
    /**
     * Verifica si existe un cliente con el DNI dado y un ID diferente.
     */
    boolean existsByDniAndIdNot(String dni, Long id);
    
    /**
     * Verifica si existe un cliente con el CUIT dado y un ID diferente.
     */
    boolean existsByCuitAndIdNot(String cuit, Long id);
    
    /**
     * Verifica si existe un cliente con el email dado y un ID diferente.
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * Verifica si existe un cliente con el teléfono dado y un ID diferente.
     */
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
    
    // ==================== Consultas por estado ====================
    
    /**
     * Obtiene todos los clientes activos (campo legacy 'activo').
     */
    List<Cliente> findByActivoTrue();
    
    /**
     * Obtiene una página de clientes activos.
     */
    Page<Cliente> findByActivoTrue(Pageable pageable);
    
    /**
     * Obtiene todos los clientes por estado.
     */
    List<Cliente> findByEstado(EstadoCliente estado);
    
    /**
     * Obtiene una página de clientes por estado.
     */
    Page<Cliente> findByEstado(EstadoCliente estado, Pageable pageable);
    
    /**
     * Obtiene todos los clientes activos (usando enum EstadoCliente).
     */
    @Query("SELECT c FROM Cliente c WHERE c.estado = com.example.facturacion.modelo.enums.EstadoCliente.ACTIVO")
    List<Cliente> findClientesActivos();
    
    /**
     * Obtiene clientes con deuda (saldo negativo).
     */
    @Query("SELECT c FROM Cliente c WHERE c.saldoCuentaCorriente < 0")
    List<Cliente> findClientesConDeuda();
    
    /**
     * Obtiene clientes con saldo a favor (saldo positivo).
     */
    @Query("SELECT c FROM Cliente c WHERE c.saldoCuentaCorriente > 0")
    List<Cliente> findClientesConSaldoAFavor();
    
    /**
     * Busca clientes por nombre, apellido, DNI o CUIT (búsqueda flexible).
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "c.dni LIKE CONCAT('%', :busqueda, '%') OR " +
           "c.cuit LIKE CONCAT('%', :busqueda, '%')")
    Page<Cliente> buscarClientes(@Param("busqueda") String busqueda, Pageable pageable);
}