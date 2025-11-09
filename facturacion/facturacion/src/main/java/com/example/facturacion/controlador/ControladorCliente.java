package com.example.facturacion.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.facturacion.servicio.ServicioCliente;
import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.enums.CondicionFiscal;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.util.Objects;

/**
    * Controlador para manejar las operaciones relacionadas con los clientes.
 */
@Controller
@RequestMapping("/clientes")
public class ControladorCliente {
    /**
     * Servicio para manejar la lógica de negocio de clientes.
     */
    @Autowired
    private ServicioCliente servicioCliente;

    /**
     * Maneja lista de clientes.
     * @return La vista de redirección.
     */
    @GetMapping("/")
    public String listarClientes() {
        return "redirect:/clientes/listar";
    }

    /**
     * Maneja la solicitud de listar todos los clientes.
     * @param model Modelo para la vista.
     * @param clientesPage paginación de clientes para la vista.
     */

    @GetMapping("/listar")
    public String mostrarListaClientes(Model model,
                                       @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
                                       @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "10") int size) {
        var clientesPage = servicioCliente.obtenerClientesPaginados(page, size);
        model.addAttribute("clientesPage", clientesPage);
        // keep a compatibility attribute with list content
        model.addAttribute("clientes", clientesPage.getContent());
        return "clientes/listar";
    }

    /**
     * Maneja la solicitud de creación de un nuevo cliente.
     * @param cliente El cliente a crear.
     * @param model Modelo para la vista.
     * @return La vista de redirección.
     */
    @GetMapping("/crear")
    public String mostrarFormularioCrearCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("todasLasCondiciones", CondicionFiscal.values()); 
        return "clientes/crear";
    }

    /**
     * Maneja la solicitud de modificación de un cliente existente.
     * @param cliente El cliente con los datos modificados.
     * @param model Modelo para la vista.
     * @param redirectAttrs Atributos para redirección.
     */
    @GetMapping("/modificar")
    public String mostrarFormularioModificarCliente(@org.springframework.web.bind.annotation.RequestParam("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        Cliente cliente = servicioCliente.obtenerClientePorId(id);
        if (cliente == null) {
            redirectAttrs.addFlashAttribute("error", "Cliente con ID " + id + " no encontrado");
            return "redirect:/clientes/listar";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("todasLasCondiciones", CondicionFiscal.values()); 
        return "clientes/modificar";
    }

    /**
     * Maneja la solicitud de creación de un nuevo cliente.
     * @param cliente El cliente a crear.
     * @param bindingResult Resultados de la validación.
     * @param model Modelo para la vista.
     * @param redirectAttrs Atributos para redirección.
     * @return La vista de redirección.
     */

    @PostMapping("/crear")
    public String crearCliente(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            // Loguear todos los errores de validación
            System.out.println("Errores de validación en crear cliente:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            // devolver al formulario con errores
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            return "clientes/crear";
        }
        try {
            // guardar cliente
            servicioCliente.guardarCliente(cliente);
            redirectAttrs.addFlashAttribute("exito", "Cliente creado correctamente.");
            return "redirect:/clientes/listar";
        } catch (IllegalArgumentException ex) {
            // errores de validación de negocio (ej. dni/cuit duplicados)
            String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error de validación de negocio";
            bindingResult.reject("error.cliente", Objects.requireNonNull(msg, "Error de validación de negocio"));
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            return "clientes/crear";
        } catch (DataIntegrityViolationException ex) {
            // integridad en BD (competencia de inserción única)
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.cliente", "Error de integridad de datos: " + rootMsg);
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            return "clientes/crear";
        }
    }


    /**
     * Maneja la solicitud de modificación de un cliente existente.
     * @param cliente El cliente con los datos modificados.
     * @param bindingResult Resultados de la validación.
     * @param model Modelo para la vista.
     * @param redirectAttrs Atributos para redirección.
     * @return La vista de redirección.
     */
    @PostMapping("/modificar")
    public String modificarCliente(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación en modificar cliente:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            return "clientes/modificar";
        }
        try {
            servicioCliente.actualizarCliente(cliente);
            redirectAttrs.addFlashAttribute("exito", "Cliente actualizado correctamente.");
            return "redirect:/clientes/listar";
        } catch (IllegalArgumentException ex) {
            String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error de validación de negocio";
            bindingResult.reject("error.cliente", Objects.requireNonNull(msg, "Error de validación de negocio"));
            return "clientes/modificar";
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.cliente", "Error de integridad de datos: " + rootMsg);
            return "clientes/modificar";
        }
    }

    /**
     * Maneja la solicitud de dar de baja un cliente por su ID.
     * @param id El ID del cliente a dar de baja.
     * @param redirectAttrs Atributos para redirección.
     * @return La vista de redirección.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarPorPath(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            servicioCliente.darDeBajaClientePorId(id);
            redirectAttrs.addFlashAttribute("exito", "Cliente dado de baja correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/clientes/listar";
    }
    
}