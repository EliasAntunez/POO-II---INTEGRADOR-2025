package com.example.facturacion.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.ClienteServicio;
import com.example.facturacion.modelo.DetalleFactura;
import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.MovimientoCuentaCorriente;
import com.example.facturacion.modelo.Servicio;
import com.example.facturacion.modelo.enums.CondicionPago;
import com.example.facturacion.modelo.enums.EstadoCliente;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.modelo.enums.TipoMovimiento;
import com.example.facturacion.repositorio.RepositorioCliente;
import com.example.facturacion.repositorio.RepositorioClienteServicio;
import com.example.facturacion.repositorio.RepositorioFactura;

@Service
public class ServicioFacturacion {

    private static final Logger log = LoggerFactory.getLogger(ServicioFacturacion.class);
    private static final int BATCH_SIZE = 50;
    // Plazos AFIP Argentina
    private static final int DIAS_RETROACTIVO_SERVICIOS = 10;
    private static final int DIAS_RETROACTIVO_BIENES = 5;
    // Mantenemos esta constante para la lógica de determinación de período automático
    private static final int DIAS_CIERRE_MES = 10; 

    @Autowired
    private RepositorioFactura repositorioFactura;

    @Autowired
    private RepositorioCliente repositorioCliente;

    @Autowired
    private RepositorioClienteServicio repositorioClienteServicio;

    @Autowired
    private ServicioCliente servicioCliente;

    @Autowired
    private ServicioNotaCredito servicioNotaCredito;

    // ==================== PROCESO MASIVO CON NORMATIVA AFIP ====================
    /**
     * Ejecuta facturación masiva cumpliendo normativa AFIP para servicios continuos.
     * Usa el período mensual automático.
     * 
     * @param fechaEmision Fecha de emisión de las facturas (debe cumplir normativa AFIP)
     * @return Resultado detallado de la facturación con estadísticas
     */
    @Transactional
    public ResultadoFacturacionMasiva ejecutarFacturacionMasiva(LocalDate fechaEmision) {
        return ejecutarFacturacionMasiva(fechaEmision, (List<Long>) null);
    }

    @Transactional
    public ResultadoFacturacionMasiva ejecutarFacturacionMasiva(LocalDate fechaEmision, List<Long> serviciosIds) {
        log.info("=== INICIANDO FACTURACIÓN MASIVA (PERÍODO MENSUAL) ===");
        log.info("Fecha de emisión solicitada: {}", fechaEmision);
        
        // VALIDACIÓN 1: Cumplir normativa AFIP de fechas
        validarFechaEmisionAFIP(fechaEmision);
        
        // Determinar el período facturado automáticamente
        YearMonth mesFacturado = determinarPeriodoFacturado(fechaEmision);
        LocalDate inicioPeriodo = mesFacturado.atDay(1);
        LocalDate finPeriodo = mesFacturado.atEndOfMonth();
        
        return ejecutarFacturacionMasivaConPeriodo(fechaEmision, inicioPeriodo, finPeriodo, serviciosIds);
    }

    /**
     * Ejecuta facturación masiva con período personalizado (rango de fechas).
     * Permite facturar cualquier rango de fechas sin restricciones AFIP de período mensual.
     * 
     * @param fechaEmision Fecha de emisión de las facturas
     * @param inicioPeriodo Fecha de inicio del período a facturar
     * @param finPeriodo Fecha de fin del período a facturar
     * @return Resultado detallado de la facturación con estadísticas
     */
    @Transactional
    public ResultadoFacturacionMasiva ejecutarFacturacionMasivaConRango(
            LocalDate fechaEmision, 
            LocalDate inicioPeriodo, 
            LocalDate finPeriodo) {
        return ejecutarFacturacionMasivaConRango(fechaEmision, inicioPeriodo, finPeriodo, null);
    }

