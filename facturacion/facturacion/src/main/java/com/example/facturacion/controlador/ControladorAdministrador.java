package com.example.facturacion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
    * Controlador para manejar las vistas del administrador.
 */

@Controller
public class ControladorAdministrador {

    /**
     * Maneja la solicitud de redirección al dashboard.
     */
    @GetMapping("/")
    public String redirigirDashboard() {
        return "redirect:/dashboard";
    }

    /**
     * Maneja la solicitud para mostrar el dashboard.
     */
    @GetMapping("/dashboard")
    public String mostrarDashboard() {
        return "dashboard";
    }
}