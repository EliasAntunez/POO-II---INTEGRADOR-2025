package com.example.facturacion.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.example.facturacion.servicio.ServicioCliente;
import com.example.facturacion.servicio.ServicioFacturacion;
import com.example.facturacion.servicio.ServicioNotaCredito;

@Controller
@RequestMapping("/facturas")
public class ControladorFactura {

    private static final Logger logger = LoggerFactory.getLogger(ControladorFactura.class);

    @Autowired
    private ServicioFacturacion servicioFacturacion;

    @Autowired
    private ServicioCliente servicioCliente;

    @Autowired
    private ServicioNotaCredito servicioNotaCredito;

    // ==================== LISTADO Y FILTROS ====================

    @GetMapping("/")
    public String index() {
        return "redirect:/facturas/listar";
    }

    @GetMapping("/listar")
    public String listarFacturas(Model model,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 @RequestParam(value = "clienteId", required = false) Long clienteId,
                                 @RequestParam(value = "estado", required = false) EstadoFactura estado,
                                 @RequestParam(value = "busqueda", required = false) String busqueda,
                                 @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                                 @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        
        // Conversión de fechas para filtro
        LocalDateTime desde = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX) : null;

        // Llamada al servicio con filtros (necesitas crear este método en ServicioFacturacion si no existe, o usar el paginado simple por ahora)
        // Por ahora usaré el método paginado simple si no tienes el filtro implementado en el servicio
        Page<Factura> facturasPage;
        
        // TODO: Idealmente deberías tener un método obtenerFacturasFiltradas en el servicio. 
        // Si no lo tienes, usa obtenerFacturasPaginadas(page, size) pero los filtros no funcionarán en backend.
        // Asumo que usaremos el básico por ahora para que compile:
        facturasPage = servicioFacturacion.obtenerFacturasPaginadas(page, size);
        
        // Cargar datos para los COMBOS de la vista (ESTO FALTABA)
        model.addAttribute("listaClientes", servicioCliente.obtenerClientesActivos()); 
        model.addAttribute("listaEstados", EstadoFactura.values());

        // Datos para la tabla y paginación
        model.addAttribute("facturasPage", facturasPage);
        model.addAttribute("facturas", facturasPage.getContent());
        
        // Mantener valores de filtros seleccionados en la vista
        model.addAttribute("clienteIdSeleccionado", clienteId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", busqueda);
        
        model.addAttribute("active", "facturas"); // Para resaltar menú
        return "facturas/listar";
    }

    // ==================== DETALLE ====================

    @GetMapping("/ver/{id}")
    public String verFactura(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            Factura factura = servicioFacturacion.obtenerFacturaPorId(id);
            model.addAttribute("factura", factura);
            model.addAttribute("active", "facturas");
            return "facturas/ver";
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/facturas/listar";
        }
    }

    // ==================== PROCESOS (POST) ====================

    @PostMapping("/generar-individual")
    public String procesarFacturacionIndividual(@RequestParam("clienteId") Long clienteId, 
                                                RedirectAttributes redirectAttrs) {
        try {
            Factura factura = servicioFacturacion.ejecutarFacturacionIndividual(clienteId);
            redirectAttrs.addFlashAttribute("exito", "Factura generada correctamente. N°: " + factura.getId());
            return "redirect:/facturas/ver/" + factura.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", "No se pudo facturar: " + ex.getMessage());
            return "redirect:/facturas/listar";
        } catch (Exception ex) {
            logger.error("Error en facturación individual", ex);
            redirectAttrs.addFlashAttribute("error", "Error inesperado: " + ex.getMessage());
            return "redirect:/facturas/listar";
        }
    }

    @PostMapping("/generar-masiva")
    public String procesarFacturacionMasiva(@RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            RedirectAttributes redirectAttrs) {
        try {
            int cantidad = servicioFacturacion.ejecutarFacturacionMasiva(inicio, fin);
            if (cantidad > 0) {
                redirectAttrs.addFlashAttribute("exito", "Proceso masivo finalizado. Se generaron " + cantidad + " facturas.");
            } else {
                redirectAttrs.addFlashAttribute("info", "El proceso finalizó sin generar facturas (no hay contratos pendientes).");
            }
        } catch (Exception ex) {
            logger.error("Error en facturación masiva", ex);
            redirectAttrs.addFlashAttribute("error", "Error crítico: " + ex.getMessage());
        }
        return "redirect:/facturas/listar";
    }

    @PostMapping("/anular/{id}")
    public String anularFactura(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            servicioNotaCredito.crearNotaCreditoPorAnulacion(id, "Anulación a solicitud del usuario");
            redirectAttrs.addFlashAttribute("exito", "Factura anulada correctamente. Se generó la Nota de Crédito.");
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/facturas/ver/" + id;
    }

    @GetMapping("/ver-nota-credito/factura/{facturaId}")
    public String verNotaCreditoPorFactura(@PathVariable("facturaId") Long facturaId, Model model, RedirectAttributes redirectAttrs) {
        try {
            var notaCredito = servicioNotaCredito.obtenerPorFactura(facturaId);
            model.addAttribute("nc", notaCredito);
            return "facturas/ver-nc";
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "No se encontró la nota de crédito.");
            return "redirect:/facturas/ver/" + facturaId;
        }
    }
}