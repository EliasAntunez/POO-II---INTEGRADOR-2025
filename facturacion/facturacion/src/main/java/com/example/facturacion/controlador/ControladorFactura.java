package com.example.facturacion.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.servicio.ResultadoFacturacionMasiva;
import com.example.facturacion.servicio.ServicioCliente;
import com.example.facturacion.servicio.ServicioClienteServicio;
import com.example.facturacion.servicio.ServicioFacturacion;
import com.example.facturacion.servicio.ServicioNotaCredito;
import com.example.facturacion.servicio.ServicioPago;

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
    
    @Autowired
    private ServicioPago servicioPago;

    @Autowired
    private ServicioClienteServicio servicioClienteServicio;

    @Autowired
    private com.example.facturacion.servicio.ServicioServicio servicioServicio;

    // ==================== API AJAX ====================
    @GetMapping("/api/servicios-cliente/{clienteId}")
    @ResponseBody
    public List<Map<String, Object>> obtenerServiciosCliente(@PathVariable Long clienteId) {
        return servicioClienteServicio.obtenerServiciosPorCliente(clienteId).stream()
            .map(cs -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cs.getId()); // ID de ClienteServicio
                map.put("nombre", cs.getServicio().getNombre());
                map.put("precio", cs.getPrecio() != null ? cs.getPrecio() : cs.getServicio().getPrecio());
                return map;
            })
            .toList();
    }

    // ==================== LISTADO Y FILTROS ====================

    @GetMapping("/")
    public String index() {
        return "redirect:/facturas/listar";
    }

    @GetMapping("/nueva-individual")
    public String nuevaIndividual(Model model) {
        model.addAttribute("listaClientes", servicioCliente.obtenerClientesActivos());
        model.addAttribute("active", "facturas");
        return "facturas/crear-individual";
    }

    @GetMapping("/nueva-masiva")
    public String nuevaMasiva(Model model) {
        model.addAttribute("listaServicios", servicioServicio.obtenerTodosLosServicios());
        model.addAttribute("active", "facturas");
        return "facturas/crear-masiva";
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
        
        // 1. Preparar fechas
        LocalDateTime desde = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX) : null;

        // 2. Obtener datos filtrados
        Page<Factura> facturasPage = servicioFacturacion.obtenerFacturasFiltradas(busqueda, estado, desde, hasta, page, size);
        
        // 3. Cargar combos
        model.addAttribute("listaClientes", servicioCliente.obtenerClientesActivos()); 
        model.addAttribute("listaEstados", EstadoFactura.values());
        model.addAttribute("listaServicios", servicioServicio.obtenerTodosLosServicios());

        // 4. Cargar datos
        model.addAttribute("facturasPage", facturasPage);
        model.addAttribute("facturas", facturasPage.getContent());
        
        // 5. Mantener filtros
        model.addAttribute("clienteIdSeleccionado", clienteId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("fechaDesdeSeleccionada", fechaDesde);
        model.addAttribute("fechaHastaSeleccionada", fechaHasta);
        
        model.addAttribute("active", "facturas");
        
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
    
    // ==================== PAGOS (NUEVO) ====================

    @GetMapping("/ver-pagos/{facturaId}")
    public String verPagosFactura(@PathVariable("facturaId") Long facturaId, Model model, RedirectAttributes redirectAttrs) {
        try {
            Factura factura = servicioFacturacion.obtenerFacturaPorId(facturaId);
            var pagos = servicioPago.obtenerPagosDeFactura(facturaId);
            
            model.addAttribute("factura", factura);
            model.addAttribute("pagos", pagos);
            model.addAttribute("active", "facturas");
            return "pagos/por-factura";
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Error al cargar pagos: " + ex.getMessage());
            return "redirect:/facturas/listar";
        }
    }

    // ==================== GENERACIÓN (VISTA) ====================

    @GetMapping("/crear")
    public String mostrarPanelGeneracion(Model model) {
        // No se usa mucho porque ahora está integrado en listar, pero lo dejamos por si acaso
        return "redirect:/facturas/listar";
    }

    // ==================== PROCESOS (POST) ====================

    @PostMapping("/generar-individual")
    public String procesarFacturacionIndividual(
            @RequestParam("clienteId") Long clienteId,
            @RequestParam(value = "tipoFacturacion", required = false, defaultValue = "mensual") String tipoFacturacion,
            @RequestParam(value = "mesFacturacion", required = false) String mesFacturacionStr,
            @RequestParam(value = "fechaEmision", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmision,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "serviciosIds", required = false) List<Long> serviciosIds,
            RedirectAttributes redirectAttrs) {
        try {
            Factura factura;
            
            if (fechaEmision == null) {
                fechaEmision = LocalDate.now();
            }

            if ("rango".equals(tipoFacturacion) && fechaInicio != null && fechaFin != null) {
                // Facturación con rango personalizado
                factura = servicioFacturacion.ejecutarFacturacionIndividualConRango(
                    clienteId, fechaEmision, fechaInicio, fechaFin, false, serviciosIds
                );
                redirectAttrs.addFlashAttribute("exito", 
                    String.format("Factura generada correctamente. N°: %d (Período: %s a %s)", 
                                 factura.getId(), fechaInicio, fechaFin));
            } else {
                // Facturación mensual (automática o seleccionada)
                YearMonth mesFacturado;
                if (mesFacturacionStr != null && !mesFacturacionStr.isEmpty()) {
                    mesFacturado = YearMonth.parse(mesFacturacionStr);
                } else {
                    // Default: mes anterior
                    mesFacturado = YearMonth.from(LocalDate.now()).minusMonths(1);
                }
                
                LocalDate inicio = mesFacturado.atDay(1);
                LocalDate fin = mesFacturado.atEndOfMonth();

                factura = servicioFacturacion.ejecutarFacturacionIndividualConRango(
                    clienteId, fechaEmision, inicio, fin, false, serviciosIds
                );
                
                redirectAttrs.addFlashAttribute("exito", 
                    String.format("Factura generada correctamente. N°: %d (Período: %s)", 
                                 factura.getId(), mesFacturado));
            }
            
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
    public String procesarFacturacionMasiva(
            @RequestParam(value = "tipoFacturacion", required = false, defaultValue = "mensual") String tipoFacturacion,
            @RequestParam(value = "mesFacturacion", required = false) String mesFacturacionStr,
            @RequestParam(value = "fechaEmision", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmision,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "serviciosIds", required = false) List<Long> serviciosIds,
            RedirectAttributes redirectAttrs) {
        try {
            ResultadoFacturacionMasiva resultado;
            
            if (fechaEmision == null) {
                fechaEmision = LocalDate.now();
            }

            if ("rango".equals(tipoFacturacion) && fechaInicio != null && fechaFin != null) {
                // Facturación con rango personalizado
                resultado = servicioFacturacion.ejecutarFacturacionMasivaConRango(
                    fechaEmision, fechaInicio, fechaFin, serviciosIds
                );
            } else {
                // Facturación mensual (automática o seleccionada)
                YearMonth mesFacturado;
                if (mesFacturacionStr != null && !mesFacturacionStr.isEmpty()) {
                    mesFacturado = YearMonth.parse(mesFacturacionStr);
                } else {
                    // Default: mes anterior
                    mesFacturado = YearMonth.from(LocalDate.now()).minusMonths(1);
                }
                
                LocalDate inicio = mesFacturado.atDay(1);
                LocalDate fin = mesFacturado.atEndOfMonth();

                resultado = servicioFacturacion.ejecutarFacturacionMasivaConRango(
                    fechaEmision, inicio, fin, serviciosIds
                );
            }
            
            if (resultado.getExitosas() > 0) {
                redirectAttrs.addFlashAttribute("exito", 
                    String.format("Facturación masiva completada. " +
                                "Exitosas: %d | Fallidas: %d | Omitidas: %d | " +
                                "Total facturado: $%.2f", 
                                resultado.getExitosas(), 
                                resultado.getFallidas(),
                                resultado.getOmitidas(),
                                resultado.getMontoTotalFacturado()));
            } else {
                redirectAttrs.addFlashAttribute("info", 
                    String.format("El proceso finalizó sin generar facturas. " +
                                "Fallidas: %d | Omitidas: %d", 
                                resultado.getFallidas(),
                                resultado.getOmitidas()));
            }
            
            // Opcional: guardar detalles para mostrar después
            // redirectAttrs.addFlashAttribute("detalles", resultado.getDetalles());
            
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("error", "Validación AFIP: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error en facturación masiva", ex);
            redirectAttrs.addFlashAttribute("error", "Error inesperado: " + ex.getMessage());
        }
        return "redirect:/facturas/listar";
    }

    @PostMapping("/anular/{id}")
    public String anularFactura(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            // Validamos en el servicio si tiene pagos antes de anular (agregado en ServicioFacturacion)
            servicioFacturacion.anularFactura(id); // Llama al método que coordina la anulación y creación de NC
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