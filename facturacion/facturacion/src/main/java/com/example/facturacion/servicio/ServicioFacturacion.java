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

    @Autowired
    private ServicioServicio servicioServicio;

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
        factura.setEstado(EstadoFactura.EMITIDA);
        
        BigDecimal totalFactura = BigDecimal.ZERO;

        for (ClienteServicio cs : servicios) {
            Servicio servicio = cs.getServicio();
            
            DetalleFactura detalle = new DetalleFactura();
            detalle.setServicio(servicio);
            detalle.setCantidad(1);
            
            // 1. Setear Precio Base (Neto)
            // CORRECCIÓN: Como servicio.getPrecio() ya es BigDecimal, lo asignamos directo.
            detalle.setPrecioUnitario(servicio.getPrecio()); 
            
            // 2. Setear Alícuota desde el Servicio
            // CORRECCIÓN: Usamos getValor() (que devuelve double) y lo convertimos a BigDecimal aquí.
            detalle.setAlicuotaIva(BigDecimal.valueOf(servicio.getAlicuota().getValor()));

            // 3. Calcular Impuestos y Totales
            detalle.calcularMontos();

            factura.agregarDetalle(detalle);
            totalFactura = totalFactura.add(detalle.getSubtotal());
        }

        factura.setTotal(totalFactura);
        factura = repositorioFactura.save(factura);

        // IMPACTO EN CUENTA CORRIENTE
        // CORRECCIÓN: Usamos TipoMovimiento.FACTURA que ya tiene un 'DEBE' implícito en tu lógica
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setTipoMovimiento(TipoMovimiento.FACTURA);
        movimiento.setMonto(totalFactura);
        movimiento.setDescripcion("Factura N° " + factura.getId());
        
        servicioCliente.registrarMovimiento(cliente.getId(), movimiento);

        return factura;
    }

    // ==================== ANULACIÓN ====================
    @Transactional
    public void anularFactura(Long facturaId) {
        Factura factura = repositorioFactura.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (factura.isAnulada()) {
            throw new IllegalArgumentException("La factura ya está anulada.");
        }

        factura.setAnulada(true);
        // CORRECCIÓN: Usamos tu estado 'ANULADA'
        factura.setEstado(EstadoFactura.ANULADA);
        repositorioFactura.save(factura);

        // IMPACTO EN CUENTA CORRIENTE (REVERSIÓN)
        // CORRECCIÓN: Usamos TipoMovimiento.ANULACION
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setTipoMovimiento(TipoMovimiento.ANULACION); 
        movimiento.setMonto(factura.getTotal()); // Monto positivo, el método registrarMovimiento debe restar si es ANULACION
        movimiento.setDescripcion("Anulación Factura N° " + factura.getId());
        
        servicioCliente.registrarMovimiento(factura.getCliente().getId(), movimiento);
    }
    
    // ==================== CONSULTAS ====================
    @Transactional(readOnly = true)
    public Page<Factura> obtenerFacturasPaginadas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaEmision"));
        return repositorioFactura.findAllByOrderByFechaEmisionDesc(pageable);
    }
    
    @Transactional(readOnly = true)
    public Factura obtenerFacturaPorId(Long id) {
        return repositorioFactura.findById(id).orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
    }
}