package com.example.facturacion.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.facturacion.servicio.ServicioCliente;
import com.example.facturacion.servicio.ServicioServicio;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.facturacion.servicio.ServicioClienteServicio;
import com.example.facturacion.modelo.ClienteServicio;

import java.util.List;

import org.springframework.ui.Model;


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
    public String listarClientesServicios(Model model) {
        List<ClienteServicio> clientesServicios = servicioClienteServicio.obtenerTodosLosClientesServiciosActivos();
        model.addAttribute("clientesServicios", clientesServicios);
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