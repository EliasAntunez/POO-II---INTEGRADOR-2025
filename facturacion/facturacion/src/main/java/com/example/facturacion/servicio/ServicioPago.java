package com.example.facturacion.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.MovimientoCuentaCorriente;
import com.example.facturacion.modelo.Pago;
import com.example.facturacion.modelo.enums.EstadoCliente;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.modelo.enums.MedioPago;
import com.example.facturacion.modelo.enums.TipoMovimiento;
import com.example.facturacion.repositorio.RepositorioFactura;
import com.example.facturacion.repositorio.RepositorioPago;

@Service
public class ServicioPago {

    @Autowired
    private RepositorioPago repositorioPago;

    @Autowired
    private RepositorioFactura repositorioFactura;

    @Autowired
    private ServicioCliente servicioCliente;

    /**
     * Registra un pago total o parcial.
     * Regla AFIP: "Si el pago se realiza... antes del vencimiento, la factura debe emitirse en ese mismo momento".
     * En este sistema, asumimos que la factura YA FUE EMITIDA (Estado EMITIDA) antes de llegar aquí.
     * * @param pago Datos del pago (monto, medio, observaciones)
     * @param facturaId ID de la factura a cancelar
     */
    @Transactional
    public Pago registrarPago(Pago pago, Long facturaId) {
        // 1. Recuperar Factura
        Factura factura = repositorioFactura.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        Cliente cliente = factura.getCliente();

        // 2. Validar Estado Cliente
        if (cliente.getEstado() == EstadoCliente.DADO_DE_BAJA) {
            throw new IllegalArgumentException("No se pueden registrar pagos de un cliente DADO DE BAJA.");
        }

        // 3. Validar Estado Factura
        if (factura.isAnulada() || factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalArgumentException("No se pueden registrar pagos a una factura ANULADA.");
        }

        // 4. Validar Saldos
        BigDecimal saldoPendiente = factura.getSaldoPendiente();
        
        // CORRECCIÓN: Usamos 'pago' en lugar de 'pagoNuevo'
        BigDecimal montoAPagar = pago.getMonto(); 

        if (montoAPagar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a 0.");
        }

        if (montoAPagar.compareTo(saldoPendiente) > 0) {
            throw new IllegalArgumentException("El pago excede el saldo pendiente de la factura ($" + saldoPendiente + ").");
        }

        if (LocalDate.now().isBefore(factura.getFechaVencimiento())) {
            // Lógica de pago anticipado (informativo)
        }

        // 5. Persistir Pago
        pago.setFactura(factura);
        pago.setCliente(cliente);
        pago.setFechaPago(LocalDateTime.now());
        
        // Guardamos y actualizamos la referencia
        pago = repositorioPago.save(pago); 

        // 6. Actualizar Factura (Monto Pagado y Estado)
        // CORRECCIÓN AQUÍ: Usamos pago.getMonto()
        BigDecimal nuevoAcumulado = factura.getMontoPagado().add(pago.getMonto());
        factura.setMontoPagado(nuevoAcumulado);

        if (factura.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            factura.setEstado(EstadoFactura.PAGADA);
        } else {
            factura.setEstado(EstadoFactura.PARCIALMENTE_PAGADA);
        }
        repositorioFactura.save(factura);

        // 7. Registrar Movimiento
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setTipoMovimiento(TipoMovimiento.PAGO);
        
        // CORRECCIÓN: Usamos pago.getMonto()
        movimiento.setMonto(pago.getMonto()); 
        
        // CORRECCIÓN: Usamos la entidad pago guardada
        movimiento.setPago(pago); 
        
        movimiento.setFactura(factura);
        movimiento.setDescripcion("Pago " + (factura.getEstado() == EstadoFactura.PAGADA ? "Total" : "Parcial") + 
                                  " Fac #" + factura.getId() + " (" + pago.getMedioPago().getDescripcion() + ")");
        movimiento.setUsuarioRegistro("SISTEMA");
        movimiento.setFechaMovimiento(LocalDateTime.now());

        servicioCliente.registrarMovimiento(cliente.getId(), movimiento);

        return pago;
    }
    
    /**
     * Caso especial: Facturación por anticipos.
     * "Si se trata de anticipos que fijan precio, la fecha de emisión debe ser el día en que se percibe el pago"
     * Este método sería para recibir dinero SIN factura previa (se crea acá).
     */
    @Transactional
    public void registrarAnticipo(Long clienteId, BigDecimal monto, MedioPago medio) {
        // 1. Crear Factura por el anticipo (Fecha Emisión = HOY, Regla AFIP)
        // 2. Registrar el Pago sobre esa factura inmediatamente.
        // (Implementación pendiente para próxima iteración).
    }

    @Transactional(readOnly = true)
    public java.util.List<Pago> obtenerPagosDeFactura(Long facturaId) {
        return repositorioPago.findByFacturaId(facturaId);
    }
    
    @Transactional(readOnly = true)
    public Pago obtenerPagoPorId(Long id) {
        return repositorioPago.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
    }
}