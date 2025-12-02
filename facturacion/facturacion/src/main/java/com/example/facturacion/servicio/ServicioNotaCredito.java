package com.example.facturacion.servicio;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.facturacion.modelo.DetalleFactura;
import com.example.facturacion.modelo.DetalleNotaCredito;
import com.example.facturacion.modelo.Factura;
import com.example.facturacion.modelo.MovimientoCuentaCorriente;
import com.example.facturacion.modelo.NotaCredito;
import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.repositorio.RepositorioFactura;
import com.example.facturacion.repositorio.RepositorioNotaCredito;

@Service
public class ServicioNotaCredito {

    @Autowired
    private RepositorioNotaCredito repositorioNotaCredito;

    @Autowired
    private RepositorioFactura repositorioFactura;

    @Autowired
    private ServicioCliente servicioCliente;

    /**
     * Genera una Nota de Crédito por anulación total de factura.
     */
    @Transactional
    public NotaCredito crearNotaCreditoPorAnulacion(Long facturaId, String motivo) {
        Factura factura = repositorioFactura.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (factura.isAnulada()) {
            throw new IllegalArgumentException("La factura ya se encuentra anulada.");
        }

        // 1. Crear Cabecera de Nota de Crédito
        NotaCredito nc = new NotaCredito();
        nc.setFactura(factura);
        nc.setCliente(factura.getCliente());
        nc.setFechaEmision(LocalDateTime.now());
        nc.setTotal(factura.getTotal());
        nc.setMotivo(motivo);

        // 2. Copiar Detalles (Espejo)
        for (DetalleFactura df : factura.getDetalles()) {
            DetalleNotaCredito dnc = new DetalleNotaCredito();
            dnc.setServicio(df.getServicio());
            dnc.setCantidad(df.getCantidad());
            dnc.setPrecioUnitario(df.getPrecioUnitario());
            dnc.setAlicuotaIva(df.getAlicuotaIva());
            dnc.setMontoIva(df.getMontoIva());
            dnc.setSubtotal(df.getSubtotal());
            
            nc.agregarDetalle(dnc);
        }

        // 3. Guardar NC
        nc = repositorioNotaCredito.save(nc);

        // 4. Actualizar Estado Factura
        factura.setAnulada(true);
        factura.setEstado(EstadoFactura.ANULADA);
        repositorioFactura.save(factura);

        // 5. Impactar Cuenta Corriente (Reversión)
        // Usamos tu método estático porAnulacion en MovimientoCuentaCorriente
        MovimientoCuentaCorriente movimiento = MovimientoCuentaCorriente.porAnulacion(
                factura.getCliente(), 
                nc, 
                "ADMIN" // Usuario hardcodeado por ahora
        );
        
        servicioCliente.registrarMovimiento(factura.getCliente().getId(), movimiento);

        return nc;
    }

    @Transactional(readOnly = true)
    public NotaCredito obtenerPorFactura(Long facturaId) {
        // Buscamos la NC directamente por el ID de la factura para evitar problemas con proxies
        NotaCredito nc = repositorioNotaCredito.findByFacturaId(facturaId) 
                .orElseThrow(() -> new IllegalArgumentException("No existe Nota de Crédito para la factura " + facturaId));
        
        // Truco para inicializar la lista Lazy dentro de la transacción (Para que la vista no falle)
        nc.getDetalles().size(); 
        
        return nc;
    }
    
    @Transactional(readOnly = true)
    public NotaCredito obtenerPorId(Long id) {
        return repositorioNotaCredito.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nota de Crédito no encontrada"));
    }
}