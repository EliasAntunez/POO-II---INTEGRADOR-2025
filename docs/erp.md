
# Requisitos funcionales:

## HU-01 Alta de Cliente
**Descripción:** Como **Administrador** quiero **registrar nuevos clientes** para **tener sus datos en el sistema y el cliente pueda acceder a los mismos.**
**Criterios de Aceptación:**
- Datos necesarios y obligatorios para su registro:
    - Razón social/Nombre
    - CUIT
    - Email
    - Teléfono
    - Dirección
    - Condición fiscal
    - Estado de la cuenta (Activa/Inactiva)
    - DNI
- El sistema debe validar que los campos obligatorios sean validados antes del registro (no permitir campos vacíos)
- El sistema debe validar que los campos obligatorios sean validados antes del registro (no permitir campos vacíos)

**Notas Técnicas:**
- El sistema debe verificar que el DNI y/o el CUIT no se repitan.
- El sistema debe validar los tipos de datos (texto, número, etc)


## HU-02 Modificación de Cliente
**Descripción:** Como **Administrador** quiero **modificar los datos de clientes** para **tener a los mismos con la información actualizada.**
**Criterios de Aceptación:**
- Tanto el DNI como el CUIT permanecerán sin modificaciones
- Si la condición fiscal del cliente cambia, la misma afectará únicamente a transacciones futuras.
- El sistema debe validar que los campos obligatorios sean validados antes (no permitir campos vacíos)


## HU-03 Baja de Cliente
**Descripción:** Como **Administrador** quiero **dar de baja a clientes** para **que estos no puedan acceder a su cuenta**
**Criterios de Aceptación:**
- El sistema debe verificar que no haya transacciones en proceso asociados al cliente a dar de baja.
- El sistema debe verificar que no haya transacciones en proceso asociados al cliente a dar de baja.

**Notas Técnicas:**
- La baja es lógica, no física (cambio de estado: de Activa a Inactiva)


## HU-04 Alta de Servicios
**Descripción:** Como **Administrador** quiero **registrar nuevos servicios** para **tener sus datos en el sistema y el cliente pueda solicitar los mismos.**
**Criterios de Aceptación:**
- Datos necesarios y obligatorios para su registro:
    - Nombre
    - Descripción
    - Precio
    - Tipo de Alícuota (27%, 21%, 10.5%, 3.5%)
    - Estado (Activo/Inactivo)
- El sistema debe validar que los campos obligatorios sean validados antes del registro (no permitir campos vacíos)
- El sistema debe validar que los campos obligatorios sean validados antes del registro (no permitir campos vacíos)

**Notas Técnicas:**
- La baja es lógica, no física (cambio de estado: de Activa a Inactiva)


## HU-05 Modificación de Servicios
**Descripción:** Como **Administrador** quiero **modificar los datos de un servicio** para **tener a los mismos con información actualizada**
**Criterios de Aceptación:**
- El sistema debe validar que los campos obligatorios sean validados (no permitir campos vacíos)


## HU-06 Baja de Servicios
**Descripción:** Como **Administrador** quiero **dar de baja a servicios** para **que estos no estén disponibles para los clientes**
**Criterios de Aceptación:**
- El sistema debe verificar que no haya transacciones en proceso asociados al servicio a dar de baja.
- Los servicios dados de baja no deben ser visibles para los clientes.
- Los servicios dados de baja no deben ser visibles para los clientes.

**Notas Técnicas:**
- La baja es lógica, no física (cambio de estado: de Activa a Inactiva)


## HU-07 Facturación Masiva
**Descripción:** Como **Administrador** quiero **realizar una facturación masiva** para **procesar un gran volumen de facturas de forma rápida y simultánea**
**Criterios de Aceptación:**
- La facturación masiva se realiza a todas aquellas cuentas que figuren como activas.
- El sistema debe permitir que el administrador ingrese un período (un mes en específico).
- Datos necesarios y obligatorios para su registro:
    - Fecha de vencimiento
    - Cantidad de facturas
    - Periodo facturado
    - Empleado responsable
**Notas Técnicas:**
- La facturación masiva queda registrada en log con su fecha y empleado responsable correspondiente.


## HU-08 Facturación Individual
**Descripción:** Como **Administrador** quiero **emitir una factura** para **documentar una operación de servicio**
**Criterios de Aceptación:**
- (faltan agregar)

**Notas Técnicas:**
- (faltan agregar)


## HU-09 Anulación de facturas
**Descripción:** Como **Administrador** quiero **anular una factura** para **corregir errores, reflejar devoluciones o cancelaciones de servicios**
**Criterios de Aceptación:**
- Se debe de incluir el motivo y el responsable de la anulación de la factura.
- No se puede aplicar doble anulación.
- La anulación se realiza mediante una nota de crédito.
- El sistema debe permitir vincular a la factura anulada con su correspondiente nota de crédito.
- El sistema debe permitir anular únicamente a aquellas facturas que fueron emitidas.


