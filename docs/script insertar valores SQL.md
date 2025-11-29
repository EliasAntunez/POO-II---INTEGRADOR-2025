-- =============================================================================
-- 1. LIMPIEZA DE TABLAS (Orden inverso por las FKs)
-- =============================================================================
/*
TRUNCATE TABLE detalle_nota_credito RESTART IDENTITY CASCADE;
TRUNCATE TABLE nota_credito RESTART IDENTITY CASCADE;
TRUNCATE TABLE detalle_factura RESTART IDENTITY CASCADE;
TRUNCATE TABLE movimiento_cuenta_corriente RESTART IDENTITY CASCADE;
TRUNCATE TABLE factura RESTART IDENTITY CASCADE;
TRUNCATE TABLE cliente_servicio RESTART IDENTITY CASCADE;
TRUNCATE TABLE servicio RESTART IDENTITY CASCADE;
TRUNCATE TABLE cliente RESTART IDENTITY CASCADE;
*/
-- =============================================================================
-- 2. SERVICIOS
-- =============================================================================
INSERT INTO servicio (id, nombre, descripcion, precio, alicuota, activo) VALUES 
(1, 'Internet Fibra 500Mb', 'Conexión alta velocidad (Tasa 27%)', 30000.00, 27.0, true),
(2, 'Mantenimiento PC', 'Servicio técnico mensual (Tasa 21%)', 15000.00, 21.0, true),
(3, 'Alquiler Modem 5G', 'Equipamiento hardware (Tasa 10.5%)', 5000.00, 10.5, true),
(4, 'Capacitación Online', 'Acceso a plataforma educativa (Exento)', 8500.00, 0.0, true);

-- =============================================================================
-- 3. CLIENTES
-- Nota: Inicializamos saldos con deuda porque vamos a insertar facturas abajo.
-- =============================================================================

-- Cliente 1: Gomez Tech (RI) - Le vamos a crear una factura de $43.625
INSERT INTO cliente (id, dni, nombre, apellido, razon_social, cuit, email, telefono, direccion, condicion_fiscal, estado, activo, saldo_cuenta_corriente) 
VALUES (1, '30111222', 'Carlos', 'Gomez', 'Gomez Tech SRL', '30301112227', 'carlos@test.com', '+54911111111', 'Av. Siempre Viva 123', 'RESPONSABLE_INSCRIPTO', 'ACTIVO', true, 43625.00);

-- Cliente 2: Ana Diseño (Monotributo) - Le vamos a crear una factura de $18.150
INSERT INTO cliente (id, dni, nombre, apellido, razon_social, cuit, email, telefono, direccion, condicion_fiscal, estado, activo, saldo_cuenta_corriente) 
VALUES (2, '25444555', 'Ana', 'Martinez', 'Ana Diseño', '27254445554', 'ana@test.com', '+54922222222', 'Calle Falsa 123', 'MONOTRIBUTISTA', 'ACTIVO', true, 18150.00);

-- Cliente 3: Pedro (Suspendido) - Sin deuda
INSERT INTO cliente (id, dni, nombre, apellido, razon_social, cuit, email, telefono, direccion, condicion_fiscal, estado, activo, saldo_cuenta_corriente) 
VALUES (3, '20999888', 'Pedro', 'Lopez', 'Constructora Lopez', '20209998881', 'pedro@test.com', '+54933333333', 'San Martin 500', 'RESPONSABLE_INSCRIPTO', 'SUSPENDIDO', false, 0.00);

-- =============================================================================
-- 4. ASIGNACIONES (Contratos vigentes)
-- =============================================================================
-- Gomez tiene Internet y Modem
INSERT INTO cliente_servicio (id_cliente, id_servicio, fecha_asignacion, activo) VALUES (1, 1, '2024-01-01', true);
INSERT INTO cliente_servicio (id_cliente, id_servicio, fecha_asignacion, activo) VALUES (1, 3, '2024-01-01', true);

-- Ana tiene Mantenimiento
INSERT INTO cliente_servicio (id_cliente, id_servicio, fecha_asignacion, activo) VALUES (2, 2, '2024-02-15', true);

-- =============================================================================
-- 5. FACTURACIÓN HISTÓRICA
-- Insertamos facturas manualmente para que el sistema arranque con datos reales
-- =============================================================================

-- FACTURA 1: Para Gomez Tech (RI) -> Tipo A
-- Total: Internet ($30k + 27%) + Modem ($5k + 10.5%) = 38.100 + 5.525 = 43.625
INSERT INTO factura (id, cliente_id, fecha_emision, total, anulada, estado, tipo_comprobante) 
VALUES (1, 1, NOW(), 43625.00, false, 'EMITIDA', 'FACTURA_A');

-- Detalles Factura 1
INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) 
VALUES 
(1, 1, 1, 30000.00, 27.00, 8100.00, 38100.00), -- Internet
(1, 3, 1, 5000.00, 10.50, 525.00, 5525.00);    -- Modem

-- Movimiento CC Factura 1
INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (1, 1, NOW(), 'FACTURA', 'Factura N° 1 - Carga Inicial', 43625.00, 'SISTEMA');


-- FACTURA 2: Para Ana Diseño (Mono) -> Tipo B
-- Total: Mantenimiento ($15k + 21%) = 18.150
INSERT INTO factura (id, cliente_id, fecha_emision, total, anulada, estado, tipo_comprobante) 
VALUES (2, 2, NOW(), 18150.00, false, 'EMITIDA', 'FACTURA_B');

-- Detalles Factura 2
INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) 
VALUES 
(2, 2, 1, 15000.00, 21.00, 3150.00, 18150.00);

-- Movimiento CC Factura 2
INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (2, 2, NOW(), 'FACTURA', 'Factura N° 2 - Carga Inicial', 18150.00, 'SISTEMA');


-- =============================================================================
-- 6. ACTUALIZAR SECUENCIAS (PostgreSQL)
-- Fundamental para que no falle al crear nuevos registros desde la app
-- =============================================================================
SELECT setval(pg_get_serial_sequence('servicio', 'id'), coalesce(max(id),0) + 1, false) FROM servicio;
SELECT setval(pg_get_serial_sequence('cliente', 'id'), coalesce(max(id),0) + 1, false) FROM cliente;
SELECT setval(pg_get_serial_sequence('cliente_servicio', 'id'), coalesce(max(id),0) + 1, false) FROM cliente_servicio;
SELECT setval(pg_get_serial_sequence('factura', 'id'), coalesce(max(id),0) + 1, false) FROM factura;
SELECT setval(pg_get_serial_sequence('detalle_factura', 'id'), coalesce(max(id),0) + 1, false) FROM detalle_factura;
SELECT setval(pg_get_serial_sequence('movimiento_cuenta_corriente', 'id'), coalesce(max(id),0) + 1, false) FROM movimiento_cuenta_corriente;