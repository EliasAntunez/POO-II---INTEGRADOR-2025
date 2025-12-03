package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DetalleNotaCreditoTest {
    /**
     * este test verifica la creación de un DetalleNotaCredito
    */
    @Test
    void testCrearDetalleNotaCredito() {
        // 1. Creamos el servicio simulado, ya que el detalle depende de él para la descripción
        Servicio servicio = new Servicio();
        servicio.setNombre("Devolución servicio Internet");

        DetalleNotaCredito detalle = new DetalleNotaCredito();
        
        // asignamos el objeto Servicio
        detalle.setServicio(servicio);
        
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("1000.00"));
        detalle.setSubtotal(new BigDecimal("1000.00"));
        
        assertNotNull(detalle);
        
        // Verificamos que el servicio asignado es el correcto
        assertEquals(servicio, detalle.getServicio());
        
        // Si queremos validar el texto, lo hacemos a través del servicio
        assertEquals("Devolución servicio Internet", detalle.getServicio().getNombre());
        
        assertEquals(1, detalle.getCantidad());
        assertEquals(new BigDecimal("1000.00"), detalle.getPrecioUnitario());
        assertEquals(new BigDecimal("1000.00"), detalle.getSubtotal());
    }
}