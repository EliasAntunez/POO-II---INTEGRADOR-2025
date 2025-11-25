package com.example.facturacion.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.servicio.ServicioFactura;

/**
 * Controlador para manejar las operaciones relacionadas con las facturas.
 * HU-11: Registrar Pago Total
 */
@Controller
@RequestMapping("/facturas")
public class ControladorFactura {
    
    @Autowired
    private ServicioFactura servicioFactura;

    /**
     * Redirección raíz a listar.
     */
    @GetMapping("/")
    public String redirigirAListar() {
        return "redirect:/facturas/listar";
    }

    /**
     * HU-11: Listar todas las facturas con paginación.
     * Permite filtrar por estado.
     */
    @GetMapping("/listar")
    public String listarFacturas(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "estado", required = false) String estado,
            Model model) {
        
        try {
            var facturasPage = (estado != null && !estado.isEmpty())
                ? servicioFactura.obtenerFacturasPorEstado(EstadoFactura.valueOf(estado), page, size)
                : servicioFactura.obtenerFacturasPaginadas(page, size);
            
            model.addAttribute("facturasPage", facturasPage);
            model.addAttribute("facturas", facturasPage.getContent());
            model.addAttribute("estadoSeleccionado", estado);
            model.addAttribute("todosLosEstados", EstadoFactura.values());
            
            // Estadísticas para el dashboard
            model.addAttribute("totalPendientes",
                servicioFactura.contarFacturasPorEstado(EstadoFactura.PENDIENTE_PAGO));
            model.addAttribute("totalEmitidas",
                servicioFactura.contarFacturasPorEstado(EstadoFactura.EMITIDA));
            model.addAttribute("totalPagadas",
                servicioFactura.contarFacturasPorEstado(EstadoFactura.PAGADA));
            
            return "facturas/listar";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", "Error al listar facturas: " + ex.getMessage());
            return "facturas/listar";
        }
    }

    /**
     * HU-11: Ver detalle de una factura.
     */
    @GetMapping("/{id}")
    public String verDetalleFactura(@PathVariable("id") Long id,
                                    Model model,
                                    RedirectAttributes redirectAttrs) {
        try {
            Factura factura = servicioFactura.obtenerFacturaPorId(id);
            
            if (factura == null) {
                redirectAttrs.addFlashAttribute("error",
                    "La factura con ID " + id + " no existe");
                return "redirect:/facturas/listar";
            }
            
            model.addAttribute("factura", factura);
            return "facturas/detalle";
            
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error",
                "Error al obtener factura: " + ex.getMessage());
            return "redirect:/facturas/listar";
        }
    }

    /**
     * HU-11: Mostrar formulario de confirmación de pago.
     */
    @GetMapping("/{id}/confirmar-pago")
    public String mostrarConfirmacionPago(@PathVariable("id") Long id,
                                        Model model,
                                        RedirectAttributes redirectAttrs) {
        try {
            Factura factura = servicioFactura.obtenerFacturaPorId(id);
            
            if (factura == null) {
                redirectAttrs.addFlashAttribute("error",
                    "La factura con ID " + id + " no existe");
                return "redirect:/facturas/listar";
            }
            
            // Validar que puede pagarse
            if (!factura.puedePagarse()) {
                redirectAttrs.addFlashAttribute("error",
                    String.format("La factura %s no puede pagarse. Estado actual: %s",
                        factura.getNumeroFactura(),
                        factura.getEstado().getDescripcion()));
                return "redirect:/facturas/" + id;
            }
            
            model.addAttribute("factura", factura);
            return "facturas/confirmar-pago";
            
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error",
                "Error al cargar formulario: " + ex.getMessage());
            return "redirect:/facturas/listar";
        }
    }

    /**
     * HU-11: REGISTRAR PAGO TOTAL DE UNA FACTURA.
     * Este es el método principal de la HU-11.
     */
    @PostMapping("/{id}/pago-total")
    public String registrarPagoTotal(
            @PathVariable("id") Long id,
            @RequestParam(value = "usuarioPago", required = false, defaultValue = "Administrador") String usuarioPago,
            RedirectAttributes redirectAttrs) {
        
        try {
            // Registrar el pago
            Factura facturaPagada = servicioFactura.registrarPagoTotal(id, usuarioPago);
            
            // Mensaje de éxito
            redirectAttrs.addFlashAttribute("exito",
                String.format("✓ Pago registrado exitosamente para la factura %s. Monto: $%.2f",
                    facturaPagada.getNumeroFactura(),
                    facturaPagada.getMontoTotal()));
            
            return "redirect:/facturas/" + id;
            
        } catch (IllegalArgumentException ex) {
            // Error de validación
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/facturas/" + id;
            
        } catch (Exception ex) {
            // Error inesperado
            redirectAttrs.addFlashAttribute("error",
                "Error inesperado al registrar pago: " + ex.getMessage());
            return "redirect:/facturas/" + id;
        }
    }

    /**
     * Listar solo facturas pendientes de pago.
     */
    @GetMapping("/pendientes")
    public String listarFacturasPendientes(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        try {
            var facturasPage = servicioFactura.obtenerFacturasPendientesPaginadas(page, size);
            
            model.addAttribute("facturasPage", facturasPage);
            model.addAttribute("facturas", facturasPage.getContent());
            model.addAttribute("soloPendientes", true);
            
            return "facturas/listar";
        } catch (Exception ex) {
            model.addAttribute("error", "Error al listar facturas pendientes: " + ex.getMessage());
            return "facturas/listar";
        }
    }

    /**
     * Anular una factura.
     */
    @PostMapping("/{id}/anular")
    public String anularFactura(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttrs) {
        try {
            Factura facturaAnulada = servicioFactura.anularFactura(id);
            
            redirectAttrs.addFlashAttribute("exito",
                String.format("Factura %s anulada correctamente",
                    facturaAnulada.getNumeroFactura()));
            
            return "redirect:/facturas/" + id;
            
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/facturas/" + id;
        }
    }
}