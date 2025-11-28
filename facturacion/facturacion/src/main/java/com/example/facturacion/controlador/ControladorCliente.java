package com.example.facturacion.controlador;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.enums.CondicionFiscal;
import com.example.facturacion.modelo.enums.EstadoCliente;
import com.example.facturacion.servicio.ServicioCliente;

import jakarta.validation.Valid;

/**
 * Controlador para manejar las operaciones relacionadas con los clientes.
 * HU-01: Alta de Cliente
 * HU-02: Modificación de Cliente
 * HU-03: Baja de Cliente
 * HU-05: Consulta de Cuenta Corriente
 */
@Controller
@RequestMapping("/clientes")
public class ControladorCliente {
    
    @Autowired
    private ServicioCliente servicioCliente;

    // ==================== Redirección y Listado ====================
    
    /**
     * Redirige a la lista de clientes.
     */
    @GetMapping("/")
    public String listarClientes() {
        return "redirect:/clientes/listar";
    }

    /**
     * Muestra la lista paginada de clientes.
     * HU-01, HU-02, HU-03: Vista principal.
     */
    @GetMapping("/listar")
    public String mostrarListaClientes(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "busqueda", required = false) String busqueda) {
        
        var clientesPage = servicioCliente.obtenerTodosLosClientesPaginados(page, size);
        
        // Aplicar filtros si se proporcionan
        if (estado != null && !estado.isEmpty() && !"TODOS".equals(estado)) {
            try {
                EstadoCliente estadoEnum = EstadoCliente.valueOf(estado);
                clientesPage = servicioCliente.obtenerClientesPorEstado(estadoEnum, page, size);
            } catch (IllegalArgumentException e) {
                // Estado inválido, ignorar filtro
            }
        }
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            clientesPage = servicioCliente.buscarClientes(busqueda, page, size);
        }
        
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("todosLosEstados", EstadoCliente.values());
        
        return "clientes/listar";
    }

    // ==================== HU-01: Alta de Cliente ====================
    
    /**
     * Muestra el formulario de creación de cliente.
     */
    @GetMapping("/crear")
    public String mostrarFormularioCrearCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
        model.addAttribute("todosLosEstados", EstadoCliente.values());
        return "clientes/crear";
    }

    /**
     * Procesa la creación de un nuevo cliente.
     */
    @PostMapping("/crear")
    public String crearCliente(
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {
        
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación en crear cliente:");
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println(error.toString()));
            
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/crear";
        }
        
        try {
            servicioCliente.guardarCliente(cliente);
            redirectAttrs.addFlashAttribute("exito", 
                "Cliente creado correctamente con cuenta corriente inicializada.");
            return "redirect:/clientes/listar";
            
        } catch (IllegalArgumentException ex) {
            String msg = Objects.requireNonNullElse(ex.getMessage(), 
                "Error de validación de negocio");
            bindingResult.reject("error.cliente", msg);
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/crear";
            
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.cliente", 
                "Error de integridad de datos: " + rootMsg);
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/crear";
        }
    }

    // ==================== HU-02: Modificación de Cliente ====================
    
    /**
     * Muestra el formulario de modificación de cliente.
     */
    @GetMapping("/modificar")
    public String mostrarFormularioModificarCliente(
            @RequestParam("id") Long id,
            Model model,
            RedirectAttributes redirectAttrs) {
        
        try {
            Cliente cliente = servicioCliente.obtenerClientePorId(id);
            model.addAttribute("cliente", cliente);
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/modificar";
            
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/clientes/listar";
        }
    }

    /**
     * Procesa la modificación de un cliente.
     * HU-02: DNI y CUIT no pueden modificarse.
     */
    @PostMapping("/modificar")
    public String modificarCliente(
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {
        
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación en modificar cliente:");
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println(error.toString()));
            
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/modificar";
        }
        
        try {
            servicioCliente.actualizarCliente(cliente);
            redirectAttrs.addFlashAttribute("exito", 
                "Cliente actualizado correctamente.");
            return "redirect:/clientes/listar";
            
        } catch (IllegalArgumentException ex) {
            String msg = Objects.requireNonNullElse(ex.getMessage(), 
                "Error de validación de negocio");
            bindingResult.reject("error.cliente", msg);
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/modificar";
            
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.cliente", 
                "Error de integridad de datos: " + rootMsg);
            model.addAttribute("todasLasCondiciones", CondicionFiscal.values());
            model.addAttribute("todosLosEstados", EstadoCliente.values());
            return "clientes/modificar";
        }
    }

    // ==================== HU-03: Baja de Cliente ====================
    
    /**
     * Da de baja un cliente (baja lógica).
     * HU-03: Verifica que no tenga transacciones en proceso.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarPorPath(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {
        
        try {
            servicioCliente.darDeBajaClientePorId(id);
            redirectAttrs.addFlashAttribute("exito", 
                "Cliente dado de baja correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        
        return "redirect:/clientes/listar";
    }
    
    /**
     * Suspende temporalmente un cliente.
     */
    @PostMapping("/suspender/{id}")
    public String suspenderCliente(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {
        
        try {
            servicioCliente.suspenderCliente(id);
            redirectAttrs.addFlashAttribute("exito", 
                "Cliente suspendido correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        
        return "redirect:/clientes/listar";
    }
    
    /**
     * Reactiva un cliente suspendido o dado de baja.
     */
    @PostMapping("/reactivar/{id}")
    public String reactivarCliente(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttrs) {
        
        try {
            servicioCliente.reactivarCliente(id);
            redirectAttrs.addFlashAttribute("exito",
"Cliente reactivado correctamente.");
} catch (IllegalArgumentException ex) {
redirectAttrs.addFlashAttribute("error", ex.getMessage());
}
    return "redirect:/clientes/listar";
}

// ==================== HU-05: Cuenta Corriente ====================

    /**
     * Muestra el detalle de la cuenta corriente de un cliente.
     */
    @GetMapping("/cuenta-corriente/{id}")
    public String verCuentaCorriente(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttrs) {
        
        try {
            Cliente cliente = servicioCliente.obtenerClientePorId(id);
            var movimientos = servicioCliente.obtenerMovimientosCliente(id);
            
            model.addAttribute("cliente", cliente);
            model.addAttribute("movimientos", movimientos);
            model.addAttribute("saldo", cliente.getSaldoCuentaCorriente());
            
            return "clientes/cuenta-corriente";
            
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/clientes/listar";
        }
    }
}