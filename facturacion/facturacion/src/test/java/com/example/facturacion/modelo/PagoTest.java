package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.facturacion.modelo.enums.MedioPago;

class PagoTest {
    /**
     * este test verifica la creación de un Pago
    */
    @Test
    void testCrearPago() {
        Cliente cliente = Cliente.builder()
            .nombre("Juan")
            .apellido("Pérez")
            .build();
        
        Pago pago = new Pago();
        pago.setCliente(cliente);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMonto(new BigDecimal("1000.00"));
        pago.setMedioPago(MedioPago.EFECTIVO);
        
        assertNotNull(pago);
        assertEquals(cliente, pago.getCliente());
        assertEquals(new BigDecimal("1000.00"), pago.getMonto());
        assertEquals(MedioPago.EFECTIVO, pago.getMedioPago());
    }
    /**
     * este test verifica la asociación de un Pago con una Factura
    */
    @Test
    void testPagoConFactura() {
        Factura factura = new Factura();
        factura.setTotal(new BigDecimal("5000.00"));
        
        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(new BigDecimal("5000.00"));
        
        assertNotNull(pago.getFactura());
        assertEquals(factura, pago.getFactura());
    }
}