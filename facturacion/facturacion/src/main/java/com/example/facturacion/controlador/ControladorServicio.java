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

import com.example.facturacion.modelo.Servicio;
import com.example.facturacion.modelo.enums.Alicuota;
import com.example.facturacion.servicio.ServicioServicio;

import jakarta.validation.Valid;

/**
 * Controlador para manejar las operaciones relacionadas con los servicios.
 * Implementa las HU-04 (Alta), HU-05 (Modificación) y HU-06 (Baja lógica)
 */
@Controller
@RequestMapping("/servicios")
public class ControladorServicio {
    
    @Autowired
    private ServicioServicio servicioServicio;
    
    /**
     * Redirección raíz a listar.
     */
    @GetMapping("/")
    public String listarServicios() {
        return "redirect:/servicios/listar";
    }

    /**
     * HU-04/05/06: Listar servicios activos con paginación.
     */
    @GetMapping("/listar")
    public String mostrarListaServicios(Model model,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        var serviciosPage = servicioServicio.obtenerServiciosPaginados(page, size);
        model.addAttribute("serviciosPage", serviciosPage);
        model.addAttribute("servicios", serviciosPage.getContent());
        return "servicios/listar";
    }

    /**
     * HU-04: Formulario para crear nuevo servicio.
     */
    @GetMapping("/crear")
    public String mostrarFormularioCrearServicio(Model model) {
        Servicio servicio = new Servicio();
        servicio.setActivo(true);  // Por defecto activo
        model.addAttribute("servicio", servicio);
        model.addAttribute("todasLasAlicuotas", Alicuota.values());
        return "servicios/crear";
    }

    /**
     * HU-05: Formulario para modificar servicio existente.
     */
    @GetMapping("/modificar")
    public String mostrarFormularioModificarServicio(@RequestParam("id") Long id, 
                                                      Model model, 
                                                      RedirectAttributes redirectAttrs) {
        Servicio servicio = servicioServicio.obtenerServicioPorId(id);
        if (servicio == null) {
            redirectAttrs.addFlashAttribute("error", "Servicio con ID " + id + " no encontrado");
            return "redirect:/servicios/listar";
        }
        model.addAttribute("servicio", servicio);
        model.addAttribute("todasLasAlicuotas", Alicuota.values());
        return "servicios/modificar";
    }

    /**
     * HU-04: Crear nuevo servicio (POST).
     */
    @PostMapping("/crear")
    public String crearServicio(@Valid @ModelAttribute("servicio") Servicio servicio, 
                                BindingResult bindingResult, 
                                Model model, 
                                RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación en crear servicio:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/crear";
        }
        
        try {
            servicioServicio.guardarServicio(servicio);
            redirectAttrs.addFlashAttribute("exito", "Servicio creado correctamente.");
            return "redirect:/servicios/listar";
        } catch (IllegalArgumentException ex) {
            String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error de validación de negocio";
            bindingResult.reject("error.servicio", Objects.requireNonNull(msg));
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/crear";
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.servicio", "Error de integridad de datos: " + rootMsg);
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/crear";
        }
    }

    /**
     * HU-05: Modificar servicio existente (POST).
     */
    @PostMapping("/modificar")
    public String modificarServicio(@Valid @ModelAttribute("servicio") Servicio servicio, 
                                    BindingResult bindingResult, 
                                    Model model, 
                                    RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación en modificar servicio:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/modificar";
        }
        
        try {
            servicioServicio.actualizarServicio(servicio);
            redirectAttrs.addFlashAttribute("exito", "Servicio actualizado correctamente.");
            return "redirect:/servicios/listar";
        } catch (IllegalArgumentException ex) {
            String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error de validación de negocio";
            bindingResult.reject("error.servicio", Objects.requireNonNull(msg));
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/modificar";
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.servicio", "Error de integridad de datos: " + rootMsg);
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/modificar";
        }
    }

    /**
     * HU-06: Dar de baja lógica un servicio.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarPorPath(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            servicioServicio.darDeBajaServicioPorId(id);
            redirectAttrs.addFlashAttribute("exito", "Servicio dado de baja correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/servicios/listar";
    }
}