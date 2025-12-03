package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DetalleFacturaTest {
    /**
     * este test verifica la creación de un DetalleFactura
     */
    @Test
    void testCrearDetalleFactura() {
        Servicio servicio = new Servicio();
        servicio.setNombre("Internet");
        servicio.setPrecio(new BigDecimal("1000.00"));
        
        DetalleFactura detalle = new DetalleFactura();
        detalle.setServicio(servicio);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("1000.00"));
        detalle.setAlicuotaIva(new BigDecimal("21"));
        
        assertNotNull(detalle);
        assertEquals(servicio, detalle.getServicio());
        assertEquals(1, detalle.getCantidad());
        assertEquals(new BigDecimal("1000.00"), detalle.getPrecioUnitario());
    }
    /**
     * este test verifica el cálculo de montos
     */
    @Test
    void testCalcularMontos() {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(new BigDecimal("1000.00"));
        detalle.setAlicuotaIva(new BigDecimal("21"));
        detalle.calcularMontos();
        
        BigDecimal baseEsperada = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
        assertEquals(new BigDecimal("2000.00"), baseEsperada);
        
        // IVA: 2000 * 0.21 = 420
        assertEquals(new BigDecimal("420.00"), detalle.getMontoIva());
        
        // Subtotal: 2000 + 420 = 2420
        assertEquals(new BigDecimal("2420.00"), detalle.getSubtotal());
    }
}