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
    /**
     * Repositorio para acceder a los datos de los servicios.
     */
    @Autowired
    private RepositorioServicio repositorioServicio;
        /**
     * Guarda un nuevo servicio. Valida unicidad de nombre.
     * @param servicio El servicio a guardar.
     * @return El servicio guardado.
     */
    public Servicio guardarServicio(Servicio servicio) {
        //verificar nombre unico podria:
        if (repositorioServicio.existsByNombre(servicio.getNombre())) {
            throw new IllegalArgumentException("El nombre del servicio ya existe");
        }
        return repositorioServicio.save(servicio);
    }
    /**
     * Actualiza un cliente existente.
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
     * Obtiene todos los clientes activos.
     * @return Lista de clientes activos.
    */
    public List<Servicio> obtenerTodosLosServicios() {
        return repositorioServicio.findByActivoTrue();
    }

    /**
     * Obtiene una página de servicios activos.
     * @param page número de página (base 0)
     * @param size tamaño de página
     * @return página de servicios
     */
    public Page<Servicio> obtenerServiciosPaginados(int page, int size) {
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return repositorioServicio.findByActivoTrue(pageable);
    }

    /**
     * Obtiene un cliente por su ID.
     * @param id El ID del cliente.
     * @return El cliente encontrado o null si no existe.
    */
    public Servicio obtenerServicioPorId(Long id) {
        final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
        return repositorioServicio.findById(safeId).orElse(null);
    }

    /**
     * Da de baja un servicio por su ID (establece activo = false).
     * @param id El ID del servicio a dar de baja.
     * @Transactional establecido para asegurar la integridad de la operación.
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
}
