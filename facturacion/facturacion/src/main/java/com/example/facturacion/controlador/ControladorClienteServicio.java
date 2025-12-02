package com.example.facturacion.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.facturacion.modelo.ClienteServicio;
import com.example.facturacion.servicio.ServicioCliente;
import com.example.facturacion.servicio.ServicioClienteServicio;
import com.example.facturacion.servicio.ServicioServicio;


@Controller
@RequestMapping("/clientes-servicios")
public class ControladorClienteServicio {

    @Autowired
    private ServicioClienteServicio servicioClienteServicio;
    @Autowired
    private ServicioCliente servicioCliente;
    @Autowired
    private ServicioServicio servicioServicio;

    @GetMapping({"/", "/listar"})
    public String listarClientesServicios(Model model,
                                          @org.springframework.web.bind.annotation.RequestParam(value = "activo", required = false) Boolean activo,
                                          @org.springframework.web.bind.annotation.RequestParam(value = "cliente", required = false) String cliente,
                                          @org.springframework.web.bind.annotation.RequestParam(value = "servicio", required = false) String servicio,
                                          @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
                                          @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "10") int size) {
        // Default to active only if no filter is provided? 
        // The previous implementation was findByActivoTrue.
        // If the user wants to see all, they can select "Todos" in the filter (which sends null).
        // But if I send null to the repo, it returns all.
        // If I want to maintain the default behavior of showing only active ones when first loading the page:
        if (activo == null && cliente == null && servicio == null) {
             activo = true;
        }
        
        var clientesServiciosPage = servicioClienteServicio.buscarClientesServicios(activo, cliente, servicio, page, size);
        model.addAttribute("clientesServiciosPage", clientesServiciosPage);
        model.addAttribute("clientesServicios", clientesServiciosPage.getContent());
        model.addAttribute("activo", activo);
        model.addAttribute("cliente", cliente);
        model.addAttribute("servicio", servicio);
        return "clientes-servicios/listar";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("clienteServicio", new ClienteServicio());
        model.addAttribute("clientes", servicioCliente.obtenerClientesActivos());
        model.addAttribute("servicios", servicioServicio.obtenerServiciosActivos());
        return "clientes-servicios/crear";
    }

    @PostMapping("/crear")
    public String crearClienteServicio(@ModelAttribute("clienteServicio") ClienteServicio clienteServicio,
                                       @org.springframework.web.bind.annotation.RequestParam(required = false) Long clienteId,
                                       @org.springframework.web.bind.annotation.RequestParam(required = false) Long servicioId,
                                       Model model,
                                       org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            if (clienteId == null || servicioId == null) {
                model.addAttribute("clientes", servicioCliente.obtenerClientesActivos());
                model.addAttribute("servicios", servicioServicio.obtenerServiciosActivos());
                model.addAttribute("error", "Debe seleccionar cliente y servicio");
                return "clientes-servicios/crear";
            }

            var cliente = servicioCliente.obtenerClientePorId(clienteId);
            var servicio = servicioServicio.obtenerServicioPorId(servicioId);
            if (cliente == null || servicio == null) {
                model.addAttribute("clientes", servicioCliente.obtenerClientesActivos());
                model.addAttribute("servicios", servicioServicio.obtenerServiciosActivos());
                model.addAttribute("error", "Cliente o Servicio no encontrado");
                return "clientes-servicios/crear";
            }

            ClienteServicio nuevoClienteServicio = ClienteServicio.asignarServicioACliente(cliente, servicio);
            if (clienteServicio != null) nuevoClienteServicio.setActivo(clienteServicio.isActivo());
            servicioClienteServicio.guardarClienteServicio(nuevoClienteServicio);
            ra.addFlashAttribute("success", "Asignación creada");
            return "redirect:/clientes-servicios/listar";
        } catch (IllegalArgumentException e) {
            model.addAttribute("clientes", servicioCliente.obtenerClientesActivos());
            model.addAttribute("servicios", servicioServicio.obtenerServiciosActivos());
            model.addAttribute("error", e.getMessage());
            return "clientes-servicios/crear";
        }
    }

    @GetMapping("/modificar")
    public String mostrarFormularioModificar(@org.springframework.web.bind.annotation.RequestParam("id") Long id, Model model,
                                            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        ClienteServicio clienteServicio = servicioClienteServicio.obtenerClienteServicioPorId(id);
        if (clienteServicio == null) {
            ra.addFlashAttribute("error", "Asignación con ID " + id + " no encontrada");
            return "redirect:/clientes-servicios/listar";
        }
        model.addAttribute("clienteServicio", clienteServicio);
        model.addAttribute("clientes", servicioCliente.obtenerClientesActivos());
        model.addAttribute("servicios", servicioServicio.obtenerServiciosActivos());
        return "clientes-servicios/modificar";
    }

    @PostMapping("/modificar")
    public String modificarClienteServicio(@ModelAttribute("clienteServicio") ClienteServicio clienteServicio,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) Long clienteId,
                                           @org.springframework.web.bind.annotation.RequestParam(required = false) Long servicioId,
                                           Model model,
                                           org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            servicioClienteServicio.guardarClienteServicio(clienteServicio, clienteId, servicioId);
            ra.addFlashAttribute("success", "Asignación modificada");
            return "redirect:/clientes-servicios/listar";
        } catch (IllegalArgumentException e) {
            model.addAttribute("clientes", servicioCliente.obtenerClientesActivos());
            model.addAttribute("servicios", servicioServicio.obtenerServiciosActivos());
            model.addAttribute("error", e.getMessage());
            return "clientes-servicios/modificar";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarClienteServicio(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                                           org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            servicioClienteServicio.eliminarClienteServicio(id);
            ra.addFlashAttribute("success", "Asignación eliminada");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes-servicios/listar";
    }
}