## HU-10 Asignar servicios a clientes
**Descripción:**  
Como **Administrador** quiero **asignar un servicio a un cliente** para **registrar la prestación y permitir su futura facturación**

**Criterios de Aceptación:**
- Debe existir un cliente registrado para asignar el servicio.
- El sistema debe mostrar la lista de servicios disponibles.
- El usuario debe poder seleccionar un servicio y asignarlo a un cliente.
- Se debe registrar la fecha de asignación del servicio.
- La asignación debe quedar asociada al cliente para poder facturar posteriormente.
- Si el cliente ya tiene el mismo servicio asignado y aún no facturado, el sistema debe advertirlo y evitar duplicados.
- Debe mostrarse un mensaje de confirmación al realizar la asignación correctamente.
- Si falta seleccionar servicio o cliente, el sistema debe mostrar un mensaje de error.
- El administrador que realice la operación debe quedar registrado.
- La interfaz debe actualizar la lista de servicios asignados al cliente en tiempo real.

**Notas Técnicas:**
- Validar existencia del cliente y del servicio antes de asignar.
- Tabla relacional: `cliente_servicio` (id, id_cliente, id_servicio, fecha_asignacion, estado, usuario_registro).
- Manejar estados del servicio asignado (ej. pendiente de facturación, facturado).
- En caso de error, retornar mensajes claros.  


## HU-11 Registrar Pago Total
**Descripción:**  
Como **Administrador** quiero **registrar el pago total de una factura** para **tener el estado de la cuenta del cliente actualizado**  

**Criterios de Aceptación:**
- El sistema debe permitir seleccionar una factura pendiente de pago.
- La factura debe estar en estado "Emitida" o "Pendiente de pago" para registrar el pago.
- El usuario debe ver el monto total de la factura antes de confirmar el pago.
- Al registrar el pago total, el estado de la factura debe pasar a **Pagada**.
- El sistema debe registrar la fecha de pago y el usuario que lo realizó.
- No se debe permitir registrar pagos adicionales una vez marcada como pagada.
- Al finalizar, el sistema muestra un mensaje de confirmación.
- Si la factura no existe, está anulada, o ya fue pagada, mostrar mensaje de error.
- La vista debe actualizar automáticamente el estado de la factura y el saldo del cliente.

**Notas Técnicas:**
- Vista Thymeleaf para listar facturas y opción "Registrar Pago".
- Formulario para confirmar el pago total.
- Entidad `Factura`: atributos sugeridos `id`, `montoTotal`, `estado`, `fechaEmision`, `fechaPago`, `cliente`, `usuarioPago`.
- Método en el controlador `POST /facturas/{id}/pago-total`.
- Servicio con método `registrarPagoTotal(facturaId, usuario)`.
- Validar estado antes de actualizar.
- Actualizar saldo del cliente si corresponde.
- Registro en tabla `pagos` o historial (opcional pero recomendado).
- Manejar transacción JPA para consistencia.


## HU-12 Registrar Pago Parcial
**Descripción:**  
Como **Administrador** quiero **registrar el pago parcial de una factura** para **saber el monto pendiente a pagar y mantener el estado de la cuenta del cliente actualizado**

**Criterios de Aceptación:**
- El sistema debe permitir seleccionar una factura con saldo pendiente.
- El usuario debe ingresar el monto a pagar.
- El sistema debe validar que el pago no exceda el saldo pendiente.
- Se debe calcular automáticamente monto pagado acumulado y saldo restante.
- Si el saldo llega a cero, la factura pasa a estado **Pagada**.
- Registrar fecha del pago parcial y usuario que lo realizó.
- Mostrar mensaje de confirmación al finalizar.
- Si el monto ingresado es inválido (cero, negativo o mayor al saldo), mostrar error.
- La vista debe actualizar el saldo pendiente y el historial de pagos.

**Notas Técnicas:**
- Vista Thymeleaf con detalle de factura y formulario para registrar pago parcial.
- Entidad `Pago`: `id`, `factura`, `monto`, `fechaPago`, `usuario`.
- Relación 1-a-N `Factura -> Pagos`.
- Método controlador `POST /facturas/{id}/pago-parcial`.
- Método de servicio `registrarPagoParcial(facturaId, monto, usuario)`.
- Validar monto y estado antes de guardar.
- Cálculo saldo pendiente = montoTotal − suma(pagos).
- Transacción JPA para garantizar integridad.
- Actualizar estado de factura cuando saldo = 0.
