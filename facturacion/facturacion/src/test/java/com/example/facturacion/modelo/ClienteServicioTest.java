package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;


class ClienteServicioTest {
    /**
     * este test verifica la creación de un ClienteServicio
     */
    
    @Test
    void testCrearClienteServicio() {
        Cliente cliente = Cliente.builder()
            .nombre("Juan")
            .apellido("Pérez")
            .build();
        
        Servicio servicio = new Servicio();
        servicio.setNombre("Internet");
        servicio.setPrecio(new BigDecimal("2500.00"));
        
        ClienteServicio cs = ClienteServicio.builder()
            .cliente(cliente)
            .servicio(servicio)
            .activo(true)
            .fechaAsignacion(LocalDate.now())
            .estaFacturado(false)
            .build();
        
        assertNotNull(cs);
        assertTrue(cs.isActivo());
        assertFalse(cs.isEstaFacturado());
        assertEquals(cliente, cs.getCliente());
        assertEquals(servicio, cs.getServicio());
    }
    /**
     * este test verifica el cambio de estado de facturado en ClienteServicio
     */
    @Test
    void testCambiarEstadoFacturado() {
        ClienteServicio cs = ClienteServicio.builder()
            .estaFacturado(false)
            .build();
        
        cs.cambiarEstadoFacturado(true);
        assertTrue(cs.isEstaFacturado());
    }
    /**
     * este test verifica los métodos de activar y desactivar en ClienteServicio
     */
    @Test
    void testActivarDesactivarClienteServicio() {
        ClienteServicio cs = ClienteServicio.builder()
            .activo(true)
            .build();
        
        cs.desactivar();
        assertFalse(cs.isActivo());
        
        cs.activar();
        assertTrue(cs.isActivo());
    }
    /**
     * este test verifica el método getPrecio en ClienteServicio
     */
    @Test
    void testGetPrecioPersonalizado() {
        Servicio servicio = new Servicio();
        servicio.setPrecio(new BigDecimal("1000.00"));
        ClienteServicio cs = ClienteServicio.builder()
            .servicio(servicio)
            .precio(new BigDecimal("1500.00"))
            .build();
        
        assertEquals(new BigDecimal("1500.00"), cs.getPrecio());
    }
    /**
     * este test verifica el método getPrecio cuando no hay precio personalizado
     */
    @Test
    void testGetPrecioDelServicio() {
        Servicio servicio = new Servicio();
        servicio.setPrecio(new BigDecimal("1000.00"));
        ClienteServicio cs = ClienteServicio.builder()
            .servicio(servicio)
            .precio(null)
            .build();
        
        assertEquals(new BigDecimal("1000.00"), cs.getPrecio());
    }
}