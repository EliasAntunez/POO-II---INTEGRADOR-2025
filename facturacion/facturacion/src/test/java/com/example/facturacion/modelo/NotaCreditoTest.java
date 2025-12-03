package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.facturacion.modelo.enums.TipoComprobante;

class NotaCreditoTest {
    /**
     * este test verifica la creación de una NotaCredito
    */
    @Test
    void testCrearNotaCredito() {
        Cliente cliente = Cliente.builder()
            .nombre("Juan")
            .apellido("Pérez")
            .build();
        
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setTotal(new BigDecimal("1000.00"));
        
        NotaCredito nc = new NotaCredito();
 
        // usamos los set y get de Lombok
        nc.setFactura(factura);
        
        nc.setFechaEmision(LocalDateTime.now());
        nc.setMotivo("Anulación de factura");
        nc.setTotal(new BigDecimal("1000.00"));
        nc.setTipoComprobante(TipoComprobante.NOTA_CREDITO_A);
        
        assertNotNull(nc);

        assertEquals(factura, nc.getFactura());
        
        assertEquals("Anulación de factura", nc.getMotivo());
        assertEquals(new BigDecimal("1000.00"), nc.getTotal());
        assertEquals(TipoComprobante.NOTA_CREDITO_A, nc.getTipoComprobante());
    }
}