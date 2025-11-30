package com.example.facturacion.controlador;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.Pago;
import com.example.facturacion.modelo.enums.MedioPago;
import com.example.facturacion.servicio.ServicioFacturacion;
import com.example.facturacion.servicio.ServicioPago;

@Controller
@RequestMapping("/pagos")
public class ControladorPago {

    @Autowired
    private ServicioPago servicioPago;

    @Autowired
    private ServicioFacturacion servicioFacturacion;

    /**
     * Formulario para registrar pago de una factura.
     */
    @GetMapping("/nuevo/{facturaId}")
    public String nuevoPago(@PathVariable("facturaId") Long facturaId, Model model, RedirectAttributes redirectAttrs) {
        try {
            Factura factura = servicioFacturacion.obtenerFacturaPorId(facturaId);
            
            // Validar si se puede pagar
            if (factura.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttrs.addFlashAttribute("error", "La factura ya está pagada.");
                return "redirect:/facturas/ver/" + facturaId;
            }
            if (factura.isAnulada()) {
                redirectAttrs.addFlashAttribute("error", "No se puede pagar una factura anulada.");
                return "redirect:/facturas/ver/" + facturaId;
            }

            Pago pago = new Pago();
            // Sugerimos pagar el total pendiente
            pago.setMonto(factura.getSaldoPendiente()); 
            
            model.addAttribute("pago", pago);
            model.addAttribute("factura", factura);
            model.addAttribute("mediosPago", MedioPago.values());
            
            return "pagos/crear";
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Error al cargar factura: " + ex.getMessage());
            return "redirect:/facturas/listar";
        }
    }

    /**
     * Procesar el pago.
     */
    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute Pago pago, 
                              @RequestParam("facturaId") Long facturaId,
                              RedirectAttributes redirectAttrs) {
        try {
            servicioPago.registrarPago(pago, facturaId);
            redirectAttrs.addFlashAttribute("exito", "Pago registrado correctamente. Comprobante almacenado.");
            return "redirect:/facturas/ver/" + facturaId;
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pagos/nuevo/" + facturaId;
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Error inesperado: " + ex.getMessage());
            return "redirect:/pagos/nuevo/" + facturaId;
        }
    }

    /**
     * Ver comprobante individual (Recibo X)
     */
    @GetMapping("/ver-comprobante/{id}")
    public String verComprobante(@PathVariable("id") Long id, Model model) {
        Pago pago = servicioPago.obtenerPagoPorId(id);
        model.addAttribute("pago", pago);
        return "pagos/ver-recibo"; // Vista de impresión
    }
}