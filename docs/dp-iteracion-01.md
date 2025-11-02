# Trabajo en equipo


# Diseño OO

# Wireframe y caso de uso

# Backlog de iteraciones
<!-- Esto hay que modificar -->
Implementaremos las siguientes Historias de Usuario:
- Historia de Usuario 1
- Historia de Usuario 2
- Historia de Usuario 3
- Historia de Usuario 4

# Tareas
<!-- Esto hay que modificar -->
## Historia de Usuario 1
### Tareas tentativas:
- Diseñar el modelo `Cliente` con atributos: id, nombre/razón social, CUIT, condición fiscal, domicilio, email, teléfono.
- Crear entidad JPA `Cliente` con Lombok.
- Crear repositorio `RepositorioCliente`.
- Implementar servicio `ServicioCliente` para alta, baja y modificación de clientes.
- Crear controlador `ControladorCliente`.
- Implementar validaciones: CUIT válido, campos obligatorios, condición fiscal correcta.

## Historia de Usuario 2
### Tareas tentativas:
- Diseñar el modelo `Cuenta` con atributos: id, cliente_id, servicio contratado, importe, estado.
- Crear entidad JPA `Cuenta` con Lombok.
- Crear repositorio `RepositorioCuenta`.
- Implementar servicio `ServicioCuenta` para alta, baja y modificación de cuentas.
- Crear controlador `ControladorCuenta`.
- Validar que cada cuenta esté asociada a un cliente existente.

## Historia de Usuario 3
### Tareas tentativas:
- Diseñar el modelo `Factura` con atributos: id, cuenta_id, fecha emisión, importe neto, IVA, total, estado.
- Crear entidad JPA `Factura` con Lombok.
- Crear repositorio `RepositorioFactura`.
- Implementar servicio `ServicioFactura` para generar facturas individuales y calcular IVA según condición fiscal.
- Crear controlador `ControladorFactura`.
- Probar generación de facturas individuales con datos de prueba.

## Historia de Usuario 4
### Tareas tentativas:
- Implementar en `ServicioFactura` la función de facturación masiva por período.
- Recorrer todas las cuentas activas y generar facturas automáticamente calculando importes e IVA.
- Crear endpoint en `ControladorFactura` para facturación masiva.
- Probar facturación masiva con datos de ejemplo.
