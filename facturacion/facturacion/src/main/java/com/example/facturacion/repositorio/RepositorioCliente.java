package com.example.facturacion.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Cliente;

/**
 * Repositorio para la entidad Cliente
 * Proporciona métodos para operaciones CRUD y consultas personalizadas.
*/
@Repository
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {
    /**
     * Verifica si existe un cliente con el DNI dado.
     * @param dni
     */
    public boolean existsByDni(String dni);
    /**
     * Verifica si existe un cliente con el CUIT dado.
     * @param cuit
     */
    public boolean existsByCuit(String cuit);
    /**
     * Verifica si existe un cliente con el email dado.
     * @param email
     */
    public boolean existsByEmail(String email);
    /**
     * Obtiene todos los clientes activos.
     */
    public java.util.List<Cliente> findByActivoTrue();

    /**
     * Obtiene una página de clientes activos.
     * @param pageable información de paginación
     */
    public Page<Cliente> findByActivoTrue(Pageable pageable);

    /**
     * Métodos útiles para exclusión por id en actualizaciones
     * @param dni
     * @param id
     */
    public boolean existsByDniAndIdNot(String dni, Long id);
    /**
     * Verifica si existe un cliente con el CUIT dado y un ID diferente.
     * @param cuit
     * @param id
     */
    public boolean existsByCuitAndIdNot(String cuit, Long id);
    /**
     * Verifica si existe un cliente con el email dado y un ID diferente.
     * @param email
     * @param id
     */
    public boolean existsByEmailAndIdNot(String email, Long id);

}