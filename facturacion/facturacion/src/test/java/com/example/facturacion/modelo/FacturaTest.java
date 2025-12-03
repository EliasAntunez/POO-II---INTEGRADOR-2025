package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.facturacion.modelo.enums.EstadoFactura;
import com.example.facturacion.modelo.enums.TipoComprobante;

class FacturaTest {
    /**
     * este test verifica la creación de una Factura
    */
    @Test
    void testCrearFactura() {
        Cliente cliente = Cliente.builder()
            .nombre("Juan")
            .apellido("Pérez")
            .build();
        
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setFechaVencimiento(LocalDate.now().plusDays(30));
        factura.setTipoComprobante(TipoComprobante.FACTURA_A);
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        factura.setTotal(new BigDecimal("1000.00"));
        
        assertNotNull(factura);
        assertEquals(cliente, factura.getCliente());
        assertEquals(TipoComprobante.FACTURA_A, factura.getTipoComprobante());
        assertEquals(EstadoFactura.PENDIENTE_PAGO, factura.getEstado());
    }
    /**
     * este test verifica la adición de un DetalleFactura a una Factura
    */
    @Test
    void testAgregarDetalleFactura() {
        Factura factura = new Factura();
        
        Servicio servicio = new Servicio();
        servicio.setNombre("Internet");
        servicio.setPrecio(new BigDecimal("1000.00"));

        DetalleFactura detalle = new DetalleFactura();
        detalle.setServicio(servicio);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("1000.00"));
        detalle.setAlicuotaIva(new BigDecimal("21"));
        
        factura.agregarDetalle(detalle);
        
        assertEquals(1, factura.getDetalles().size());
        assertEquals(detalle.getFactura(), factura);
    }
    /**
     * este test verifica el marcado de una Factura como pagada
    */
    @Test
    void testMarcarFacturaComoPagada() {
        Factura factura = new Factura();
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);

        // Usamos el setter proporcionado por Lombok.
        factura.setEstado(EstadoFactura.PAGADA);
        
        assertEquals(EstadoFactura.PAGADA, factura.getEstado());
    }
    /**
     * este test verifica la anulación de una Factura
    */
    @Test
    void testAnularFactura() {
        Factura factura = new Factura();
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);

        // Actualizamos el estado y la bandera booleana manualmente.
        factura.setEstado(EstadoFactura.ANULADA);
        factura.setAnulada(true);
        
        assertEquals(EstadoFactura.ANULADA, factura.getEstado());
        assertTrue(factura.isAnulada());
    }
}