    @Transactional
    public ResultadoFacturacionMasiva ejecutarFacturacionMasivaConRango(
            LocalDate fechaEmision, 
            LocalDate inicioPeriodo, 
            LocalDate finPeriodo,
            List<Long> serviciosIds) {
        
        log.info("=== INICIANDO FACTURACIÓN MASIVA (RANGO PERSONALIZADO) ===");
        log.info("Fecha de emisión: {}", fechaEmision);
        log.info("Rango: {} a {}", inicioPeriodo, finPeriodo);
        
        // Validaciones básicas del rango
        if (inicioPeriodo == null || finPeriodo == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin del período son obligatorias");
        }
        
        if (inicioPeriodo.isAfter(finPeriodo)) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser posterior a la fecha de fin. " +
                "Inicio: " + inicioPeriodo + ", Fin: " + finPeriodo
            );
        }
        
        if (fechaEmision == null) {
            fechaEmision = LocalDate.now();
            log.info("Fecha de emisión no especificada, usando hoy: {}", fechaEmision);
        }
        
        // No validar AFIP para rangos personalizados (más flexibilidad)
        return ejecutarFacturacionMasivaConPeriodo(fechaEmision, inicioPeriodo, finPeriodo, serviciosIds);
    }

    /**
     * Método privado que ejecuta la facturación masiva con un período específico.
     * Usado tanto por facturación mensual como por rango personalizado.
     */
    @Transactional
    protected ResultadoFacturacionMasiva ejecutarFacturacionMasivaConPeriodo(
            LocalDate fechaEmision,
            LocalDate inicioPeriodo, 
            LocalDate finPeriodo,
            List<Long> serviciosIds) {
        
        log.info("Período a facturar: {} a {}", inicioPeriodo, finPeriodo);
        
        ResultadoFacturacionMasiva resultado = new ResultadoFacturacionMasiva();
        resultado.setFechaEmision(fechaEmision);
        resultado.setPeriodoInicio(inicioPeriodo);
        resultado.setPeriodoFin(finPeriodo);
        
        // Procesar clientes en lotes (paginación)
        int pageNumber = 0;
        Page<Cliente> clientesPage;
        
        do {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            clientesPage = repositorioCliente.findByEstado(EstadoCliente.ACTIVO, pageable);
            
            log.info("Procesando lote {}/{} ({} clientes)", 
                     pageNumber + 1, 
                     clientesPage.getTotalPages(), 
                     clientesPage.getNumberOfElements());
            
            for (Cliente cliente : clientesPage.getContent()) {
                procesarClienteConNormativaAFIP(cliente, fechaEmision, inicioPeriodo, finPeriodo, resultado, serviciosIds);
            }
            
            pageNumber++;
        } while (clientesPage.hasNext());
        
        log.info("=== FACTURACIÓN MASIVA COMPLETADA ===");
        log.info("Exitosas: {} | Fallidas: {} | Omitidas: {}", 
                 resultado.getExitosas(), resultado.getFallidas(), resultado.getOmitidas());
        log.info("Monto total facturado: ${}", resultado.getMontoTotalFacturado());
        
        return resultado;
    }

    /**
     * Valida que la fecha de emisión cumpla con normativa AFIP (Argentina).
     * 
     * REGLAS:
     * - No puede ser fecha futura.
     * - Servicios: Hasta 10 días corridos anteriores a la fecha de emisión (hoy).
     * - Bienes: Hasta 5 días corridos anteriores a la fecha de emisión (hoy).
     * 
     * Si se excede el plazo, se debe emitir con fecha actual.
     */
    private void validarFechaEmisionAFIP(LocalDate fechaEmision) {
        LocalDate hoy = LocalDate.now();
        
        // 1. No puede ser fecha futura
        if (fechaEmision.isAfter(hoy)) {
            throw new IllegalArgumentException(
                "La fecha de emisión no puede ser futura. Fecha ingresada: " + fechaEmision
            );
        }
        
        // 2. Validar retroactividad
        long diasDiferencia = ChronoUnit.DAYS.between(fechaEmision, hoy);
        
        // Asumimos Servicios por defecto (10 días) ya que es el dominio principal
        if (diasDiferencia > DIAS_RETROACTIVO_SERVICIOS) {
            throw new IllegalArgumentException(
                String.format(
                    "La fecha de emisión excede el plazo retroactivo permitido por AFIP para Servicios (%d días). " +
                    "Fecha ingresada: %s (%d días atrás). " +
                    "Si olvidó facturar, debe emitir con la fecha actual (%s) para no alterar la realidad contable.",
                    DIAS_RETROACTIVO_SERVICIOS, fechaEmision, diasDiferencia, hoy
                )
            );
        }
        
        log.info("✓ Fecha válida: {} días de antigüedad (permitido hasta {})", diasDiferencia, DIAS_RETROACTIVO_SERVICIOS);
    }

    /**
     * Determina el período (mes) que se va a facturar según la fecha de emisión.
     */
    private YearMonth determinarPeriodoFacturado(LocalDate fechaEmision) {
        LocalDate ultimoDiaMes = fechaEmision.withDayOfMonth(fechaEmision.lengthOfMonth());
        long diasHastaFinMes = ChronoUnit.DAYS.between(fechaEmision, ultimoDiaMes);
        
        // Si estamos en los últimos días del mes (cierre), facturamos ese mes
        if (diasHastaFinMes <= DIAS_CIERRE_MES) {
            return YearMonth.from(fechaEmision);
        }
        
        // Si no, facturamos el mes anterior
        return YearMonth.from(fechaEmision).minusMonths(1);
    }

    /**
     * Procesa un cliente individual aplicando todas las validaciones AFIP.
     * Usa propagación REQUIRES_NEW para que cada cliente sea una transacción independiente.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void procesarClienteConNormativaAFIP(
            Cliente cliente, 
            LocalDate fechaEmision,
            LocalDate inicioPeriodo, 
            LocalDate finPeriodo,
            ResultadoFacturacionMasiva resultado,
            List<Long> serviciosIds) {
        
        try {
            log.debug("Procesando cliente ID: {} - {}", cliente.getId(), 
                     cliente.getRazonSocial() != null ? cliente.getRazonSocial() : 
                     (cliente.getNombre() + " " + cliente.getApellido()));
            
            // VALIDACIÓN 1: Cliente activo
            if (cliente.getEstado() != EstadoCliente.ACTIVO) {
                resultado.agregarOmitido(
                    cliente.getId(), 
                    obtenerNombreCliente(cliente),
                    "Cliente no activo: " + cliente.getEstado()
                );
                return;
            }
            
            // VALIDACIÓN 2: No facturar dos veces el mismo período
            if (yaFueFacturadoEnPeriodo(cliente, inicioPeriodo, finPeriodo)) {
                resultado.agregarOmitido(
                    cliente.getId(),
                    obtenerNombreCliente(cliente),
                    String.format("Ya facturado en período %s a %s", 
                                 inicioPeriodo, finPeriodo)
                );
                return;
            }
            
            // VALIDACIÓN 3: Servicios activos
            List<ClienteServicio> serviciosActivos = repositorioClienteServicio
                .findByClienteAndActivoTrue(cliente);
            
            // Filtrar por servicios seleccionados si se especificaron
            if (serviciosIds != null && !serviciosIds.isEmpty()) {
                serviciosActivos = serviciosActivos.stream()
                    .filter(cs -> serviciosIds.contains(cs.getServicio().getId()))
                    .toList();
            }

            if (serviciosActivos.isEmpty()) {
                resultado.agregarOmitido(
                    cliente.getId(),
                    obtenerNombreCliente(cliente),
                    "Sin servicios activos" + (serviciosIds != null ? " seleccionados" : "")
                );
                return;
            }
            
            // VALIDACIÓN 4: Cliente CONTADO no debe tener deuda
            if (cliente.getCondicionPago() == CondicionPago.CONTADO) {
                BigDecimal saldo = cliente.getSaldoCuentaCorriente();
                if (saldo != null && saldo.compareTo(BigDecimal.ZERO) < 0) {
                    resultado.agregarFallido(
                        cliente.getId(),
                        obtenerNombreCliente(cliente),
                        String.format("Cliente CONTADO con deuda pendiente: $%.2f", saldo.abs())
                    );
                    return;
                }
            }
            
            // GENERAR FACTURA
            Factura factura = generarFacturaConFechaEmision(
                cliente, 
                serviciosActivos, 
                fechaEmision,
                inicioPeriodo,
                finPeriodo
            );
            
            resultado.agregarExitoso(
                cliente.getId(),
                obtenerNombreCliente(cliente),
                factura.getId(),
                factura.getTotal()
            );
            
            log.debug("✓ Factura {} generada exitosamente para cliente {}", 
                     factura.getId(), cliente.getId());
            
        } catch (Exception e) {
            log.error("✗ Error al procesar cliente {}: {}", 
                     cliente.getId(), e.getMessage(), e);
            resultado.agregarFallido(
                cliente.getId(),
                obtenerNombreCliente(cliente),
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Verifica si el cliente ya tiene factura en el período especificado.
     */
    private boolean yaFueFacturadoEnPeriodo(Cliente cliente, LocalDate inicio, LocalDate fin) {
        return repositorioFactura.existsByClienteAndFechaEmisionBetween(cliente, inicio, fin);
    }

    /**
     * Obtiene el nombre del cliente para mostrar en reportes.
     */
    private String obtenerNombreCliente(Cliente cliente) {
        if (cliente.getRazonSocial() != null && !cliente.getRazonSocial().isEmpty()) {
            return cliente.getRazonSocial();
        }
        return cliente.getNombre() + " " + cliente.getApellido();
    }

    /**
     * Genera factura con fecha de emisión específica (para facturación masiva).
     */
    private Factura generarFacturaConFechaEmision(
            Cliente cliente,
            List<ClienteServicio> servicios,
            LocalDate fechaEmision,
            LocalDate periodoInicio,
            LocalDate periodoFin) {
        
        Factura factura = new Factura();
        factura.setCliente(cliente);
        
        // Convertir LocalDate a LocalDateTime (inicio del día)
        factura.setFechaEmision(fechaEmision.atStartOfDay());
        factura.setFechaVencimiento(calcularFechaVencimiento(fechaEmision, cliente));
        
        // NUEVO: Guardar período facturado
        factura.setFechaInicioPeriodo(periodoInicio);
        factura.setFechaFinPeriodo(periodoFin);
        
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        
        // Asignar tipo de comprobante según condición fiscal
        factura.setTipoComprobante(
            com.example.facturacion.modelo.enums.TipoComprobante.getTipoFactura(cliente.getCondicionFiscal())
        );
        
        // Agregar detalles de servicios
        BigDecimal totalFactura = BigDecimal.ZERO;
        List<DetalleFactura> detalles = new ArrayList<>();
        
        for (ClienteServicio cs : servicios) {
            Servicio servicio = cs.getServicio();
            
            DetalleFactura detalle = new DetalleFactura();
            detalle.setServicio(servicio);
            // Nota: DetalleFactura no tiene campo descripción, se obtiene del servicio
            detalle.setCantidad(1);
            detalle.setPrecioUnitario(cs.getPrecio());
            
            // Setear alícuota IVA
            detalle.setAlicuotaIva(BigDecimal.valueOf(servicio.getAlicuota().getValor()));
            
            // Calcular montos (incluye IVA)
            detalle.calcularMontos();
            
            factura.agregarDetalle(detalle);
            totalFactura = totalFactura.add(detalle.getSubtotal());
        }
        
        factura.setTotal(totalFactura);
        
        // Guardar factura
        Factura facturaGuardada = repositorioFactura.save(factura);
        
        // Actualizar cuenta corriente
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setFactura(facturaGuardada); // Vincular factura para el link
        movimiento.setTipoMovimiento(TipoMovimiento.FACTURA);
        movimiento.setMonto(facturaGuardada.getTotal());
        movimiento.setDescripcion(
            "Factura " + facturaGuardada.getTipoComprobante().getLetra() + 
            " N° " + facturaGuardada.getId() + 
            " - Período " + periodoInicio + " a " + periodoFin
        );
        
        servicioCliente.registrarMovimiento(cliente.getId(), movimiento);
        
        return facturaGuardada;
    }

    /**
     * Calcula fecha de vencimiento según condición de pago del cliente.
     */
    private LocalDate calcularFechaVencimiento(LocalDate fechaEmision, Cliente cliente) {
        if (cliente.getCondicionPago() == null) {
            return fechaEmision.plusDays(30);
        }
        
        return switch (cliente.getCondicionPago()) {
            case CONTADO -> fechaEmision; // Vence el mismo día
            case CUENTA_CORRIENTE_30 -> fechaEmision.plusDays(30);
            case CUENTA_CORRIENTE_60 -> fechaEmision.plusDays(60);
            case CUENTA_CORRIENTE_90 -> fechaEmision.plusDays(90);
            default -> fechaEmision.plusDays(30);
        };
    }

    // ==================== PROCESO MASIVO LEGACY (RETROCOMPATIBILIDAD) ====================
    /**
     * Método legacy mantenido para retrocompatibilidad.
     * Se recomienda usar ejecutarFacturacionMasiva(LocalDate fechaEmision) en su lugar.
     * 
     * @deprecated Usar ejecutarFacturacionMasiva(LocalDate fechaEmision) para cumplir normativa AFIP
     */
    @Deprecated
    @Transactional
    public int ejecutarFacturacionMasiva(LocalDate inicio, LocalDate fin) {
        log.warn("Usando método legacy de facturación masiva. Se recomienda usar la versión con fecha de emisión AFIP.");
        
        // Determinar fecha de emisión válida (último día del período)
        LocalDate fechaEmision = fin;
        
        try {
            ResultadoFacturacionMasiva resultado = ejecutarFacturacionMasiva(fechaEmision);
            return resultado.getExitosas();
        } catch (IllegalArgumentException e) {
            log.error("Error en facturación masiva legacy: {}", e.getMessage());
            // Fallback: usar fecha actual si la fecha no es válida
            ResultadoFacturacionMasiva resultadoFallback = ejecutarFacturacionMasiva(LocalDate.now());
            return resultadoFallback.getExitosas();
        }
    }

    // ==================== PROCESO INDIVIDUAL ====================
    /**
     * Facturación individual con validación de doble facturación (período mensual automático).
     * 
     * @param clienteId ID del cliente a facturar
     * @param forzarRefacturacion Si es true, permite refacturar el mes actual
     * @return Factura generada
     */
    @Transactional
    public Factura ejecutarFacturacionIndividual(Long clienteId, boolean forzarRefacturacion) {
        LocalDate hoy = LocalDate.now();
        YearMonth mesAnterior = YearMonth.from(hoy).minusMonths(1);
        
        return ejecutarFacturacionIndividualConRango(
            clienteId, 
            hoy,
            mesAnterior.atDay(1), 
            mesAnterior.atEndOfMonth(),
            forzarRefacturacion,
            null // Todos los servicios
        );
    }

    /**
     * Facturación individual con rango de fechas personalizado.
     * Permite especificar el período exacto a facturar.
     * 
     * @param clienteId ID del cliente a facturar
     * @param fechaEmision Fecha de emisión de la factura
     * @param inicioPeriodo Fecha de inicio del período
     * @param finPeriodo Fecha de fin del período
     * @param forzarRefacturacion Si es true, permite refacturar aunque ya exista factura en el período
     * @param clienteServicioIds Lista de IDs de servicios del cliente a facturar (null o vacío para todos)
     * @return Factura generada
     */
    @Transactional
    public Factura ejecutarFacturacionIndividualConRango(
            Long clienteId,
            LocalDate fechaEmision,
            LocalDate inicioPeriodo,
            LocalDate finPeriodo,
            boolean forzarRefacturacion,
            List<Long> clienteServicioIds) {
        
        Cliente cliente = servicioCliente.obtenerClientePorId(clienteId);

        if (cliente.getEstado() != EstadoCliente.ACTIVO) {
            throw new IllegalArgumentException("No se puede facturar un cliente " + cliente.getEstado());
        }

        // Validaciones de fechas
        if (inicioPeriodo == null || finPeriodo == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin del período son obligatorias");
        }
        
        if (inicioPeriodo.isAfter(finPeriodo)) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        if (fechaEmision == null) {
            fechaEmision = LocalDate.now();
        }

        // Validar doble facturación
        if (!forzarRefacturacion) {
            if (yaFueFacturadoEnPeriodo(cliente, inicioPeriodo, finPeriodo)) {
                throw new IllegalArgumentException(
                    String.format("El cliente ya fue facturado en el período %s a %s. ",
                                inicioPeriodo, finPeriodo)
                );
            }
        }

        List<ClienteServicio> serviciosActivos = repositorioClienteServicio
            .findByActivoTrue().stream()
            .filter(cs -> cs.getCliente().getId().equals(clienteId))
            .toList();

        // Filtrar por servicios seleccionados si se especificaron
        if (clienteServicioIds != null && !clienteServicioIds.isEmpty()) {
            serviciosActivos = serviciosActivos.stream()
                .filter(cs -> clienteServicioIds.contains(cs.getId()))
                .toList();
        }

        if (serviciosActivos.isEmpty()) {
            throw new IllegalArgumentException("El cliente no tiene servicios activos seleccionados para facturar");
        }

        return generarFacturaConFechaEmision(
            cliente,
            serviciosActivos,
            fechaEmision,
            inicioPeriodo,
            finPeriodo
        );
    }

    /**
     * Sobrecarga del método para mantener retrocompatibilidad.
     * Por defecto NO permite refacturación y usa período mensual automático.
     */
    @Transactional
    public Factura ejecutarFacturacionIndividual(Long clienteId) {
        return ejecutarFacturacionIndividual(clienteId, false);
    }

    // ==================== LÓGICA COMÚN DE GENERACIÓN (LEGACY) ====================
    /**
     * Método legacy mantenido para retrocompatibilidad.
     * Genera factura con fecha actual sin validaciones AFIP.
     * 
     * @deprecated Usar generarFacturaConFechaEmision para cumplir normativa AFIP
     */
    @Deprecated
    private Factura generarFacturaParaCliente(Cliente cliente, List<ClienteServicio> servicios) {
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        
        // Asignamos A o B según la condición del cliente
        factura.setTipoComprobante(
            com.example.facturacion.modelo.enums.TipoComprobante.getTipoFactura(cliente.getCondicionFiscal())
        );
        
        BigDecimal totalFactura = BigDecimal.ZERO;

        for (ClienteServicio cs : servicios) {
            Servicio servicio = cs.getServicio();
            
            DetalleFactura detalle = new DetalleFactura();
            detalle.setServicio(servicio);
            detalle.setCantidad(1);
            
            // Setear Precio Base (Neto)
            detalle.setPrecioUnitario(servicio.getPrecio()); 
            
            // Setear Alícuota desde el Servicio
            detalle.setAlicuotaIva(BigDecimal.valueOf(servicio.getAlicuota().getValor()));

            // Calcular Impuestos y Totales
            detalle.calcularMontos();

            factura.agregarDetalle(detalle);
            totalFactura = totalFactura.add(detalle.getSubtotal());
        }

        factura.setTotal(totalFactura);
        
        factura = repositorioFactura.save(factura);

        // IMPACTO EN CUENTA CORRIENTE
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setFactura(factura); // Vincular factura para el link
        movimiento.setTipoMovimiento(TipoMovimiento.FACTURA);
        movimiento.setMonto(totalFactura);
        movimiento.setDescripcion("Factura " + factura.getTipoComprobante().getLetra() + " N° " + factura.getId());
        
        servicioCliente.registrarMovimiento(cliente.getId(), movimiento);

        return factura;
    }

    // ==================== ANULACIÓN ====================
    @Transactional
    public void anularFactura(Long facturaId) {
        // Delegamos la anulación al servicio de Notas de Crédito, que se encarga de:
        // 1. Validar que no esté anulada
        // 2. Crear la NC
        // 3. Marcar la factura como anulada
        // 4. Revertir el saldo en cuenta corriente
        servicioNotaCredito.crearNotaCreditoPorAnulacion(facturaId, "Anulación manual solicitada por usuario");
    }
    
    // ==================== CONSULTAS ====================
    @Transactional(readOnly = true)
    public Page<Factura> obtenerFacturasFiltradas(String busqueda, EstadoFactura estado,
                                                LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                                int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        if ((busqueda == null || busqueda.isEmpty()) && estado == null && fechaDesde == null && fechaHasta == null) {
            return repositorioFactura.findAll(pageable);
        }
        
        return repositorioFactura.buscarConFiltros(busqueda, estado, fechaDesde, fechaHasta, pageable);
    }
    
    @Transactional(readOnly = true)
    public Factura obtenerFacturaPorId(Long id) {
        return repositorioFactura.findById(id).orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
    }
}