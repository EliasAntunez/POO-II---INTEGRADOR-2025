package com.example.facturacion.servicio;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.facturacion.modelo.Servicio;
import com.example.facturacion.repositorio.RepositorioServicio;

import jakarta.transaction.Transactional;

/**
 * Servicio para manejar la lógica de negocio relacionada con los servicios.
 */
@Service
public class ServicioServicio {
    
    @Autowired
    private RepositorioServicio repositorioServicio;
    
    /**
     * Guarda un nuevo servicio. Valida unicidad de nombre.
     * @param servicio El servicio a guardar.
     * @return El servicio guardado.
     */
    public Servicio guardarServicio(Servicio servicio) {
        if (repositorioServicio.existsByNombre(servicio.getNombre())) {
            throw new IllegalArgumentException("El nombre del servicio ya existe");
        }
        return repositorioServicio.save(servicio);
    }
    
    /**
     * Actualiza un servicio existente.
     * @param servicio El servicio a actualizar.
     * @return El servicio actualizado.
     */
    public Servicio actualizarServicio(Servicio servicio) {
        final long id = Objects.requireNonNull(servicio.getId(), "El ID del servicio es requerido para actualizar");
        if (!repositorioServicio.existsById(id)) {
            throw new IllegalArgumentException("El servicio con ID " + id + " no existe");
        }
        if (repositorioServicio.existsByNombreAndIdNot(servicio.getNombre(), id)) {
            throw new IllegalArgumentException("El nombre ya existe en otro servicio");
        }
        return repositorioServicio.save(servicio);
    }
    
    /**
     * Obtiene todos los servicios activos.
     * @return Lista de servicios activos.
     */
    public List<Servicio> obtenerTodosLosServicios() {
        return repositorioServicio.findByActivoTrue();
    }

    /**
     * Obtiene una página de TODOS los servicios (activos e inactivos).
     * @param page número de página (base 0)
     * @param size tamaño de página
     * @return página de servicios
     */
    public Page<Servicio> obtenerServiciosPaginados(int page, int size) {
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return repositorioServicio.findAll(pageable);  // Cambiado de findByActivoTrue a findAll
    }

    /**
     * Obtiene un servicio por su ID.
     * @param id El ID del servicio.
     * @return El servicio encontrado o null si no existe.
     */
    public Servicio obtenerServicioPorId(Long id) {
        final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
        return repositorioServicio.findById(safeId).orElse(null);
    }

    /**
     * Da de baja un servicio por su ID (establece activo = false).
     * @param id El ID del servicio a dar de baja.
     */
    @Transactional
    public void darDeBajaServicioPorId(Long id) {
        final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
        if(!repositorioServicio.existsById(safeId)){
            throw new IllegalArgumentException("El servicio con ID " + safeId + " no existe");
        }
        try{
            repositorioServicio.findById(safeId).ifPresent(servicio -> {
                servicio.setActivo(false);
                repositorioServicio.save(servicio);
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al dar de baja el servicio con ID " + safeId + ": " + ex.getMessage());
        }
    }
    
    /**
     * Reactiva un servicio por su ID (establece activo = true).
     * @param id El ID del servicio a reactivar.
     */
    @Transactional
    public void reactivarServicioPorId(Long id) {
        final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
        if(!repositorioServicio.existsById(safeId)){
            throw new IllegalArgumentException("El servicio con ID " + safeId + " no existe");
        }
        try{
            repositorioServicio.findById(safeId).ifPresent(servicio -> {
                servicio.setActivo(true);
                repositorioServicio.save(servicio);
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al reactivar el servicio con ID " + safeId + ": " + ex.getMessage());
        }
    }
    public List<Servicio> obtenerServiciosActivos() {
        return repositorioServicio.findByActivoTrue();
    }
}
