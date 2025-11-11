package com.example.facturacion.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    public boolean existsByNombre(String nombre);
    /**
     * Métodos útiles para exclusión por id en actualizaciones
     * @param nombre
     * @param id
     */
    public boolean existsByNombreAndIdNot(String nombre, Long id);
    public java.util.List<Servicio> findByActivoTrue();
    /**
     * Obtiene una página de servicios activos.
     * @param pageable información de paginación
     */
    public Page<Servicio> findByActivoTrue(Pageable pageable);


}
