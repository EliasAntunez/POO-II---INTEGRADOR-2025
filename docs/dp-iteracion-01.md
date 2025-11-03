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

### Caso de uso 3: Generar Factura Individual
- **Actor:** Usuario administrativo  
- **Flujo principal:**  
  1. Accede a pantalla de Facturación  
  2. Selecciona cliente  
  3. Selecciona uno o más servicios  
  4. Sistema calcula automáticamente importe neto, IVA y total  
  5. Usuario confirma factura  
- **Resultado esperado:** Factura generada con número único, asociada al cliente y servicios


# Backlog de iteración 1

## Historias de usuario seleccionadas

### Historia de Usuario 1: Gestión de Clientes
> Como usuario del sistema, quiero poder registrar, modificar y eliminar clientes con su condición fiscal para mantener actualizada la base de clientes.

### Historia de Usuario 2: Gestión de Servicios
> Como usuario del sistema, quiero registrar y administrar los servicios ofrecidos por la empresa con su precio base y alícuota de IVA, para poder facturarlos correctamente.

### Historia de Usuario 3: Facturación Individual
> Como usuario del sistema, quiero generar facturas individuales asociadas a un cliente y servicios seleccionados, de manera que el sistema calcule el IVA y el total automáticamente.

---

# Tareas

## Historia de Usuario 1
### Tareas tentativas:
- [ ] Diseñar el modelo `Cliente` con atributos: id, nombre/razón social, CUIT, condición fiscal, domicilio, email, teléfono.
- [ ] Crear entidad JPA `Cliente` con Lombok.
- [ ] Crear repositorio `RepositorioCliente`.
- [ ] Implementar servicio `ServicioCliente` para alta, baja y modificación de clientes.
- [ ] Crear controlador `ControladorCliente`.
- [ ] Implementar validaciones: CUIT válido, campos obligatorios, condición fiscal correcta.
- [ ] Probar endpoints con Postman.

---

## Historia de Usuario 2
### Tareas tentativas:
- [ ] Diseñar el modelo `Servicio` con atributos: id, nombre, descripción, precio base, alícuota IVA.
- [ ] Crear entidad JPA `Servicio` con Lombok.
- [ ] Crear repositorio `RepositorioServicio`.
- [ ] Implementar servicio `ServicioServicio` para gestionar servicios.
- [ ] Crear controlador `ControladorServicio`.
- [ ] Implementar validaciones: precio positivo, nombre no vacío.
- [ ] Probar endpoints con Postman.

---

## Historia de Usuario 3
### Tareas tentativas:
- [ ] Diseñar el modelo `Factura` con atributos: id, fechaEmision, cliente, listaServicios, importeNeto, IVA, total.
- [ ] Crear entidad JPA `Factura` con relaciones a `Cliente` y `Servicio`.
- [ ] Implementar lógica en `ServicioFactura` para:
  - Calcular IVA según condición fiscal del cliente.
  - Calcular total de factura.
  - Generar factura con número único.
- [ ] Crear controlador `ControladorFactura`.
- [ ] Probar generación de factura individual desde Postman.
- [ ] Validar consistencia de datos (cliente existente, servicios válidos, totales correctos).
