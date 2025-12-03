package com.example.facturacion.controlador;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.facturacion.modelo.Cliente;
import com.example.facturacion.modelo.enums.CondicionFiscal;
import com.example.facturacion.modelo.enums.CondicionPago;
import com.example.facturacion.modelo.enums.EstadoCliente;
import com.example.facturacion.servicio.ServicioCliente;

@ExtendWith(MockitoExtension.class)
class ControladorClienteTest {

    @Mock
    private ServicioCliente servicioCliente;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ControladorCliente controladorCliente;

    private Cliente clienteTest;

    @BeforeEach
    public void setUp() {
        clienteTest = Cliente.builder()
            .id(1L)
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
    }
    /**
     * este test verifica la lista de clientes en el controlador
     */
    @Test
    void testListarClientes() {
        List<Cliente> clientes = Arrays.asList(clienteTest);
        Page<Cliente> page = new PageImpl<>(clientes);
        
        when(servicioCliente.obtenerTodosLosClientesPaginados(0, 10)).thenReturn(page);
        
        String vista = controladorCliente.mostrarListaClientes(model, 0, 10, null, null);
        
        assertEquals("clientes/listar", vista);
        verify(model).addAttribute(eq("clientes"), any());
    }
    /**
    * este test verifica la visualización del formulario para crear un cliente
    */
    @Test
    void testMostrarFormularioCrear() {
        
        String vista = controladorCliente.mostrarFormularioCrearCliente(model);
        
        assertEquals("clientes/crear", vista);
        verify(model).addAttribute(eq("cliente"), any(Cliente.class));
    }
    /**
     * este test verifica el guardado de un cliente en el controlador
     */
    @Test
    void testGuardarCliente() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(servicioCliente.guardarCliente(any(Cliente.class))).thenReturn(clienteTest);

        String vista = controladorCliente.crearCliente(clienteTest, bindingResult, model, redirectAttributes);
        
        assertEquals("redirect:/clientes/listar", vista);
        verify(servicioCliente).guardarCliente(clienteTest);
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
    }
    /**
     * este test verifica el guardado de un cliente con errores de validación
     */
    @Test
    void testGuardarClienteConErrores() {
        when(bindingResult.hasErrors()).thenReturn(true);
        

        String vista = controladorCliente.crearCliente(clienteTest, bindingResult, model, redirectAttributes);
        
        assertEquals("clientes/crear", vista);

        verify(servicioCliente, never()).guardarCliente(any());
    }
    /**
     * este test verifica la visualización del formulario para modificar un cliente
     */
    @Test
    void testMostrarFormularioModificar() {
        when(servicioCliente.obtenerClientePorId(1L)).thenReturn(clienteTest);
        
        String vista = controladorCliente.mostrarFormularioModificarCliente(1L, model, redirectAttributes);
        
        assertEquals("clientes/modificar", vista);
        verify(model).addAttribute("cliente", clienteTest);
    }
    /**
     * este test verifica la actualización de un cliente en el controlador
     */
    @Test
    void testActualizarCliente() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(servicioCliente.actualizarCliente(any(Cliente.class))).thenReturn(clienteTest);
        
        String vista = controladorCliente.modificarCliente(clienteTest, bindingResult, model, redirectAttributes);
        
        assertEquals("redirect:/clientes/listar", vista);
        verify(servicioCliente).actualizarCliente(clienteTest);
    }
    /**
     * este test verifica la actualización de un cliente con errores de validación
     */
    @Test
    void testDarDeBajaCliente() {
        doNothing().when(servicioCliente).darDeBajaClientePorId(1L);
        

        String vista = controladorCliente.eliminarPorPath(1L, redirectAttributes);
        
        assertEquals("redirect:/clientes/listar", vista);
        verify(servicioCliente).darDeBajaClientePorId(1L);
        verify(redirectAttributes).addFlashAttribute(eq("exito"), anyString());
    }
    /**
     * este test verifica la reactivación de un cliente en el controlador
     */
    @Test
    void testReactivarCliente() {
        doNothing().when(servicioCliente).reactivarCliente(1L);
        
        String vista = controladorCliente.reactivarCliente(1L, redirectAttributes);
        
        assertEquals("redirect:/clientes/listar", vista);
        verify(servicioCliente).reactivarCliente(1L);
    }
    /**
     * este test verifica la visualización de la cuenta corriente de un cliente
     */
    @Test
    void testVerCuentaCorriente() {
        when(servicioCliente.obtenerClientePorId(1L)).thenReturn(clienteTest);
        when(servicioCliente.obtenerMovimientosCliente(1L)).thenReturn(Arrays.asList());
        
        String vista = controladorCliente.verCuentaCorriente(1L, model, redirectAttributes);
        
        assertEquals("clientes/cuenta-corriente", vista);
        verify(model).addAttribute("cliente", clienteTest);
        verify(model).addAttribute(eq("movimientos"), any());
    }
}