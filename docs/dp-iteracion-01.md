# Trabajo en equipo


# Diseño OO

 ```mermaid
classDiagram
    %% Clases
    class Cliente {
        +Long id
        +String nombreRazonSocial
        +String cuit
        +String condicionFiscal
        +String domicilio
        +String email
        +String telefono
    }

    class Servicio {
        +Long id
        +String nombre
        +String descripcion
        +BigDecimal precioBase
        +BigDecimal alicuotaIVA
        +calcularPrecioConIVA()
    }

    class ClienteServicio {
        +Long id
        +int cantidadOpcional
        +LocalDate fechaVigenciaInicio
        +LocalDate fechaVigenciaFin
    }

    class Factura {
        +Long id
        +LocalDate fechaEmision
        +BigDecimal importeNeto
        +BigDecimal iva
        +BigDecimal total
        +calcularIVA()
        +calcularTotal()
        +agregarDetalle(DetalleFactura)
    }

    class DetalleFactura {
        +Long id
        +int cantidad
        +BigDecimal precioUnitario
        +BigDecimal subtotal
        +calcularSubtotal()
    }

    %% Relaciones
    Cliente "1" -- "0..*" Factura
    Factura "1" -- "0..*" DetalleFactura
    Servicio "1" -- "0..*" DetalleFactura

    Cliente "1" -- "0..*" ClienteServicio
    Servicio "1" -- "0..*" ClienteServicio
```

# Wireframe y flujo de interacción

## Wireframe conceptual

> Los wireframes conceptuales se encuentran en `img/wireframe-iteracion1.png`.

---

## Flujo general de interacción entre pantallas

| Paso | Acción del usuario | Respuesta del sistema |
|------|------------------|--------------------|
| 1 | Accede al menú principal | Se muestra menú con opciones: Clientes, Servicios, Facturación |
| 2 | Gestiona clientes | El sistema permite crear, modificar o eliminar clientes; cambios reflejados en lista de facturación |
| 3 | Gestiona servicios | El sistema permite crear, modificar o eliminar servicios; servicios activos disponibles para facturación |
| 4 | Genera factura individual | Selecciona cliente y servicios, el sistema calcula IVA y total automáticamente, confirma y guarda factura |
| 5 | Consulta facturas | Se muestran facturas generadas con cliente, servicios y montos correspondientes |

---

## Casos de uso principales

### Caso de uso 1: Crear/Modificar/Eliminar Cliente
- **Actor:** Usuario administrativo  
- **Flujo principal:**  
  1. Accede a pantalla de Clientes  
  2. Selecciona crear, modificar o eliminar cliente  
  3. Completa el formulario (en caso de alta/modificación) y confirma  
  4. Sistema guarda cambios y actualiza tabla  
- **Resultado esperado:** Cliente registrado y disponible para facturación

### Caso de uso 2: Crear/Modificar/Eliminar Servicio
- **Actor:** Usuario administrativo  
- **Flujo principal:**  
  1. Accede a pantalla de Servicios  
  2. Selecciona crear, modificar o eliminar servicio  
  3. Completa formulario y confirma  
  4. Sistema actualiza lista de servicios disponibles  
- **Resultado esperado:** Servicios disponibles reflejan los cambios y pueden ser seleccionados en facturación

## Caso de uso 3: Asignar servicios a clientes

**Actor:** Usuario administrativo

**Flujo principal:**
1. El usuario accede a la pantalla de Asignar Servicios a Clientes  
2. Selecciona un cliente de la lista.  
3. El sistema muestra los servicios disponibles.  
4. El usuario selecciona uno o más servicios que quiere asignar al cliente.  
5. El usuario confirma la operación.  
6. El sistema guarda la relación cliente-servicio en la base de datos (`ClienteServicio`) con información opcional como cantidad, fechas de vigencia o frecuencia. 
7. El sistema muestra un mensaje de éxito y actualiza la pantalla del cliente con los servicios asignados.
**Resultado esperado:**  
- El cliente queda asociado a los servicios seleccionados.  
- Esta información se utilizará posteriormente para facturación individual o masiva.


# Backlog de iteración 1

## Historias de usuario seleccionadas

### Gestión de Clientes
> Como usuario del sistema, quiero poder registrar, modificar y eliminar clientes con su condición fiscal para mantener actualizados los datos de los clientes.

### Gestión de Servicios
> Como usuario del sistema, quiero registrar y administrar los servicios ofrecidos por la empresa con su precio base y alícuota de IVA, para poder facturarlos correctamente.

### Asignación de Servicios a Clientes
> Como usuario del sistema, quiero asignar servicios a clientes para posteriormente poder realizar su facturación.

---

# Tareas

## Gestión de Clientes
### Tareas tentativas:
- [ ] Diseñar el modelo `Cliente` con atributos: id, nombre/razón social, CUIT, condición fiscal, domicilio, email, teléfono.
- [ ] Crear entidad JPA `Cliente` con Lombok.
- [ ] Crear repositorio `RepositorioCliente`.
- [ ] Implementar servicio `ServicioCliente` para alta, baja y modificación de clientes.
- [ ] Crear controlador `ControladorCliente`.
- [ ] Implementar validaciones: CUIT válido, campos obligatorios, condición fiscal correcta.
- [ ] Probar endpoints con Postman.

---

## Gestión de Servicios
### Tareas tentativas:
- [ ] Diseñar el modelo `Servicio` con atributos: id, nombre, descripción, precio base, alícuota IVA.
- [ ] Crear entidad JPA `Servicio` con Lombok.
- [ ] Crear repositorio `RepositorioServicio`.
- [ ] Implementar servicio `ServicioServicio` para gestionar servicios.
- [ ] Crear controlador `ControladorServicio`.
- [ ] Implementar validaciones: precio positivo, nombre no vacío.
- [ ] Probar endpoints con Postman.

---

## Asignación de Servicios a Clientes
### Tareas tentativas:
- [ ] Diseñar el modelo `ClienteServicio` con atributos: id, cliente, servicio, cantidadOpcional, fechaVigenciaInicio, fechaVigenciaFin.
- [ ] Crear entidad JPA `ClienteServicio` con relaciones a `Cliente` y `Servicio`.
- [ ] Implementar servicio `ServicioClienteServicio` para:
  - Asignar uno o más servicios a un cliente.
  - Registrar cantidad opcional y fechas de vigencia si corresponde.
  - Validar que el cliente y los servicios existen.
- [ ] Crear controlador `ControladorClienteServicio` para la interfaz de asignación.
- [ ] Probar asignación de servicios a clientes desde Postman o interfaz de prueba.
- [ ] Validar consistencia de datos (cliente existente, servicios válidos, relación correcta).
