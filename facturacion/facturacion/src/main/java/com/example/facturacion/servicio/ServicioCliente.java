package com.example.facturacion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.repositorio.RepositorioCliente;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
    * Servicio para manejar la lógica de negocio relacionada con los clientes.
 */
@Service
public class ServicioCliente {
    /**
     * Repositorio para acceder a los datos de los clientes.
     */
    @Autowired
    private RepositorioCliente repositorioCliente;
    /**
     * Guarda un nuevo cliente. Valida unicidad de DNI y CUIT.
     * @param cliente El cliente a guardar.
     * @return El cliente guardado.
     */
    public Cliente guardarCliente(Cliente cliente) {
        //verificar dni unico podria:
        if (repositorioCliente.existsByDni(cliente.getDni())) {
            throw new IllegalArgumentException("El DNI ya existe");
        }
        //verificar CUIT unico podria:
        if (repositorioCliente.existsByCuit(cliente.getCuit())) {
            throw new IllegalArgumentException("El CUIT ya existe");
        }
        return repositorioCliente.save(cliente);
    }

    /**
     * Actualiza un cliente existente.
     * @param cliente El cliente a actualizar.
     * @return El cliente actualizado.
     */
    public Cliente actualizarCliente(Cliente cliente) {
        // asegurar que el id no sea nulo y usar una variable local no-nula para satisfacer
        // las comprobaciones de null-safety del IDE
        final Long id = Objects.requireNonNull(cliente.getId(), "El ID del cliente es requerido para actualizar");
        // verificar que exista
        if (!repositorioCliente.existsById(id)) {
            throw new IllegalArgumentException("El cliente con ID " + id + " no existe");
        }
        // validar unicidad excluyendo el id actual
        if (repositorioCliente.existsByDniAndIdNot(cliente.getDni(), id)) {
            throw new IllegalArgumentException("El DNI ya existe en otro cliente");
        }
        if (repositorioCliente.existsByCuitAndIdNot(cliente.getCuit(), id)) {
            throw new IllegalArgumentException("El CUIT ya existe en otro cliente");
        }
        if (repositorioCliente.existsByEmailAndIdNot(cliente.getEmail(), id)) {
            throw new IllegalArgumentException("El Email ya existe en otro cliente");
        }
        return repositorioCliente.save(cliente);
    }

    /**
     * Obtiene todos los clientes activos.
     * @return Lista de clientes activos.
    */
    public List<Cliente> obtenerTodosLosClientes() {
        return repositorioCliente.findByActivoTrue();
    }

    /**
     * Obtiene una página de clientes activos.
     * @param page número de página (base 0)
     * @param size tamaño de página
     * @return página de clientes
     */
    public Page<Cliente> obtenerClientesPaginados(int page, int size) {
        final int safePage = Math.max(0, page);
        final int safeSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return repositorioCliente.findByActivoTrue(pageable);
    }

    /**
     * Obtiene un cliente por su ID.
     * @param id El ID del cliente.
     * @return El cliente encontrado o null si no existe.
    */
    public Cliente obtenerClientePorId(Long id) {
        final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
        return repositorioCliente.findById(safeId).orElse(null);
    }

    /**
     * Da de baja un cliente por su ID (establece activo = false).
     * @param id El ID del cliente a dar de baja.
     * @Transactional establecido para asegurar la integridad de la operación.
    */
    @Transactional
    public void darDeBajaClientePorId(Long id) {
        final Long safeId = Objects.requireNonNull(id, "El ID no puede ser nulo");
        if(!repositorioCliente.existsById(safeId)){
            throw new IllegalArgumentException("El cliente con ID " + safeId + " no existe");
        }
        try{
            repositorioCliente.findById(safeId).ifPresent(cliente -> {
                cliente.setActivo(false);
                repositorioCliente.save(cliente);
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al dar de baja el cliente con ID " + safeId + ": " + ex.getMessage());
        }
        
    }

    //OBTENER TODOS LOS CLIENTES ACTIVOS
    public List<Cliente> obtenerClientesActivos() {
        return repositorioCliente.findByActivoTrue();
    }
}