package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ServicioTest {
    /**
     * este test verifica la creación de un Servicio
    */
    @Test
    void testCrearServicio() {
        Servicio servicio = new Servicio();
        servicio.setNombre("Internet");
        servicio.setDescripcion("Servicio de Internet 100Mbps");
        servicio.setPrecio(new BigDecimal("2500.00"));
        // servicio.setAlicuota(Alicuota.VEINTIUNO); // ERROR: Valor desconocido en el Enum
        servicio.setActivo(true);
        
        assertNotNull(servicio);
        assertEquals("Internet", servicio.getNombre());
        assertEquals("Servicio de Internet 100Mbps", servicio.getDescripcion());
        assertEquals(new BigDecimal("2500.00"), servicio.getPrecio());
        assertTrue(servicio.isActivo());
    }
    /**
     * este test verifica el estado activo por defecto en Servicio
    */
    @Test
    void testServicioActivoPorDefecto() {
        Servicio servicio = new Servicio();
        assertTrue(servicio.isActivo());
    }
    /**
     * este test verifica la desactivación de un Servicio
    */
    @Test
    void testDesactivarServicio() {
        Servicio servicio = new Servicio();
        servicio.setActivo(false);
        
        assertFalse(servicio.isActivo());
    }
}