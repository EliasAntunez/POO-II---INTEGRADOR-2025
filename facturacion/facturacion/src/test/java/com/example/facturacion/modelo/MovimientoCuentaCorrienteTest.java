package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.facturacion.modelo.enums.TipoMovimiento;

class MovimientoCuentaCorrienteTest {
    /**
     * este test verifica la creación de un MovimientoCuentaCorriente
    */
    @Test
    void testCrearMovimiento() {
        Cliente cliente = Cliente.builder()
            .nombre("Juan")
            .apellido("Pérez")
            .build();
        
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setCliente(cliente);
        movimiento.setTipoMovimiento(TipoMovimiento.FACTURA);
        movimiento.setMonto(new BigDecimal("1000.00"));
        movimiento.setDescripcion("Factura A N° 123");
        movimiento.setFechaMovimiento(LocalDateTime.now());
        
        assertNotNull(movimiento);
        assertEquals(cliente, movimiento.getCliente());
        assertEquals(TipoMovimiento.FACTURA, movimiento.getTipoMovimiento());
        assertEquals(new BigDecimal("1000.00"), movimiento.getMonto());
        assertEquals("Factura A N° 123", movimiento.getDescripcion());
    }
    /**
     * este test verifica el tipo de movimiento en MovimientoCuentaCorriente
    */
    @Test
    void testMovimientoPago() {
        MovimientoCuentaCorriente movimiento = new MovimientoCuentaCorriente();
        movimiento.setTipoMovimiento(TipoMovimiento.PAGO);
        movimiento.setMonto(new BigDecimal("500.00"));
        
        assertEquals(TipoMovimiento.PAGO, movimiento.getTipoMovimiento());
    }
}
