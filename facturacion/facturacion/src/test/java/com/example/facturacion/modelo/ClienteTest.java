package com.example.facturacion.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.facturacion.modelo.enums.CondicionFiscal;
import com.example.facturacion.modelo.enums.CondicionPago;
import com.example.facturacion.modelo.enums.EstadoCliente;

class ClienteTest {
    /**
     * este test verifica la creación de un Cliente
    */
    @Test
    void testCrearCliente() {
        Cliente cliente = Cliente.builder()
            .dni("12345678")
            .nombre("Juan")
            .apellido("Pérez")
            .razonSocial("Juan Pérez")
            .cuit("20123456789")
            .email("juan@example.com")
            .telefono("1234567890")
            .direccion("Calle Falsa 123")
            .condicionFiscal(CondicionFiscal.RESPONSABLE_INSCRIPTO)
            .condicionPago(CondicionPago.CUENTA_CORRIENTE_30)
            .estado(EstadoCliente.ACTIVO)
            .saldoCuentaCorriente(BigDecimal.ZERO)
            .build();
        
        assertNotNull(cliente);
        assertEquals("12345678", cliente.getDni());
        assertEquals("Juan", cliente.getNombre());
        assertEquals("Pérez", cliente.getApellido());
        assertEquals(EstadoCliente.ACTIVO, cliente.getEstado());
    }
    /**
     * este test verifica si el cliente tiene deuda (saldoCuentaCorriente > 0)
    */
    @Test
    void testClienteTieneDeuda() {
        Cliente cliente = Cliente.builder()
            .saldoCuentaCorriente(new BigDecimal("100.00")) 
            .build();
        
        assertTrue(cliente.tieneDeuda(), "El cliente debería tener deuda si el saldo es positivo");
    }
    /**
     * este test verifica si el cliente no tiene deuda (saldoCuentaCorriente == 0)
    */
    @Test
    void testClienteSinDeuda() {
        Cliente cliente = Cliente.builder()
            .saldoCuentaCorriente(BigDecimal.ZERO)
            .build();
        
        assertFalse(cliente.tieneDeuda(), "El cliente no debería tener deuda con saldo 0");
    }
    /**
     * este test verifica el método activar cliente
    */
    @Test
    void testActivarCliente() {
        Cliente cliente = Cliente.builder()
            .estado(EstadoCliente.SUSPENDIDO)
            .build();
        
        cliente.activar();
        assertEquals(EstadoCliente.ACTIVO, cliente.getEstado());
        assertTrue(cliente.isActivo(), "El booleano activo debe ser true tras activar");
    }
    /**
     * este test verifica el método dar de baja cliente
    */
    @Test
    void testDarDeBajaCliente() {
        Cliente cliente = Cliente.builder()
            .estado(EstadoCliente.ACTIVO)
            .build();
        
        cliente.darDeBaja();
        assertEquals(EstadoCliente.DADO_DE_BAJA, cliente.getEstado());
        assertFalse(cliente.isActivo(), "El booleano activo debe ser false tras dar de baja");
    }
}