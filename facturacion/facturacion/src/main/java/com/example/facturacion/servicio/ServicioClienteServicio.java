package com.example.facturacion.servicio;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.ClienteServicio;
import com.example.facturacion.modelo.Servicio;
import com.example.facturacion.repositorio.RepositorioClienteServicio;

@Service
public class ServicioClienteServicio {

    @Autowired
    private RepositorioClienteServicio repositorioClienteServicio;

    @Autowired
    private ServicioCliente servicioCliente;

    @Autowired
    private ServicioServicio servicioServicio;

    public ClienteServicio obtenerClienteServicioPorId(Long id) {
        return repositorioClienteServicio.findById(id).orElse(null);
    }

    // Obtener todos los clientes-servicios activos
    public List<ClienteServicio> obtenerTodosLosClientesServiciosActivos() {
        return repositorioClienteServicio.findByActivoTrue();
    }

    // Guardar un nuevo cliente-servicio: resolver referencias por id y validar duplicados
    public ClienteServicio guardarClienteServicio(ClienteServicio clienteServicio) {
        return guardarClienteServicio(clienteServicio, null, null);
    }

    public ClienteServicio guardarClienteServicio(ClienteServicio clienteServicio, Long clienteIdParam, Long servicioIdParam) {
        Objects.requireNonNull(clienteServicio, "clienteServicio requerido");

        Long clienteId = clienteIdParam != null ? clienteIdParam : (clienteServicio.getCliente() != null ? clienteServicio.getCliente().getId() : null);
        Long servicioId = servicioIdParam != null ? servicioIdParam : (clienteServicio.getServicio() != null ? clienteServicio.getServicio().getId() : null);
        if (clienteId == null || servicioId == null) {
            throw new IllegalArgumentException("Cliente y Servicio deben estar seleccionados");
        }

        Cliente cliente = servicioCliente.obtenerClientePorId(clienteId);
        Servicio servicio = servicioServicio.obtenerServicioPorId(servicioId);
        if (cliente == null) throw new IllegalArgumentException("Cliente no encontrado");
        if (servicio == null) throw new IllegalArgumentException("Servicio no encontrado");

        Long id = clienteServicio.getId();
        if (id == null) {
            // create
            if (repositorioClienteServicio.existsByClienteIdAndServicioIdAndActivoTrue(clienteId, servicioId)) {
                throw new IllegalArgumentException("Ya existe una asignación activa para ese cliente y servicio");
            }
            ClienteServicio cs = ClienteServicio.asignarServicioACliente(cliente, servicio);
            cs.setActivo(clienteServicio.isActivo());
            cs.ensureDefaults();
            return repositorioClienteServicio.save(cs);
        } else {
            // update
            ClienteServicio existente = repositorioClienteServicio.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));

            // if changing cliente/servicio, ensure no other active assignment exists
            java.util.Optional<ClienteServicio> conflicto = repositorioClienteServicio.findByClienteIdAndServicioIdAndActivoTrue(clienteId, servicioId);
            if (conflicto.isPresent() && !conflicto.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe una asignación activa para ese cliente y servicio");
            }

            existente.setCliente(cliente);
            existente.setServicio(servicio);
            if (clienteServicio.getFechaAsignacion() != null) existente.setFechaAsignacion(clienteServicio.getFechaAsignacion());
            existente.setActivo(clienteServicio.isActivo());
            return repositorioClienteServicio.save(existente);
        }
    }

    public void eliminarClienteServicio(Long id) {
        ClienteServicio clienteServicio = obtenerClienteServicioPorId(id);
        if (clienteServicio != null) {
            clienteServicio.desactivar();
            repositorioClienteServicio.save(clienteServicio);
        } else {
            throw new IllegalArgumentException("Asignación con ID " + id + " no encontrada");
        }
    }

}
