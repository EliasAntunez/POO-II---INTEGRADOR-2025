package com.example.facturacion.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.facturacion.modelo.Servicio;

/**
 * Repositorio para la entidad Servicio
 * Proporciona métodos para operaciones CRUD y consultas personalizadas.
*/
@Repository
public interface RepositorioServicio extends JpaRepository<Servicio, Long> {
    /**
     * Verifica si existe un servicio con el nombre dado.
     * @param nombre
     */
    boolean existsByNombre(String nombre);
    
    /**
     * Métodos útiles para exclusión por id en actualizaciones
     * @param nombre
     * @param id
     */
    boolean existsByNombreAndIdNot(String nombre, Long id);
    
    /**
     * Obtiene todos los servicios activos.
     */
    java.util.List<Servicio> findByActivoTrue();
    
    /**
     * Obtiene una página de servicios activos.
     * @param pageable información de paginación
     */
    Page<Servicio> findByActivoTrue(Pageable pageable);
    
    /**
     * Obtiene una página de TODOS los servicios (activos e inactivos).
     * @param pageable información de paginación
     */
    Page<Servicio> findAll(Pageable pageable);

    @Query("SELECT s FROM Servicio s WHERE " +
           "(:activo IS NULL OR s.activo = :activo) AND " +
           "(:nombre IS NULL OR LOWER(s.nombre) LIKE :nombre)")
    Page<Servicio> buscarServicios(@Param("activo") Boolean activo, @Param("nombre") String nombre, Pageable pageable);
}