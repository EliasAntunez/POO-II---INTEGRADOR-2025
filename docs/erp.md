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
        - La facturación masiva queda registrada en log con su fecha y empleado responsable correcpondiente.

## HU-08 Facturación Individual
**Descripción:** Como **Administrador** quiero **emitir una factura** para **documentar una operación de servicio**
**Criterios de Aceptación:**
        - 
**Notas Técnicas:**
        - 

## HU-09 Anulación de facturas
**Descripción:** Como **Administrador** quiero **anular una factura** para **para corregir errores, reflejar devoluciones o cancelaciones de servicios**
**Criterios de Aceptación:**
        - Se debe de incluir el motivo y el responsable de la anulacón de la factura.
        - No se puede aplicar doble anulación.
        - La anulación se realiza mediante una nota de crédito.
        - El sistema debe permitir vincular a la factura anulada con su correspondiente nota de crédito.
        - El sistema debe permitir anular únicamente a aquellas facturas que fueron emitidas.

## HU-10 Ver historial de facturaciones masivas
**Descripción:** Como **Administrador** quiero **ver el historial de las facturaciones masivas realizadas** para **auditar transacciones**
**Criterios de Aceptación:**
        - El sistema debe permitir filtrar por período.

## HU-11 Asignar servicios a clientes
**Descripción:** Como **Administrador** quiero **emitir una factura** para **documentar una operación de servicio**
**Criterios de Aceptación:**
        - 
**Notas Técnicas:**
        - 

## HU-12 Ver Estado de Cuenta de un cliente
**Descripción:** Como **Administrador** quiero **ver el estado de cuenta de un cliente** para **tener un control financiero del mismo**
**Criterios de Aceptación:**
        - Datos que deben aparecer:
            - Nombre del titular
            - Número de cuenta
            - Período de tiempo que cubre el informe.
        - El estado de cuenta incluye:
            - Un listado de todos los depósitos, retiros, transferencias, compras (con fecha y establecimiento) y pagos realizados.
            - Saldo inicial y saldo final del período.
            - Información de pagos (Para tarjetas de crédito, incluye la fecha límite de pago, el pago mínimo requerido y la deuda total).
        - Se debe listar las facturas por su estado (Vigente, Anulada, Pagada, Vencida o Parcialmente pagada)

## HU-13 Registrar Pago Total
**Descripción:** Como **Administrador** quiero **registrar el pago total de una factura** para **tener el estado de la cuenta del cliente actualizado**
**Criterios de Aceptación:**
        - 
**Notas Técnicas:**
        - 

## HU-14 Registrar Pago Parcial
**Descripción:** Como **Administrador** quiero **registrar el pago parcial de una factura** para **saber el monto pendiente a pagar y para tener el estado de la cuenta del cliente actualizado**
**Criterios de Aceptación:**
        - 
**Notas Técnicas:**
        - 

## HU-15 Emitir Comprobantes de Pago
**Descripción:** Como **Administrador** quiero **emitir el comprobante de un pago** para **tener documentadas aquellas facturas/servicios abonados por el cliente**
**Criterios de Aceptación:**
        - 
**Notas Técnicas:**
        - 





