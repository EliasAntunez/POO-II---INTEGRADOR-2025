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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.facturacion.modelo.Servicio;
import com.example.facturacion.modelo.enums.Alicuota;
import com.example.facturacion.servicio.ServicioServicio;

import jakarta.validation.Valid;

/**
    * Controlador para manejar las operaciones relacionadas con los servicios.
 */

@Controller
@RequestMapping("/servicios")
public class ControladorServicio {
    /**
     * Servicio para manejar la lógica de negocio de servicios.
     */
    @Autowired
    private ServicioServicio servicioServicio;
    /**
     * Maneja lista de servicios.
     * @return La vista de redirección.
     */
    @GetMapping("/")
    public String listarServicios() {
        return "redirect:/servicios/listar";
    }

    /**
     * Maneja la solicitud de listar todos los servicios.
     * @param model Modelo para la vista.
     * @param serviciosPage paginación de servicios para la vista.
     */

    @GetMapping("/listar")
    public String mostrarListaServicios(Model model,
                                        @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
                                        @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "10") int size) {
        var serviciosPage = servicioServicio.obtenerServiciosPaginados(page, size);
        model.addAttribute("serviciosPage", serviciosPage);
        // keep a compatibility attribute with list content
        model.addAttribute("servicios", serviciosPage.getContent());
        return "servicios/listar";
    }

    /**
     * Maneja la solicitud de creación de un nuevo servicio.
     * @param servicio El servicio a crear.
     * @param model Modelo para la vista.
     * @return La vista de redirección.
     */
    @GetMapping("/crear")
    public String mostrarFormularioCrearServicio(Model model) {
        model.addAttribute("servicio", new Servicio());
        model.addAttribute("todasLasAlicuotas", Alicuota.values());
        return "servicios/crear";
    }

    /**
     * Maneja la solicitud de modificación de un servicio existente.
     * @param servicio El servicio con los datos modificados.
     * @param model Modelo para la vista.
     * @param redirectAttrs Atributos para redirección.
     */
    @GetMapping("/modificar")
    public String mostrarFormularioModificarServicio(@org.springframework.web.bind.annotation.RequestParam("id") Long id, Model model, RedirectAttributes redirectAttrs) {
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
     * Maneja la solicitud de creación de un nuevo servicio.
     * @param servicio El servicio a crear.
     * @param bindingResult Resultados de la validación.
     * @param model Modelo para la vista.
     * @param redirectAttrs Atributos para redirección.
     * @return La vista de redirección.
     */

    @PostMapping("/crear")
    public String crearServicio(@Valid @ModelAttribute("servicio") Servicio servicio, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            // Loguear todos los errores de validación
            System.out.println("Errores de validación en crear servicio:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            // devolver al formulario con errores
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/crear";
        }
        try {
            // guardar servicio
            servicioServicio.guardarServicio(servicio);
            redirectAttrs.addFlashAttribute("exito", "Servicio creado correctamente.");
            return "redirect:/servicios/listar";
        } catch (IllegalArgumentException ex) {
            // errores de validación de negocio (ej. dni/cuit duplicados)
            String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error de validación de negocio";
            bindingResult.reject("error.servicio", Objects.requireNonNull(msg, "Error de validación de negocio"));
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/crear";
        } catch (DataIntegrityViolationException ex) {
            // integridad en BD (competencia de inserción única)
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.servicio", "Error de integridad de datos: " + rootMsg);
            model.addAttribute("todasLasAlicuotas", Alicuota.values());
            return "servicios/crear";
        }
    }


    /**
     * Maneja la solicitud de modificación de un servicio existente.
     * @param servicio El servicio con los datos modificados.
     * @param bindingResult Resultados de la validación.
     * @param model Modelo para la vista.
     * @param redirectAttrs Atributos para redirección.
     * @return La vista de redirección.
     */
    @PostMapping("/modificar")
    public String modificarServicio(@Valid @ModelAttribute("servicio") Servicio servicio, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            System.out.println("Errores de validación en modificar servicio:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error.toString()));
            return "servicios/modificar";
        }
        try {
            servicioServicio.actualizarServicio(servicio);
            redirectAttrs.addFlashAttribute("exito", "Servicio actualizado correctamente.");
            return "redirect:/servicios/listar";
        } catch (IllegalArgumentException ex) {
            String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error de validación de negocio";
            bindingResult.reject("error.servicio", Objects.requireNonNull(msg, "Error de validación de negocio"));
            return "servicios/modificar";
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            String rootMsg = (root != null) ? root.getMessage() : ex.getMessage();
            bindingResult.reject("error.servicio", "Error de integridad de datos: " + rootMsg);
            return "servicios/modificar";
        }
    }

    /**
     * Maneja la solicitud de dar de baja un servicio por su ID.
     * @param id El ID del servicio a dar de baja.
     * @param redirectAttrs Atributos para redirección.
     * @return La vista de redirección.
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
