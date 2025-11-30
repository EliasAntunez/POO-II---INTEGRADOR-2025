package com.example.facturacion.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.ClienteServicio;
import com.example.facturacion.modelo.DetalleFactura;
import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.MovimientoCuentaCorriente;
import com.example.facturacion.modelo.Servicio;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.modelo.enums.TipoMovimiento;
import com.example.facturacion.repositorio.RepositorioClienteServicio;
import com.example.facturacion.repositorio.RepositorioFactura;

@Service
public class ServicioFacturacion {

    @Autowired
    private RepositorioFactura repositorioFactura;

    @Autowired
    private RepositorioClienteServicio repositorioClienteServicio;

    @Autowired
    private ServicioCliente servicioCliente;

    // ==================== PROCESO MASIVO ====================
    @Transactional
    public int ejecutarFacturacionMasiva(LocalDate inicio, LocalDate fin) {
        List<ClienteServicio> contratosActivos = repositorioClienteServicio.findByActivoTrue();

        Map<Cliente, List<ClienteServicio>> serviciosPorCliente = contratosActivos.stream()
                .collect(Collectors.groupingBy(ClienteServicio::getCliente));

        int facturasGeneradas = 0;

        for (Map.Entry<Cliente, List<ClienteServicio>> entry : serviciosPorCliente.entrySet()) {
            Cliente cliente = entry.getKey();
            List<ClienteServicio> serviciosDelCliente = entry.getValue();

            // Usamos el método de tu Enum EstadoCliente para verificar
            if (cliente.getEstado().esActivo()) { 
                generarFacturaParaCliente(cliente, serviciosDelCliente);
                facturasGeneradas++;
            }
        }
        return facturasGeneradas;
    }

    // ==================== PROCESO INDIVIDUAL ====================
    @Transactional
    public Factura ejecutarFacturacionIndividual(Long clienteId) {
        Cliente cliente = servicioCliente.obtenerClientePorId(clienteId);
        
        List<ClienteServicio> serviciosDelCliente = repositorioClienteServicio.findByActivoTrue().stream()
                .filter(cs -> cs.getCliente().getId().equals(clienteId))
                .collect(Collectors.toList());

        if (serviciosDelCliente.isEmpty()) {
            throw new IllegalArgumentException("El cliente no tiene servicios activos contratados para facturar.");
        }

        return generarFacturaParaCliente(cliente, serviciosDelCliente);
    }

    // ==================== LÓGICA COMÚN DE GENERACIÓN ====================
    private Factura generarFacturaParaCliente(Cliente cliente, List<ClienteServicio> servicios) {
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        
        // === CORRECCIÓN FUNDAMENTAL ===
        // Asignamos A o B según la condición del cliente (Responsable Inscripto vs resto)
        // Asegúrate de importar: com.example.facturacion.modelo.enums.TipoComprobante
        factura.setTipoComprobante(
            com.example.facturacion.modelo.enums.TipoComprobante.getTipoFactura(cliente.getCondicionFiscal())
        );
        
        BigDecimal totalFactura = BigDecimal.ZERO;

        for (ClienteServicio cs : servicios) {
            Servicio servicio = cs.getServicio();
            
            DetalleFactura detalle = new DetalleFactura();
            detalle.setServicio(servicio);
            detalle.setCantidad(1);
            
            // 1. Setear Precio Base (Neto)
            detalle.setPrecioUnitario(servicio.getPrecio()); 
            
            // 2. Setear Alícuota desde el Servicio
            // Convertimos el double del Enum a BigDecimal
            detalle.setAlicuotaIva(BigDecimal.valueOf(servicio.getAlicuota().getValor()));

            // 3. Calcular Impuestos y Totales
            detalle.calcularMontos();

            factura.agregarDetalle(detalle);
            totalFactura = totalFactura.add(detalle.getSubtotal());
        }

        factura.setTotal(totalFactura);
        
        // Ahora sí guardará sin error porque tipo_comprobante ya tiene valor
        factura = repositorioFactura.save(factura);

        // IMPACTO EN CUENTA CORRIENTE
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setTipoMovimiento(TipoMovimiento.FACTURA);
        movimiento.setMonto(totalFactura);
        
        // Usamos el ID generado o el getter de compatibilidad
        movimiento.setDescripcion("Factura " + factura.getTipoComprobante().getLetra() + " N° " + factura.getId());
        
        servicioCliente.registrarMovimiento(cliente.getId(), movimiento);

        return factura;
    }

    // ==================== ANULACIÓN ====================
    @Transactional
    public void anularFactura(Long facturaId) {
        Factura factura = repositorioFactura.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        // Validación 1: Que no esté ya anulada
        if (factura.isAnulada()) {
            throw new IllegalArgumentException("La factura ya está anulada.");
        }

        // === NUEVA VALIDACIÓN (REGLA DE NEGOCIO) ===
        // Si el monto pagado es mayor a 0, bloqueamos la anulación directa.
        if (factura.getMontoPagado().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("No se puede anular una factura que tiene pagos registrados. Debe anular los pagos primero o realizar una nota de crédito de ajuste.");
        }

        factura.setAnulada(true);
        factura.setEstado(EstadoFactura.ANULADA);
        repositorioFactura.save(factura);

        // IMPACTO EN CUENTA CORRIENTE (REVERSIÓN)
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setTipoMovimiento(TipoMovimiento.ANULACION);
        movimiento.setMonto(factura.getTotal());
        movimiento.setDescripcion("Anulación Factura N° " + factura.getId());
        
        servicioCliente.registrarMovimiento(factura.getCliente().getId(), movimiento);
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