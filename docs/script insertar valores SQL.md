-- =============================================================================
-- 1. LIMPIEZA TOTAL (Reinicia IDs)
-- =============================================================================
/*
TRUNCATE TABLE movimiento_cuenta_corriente RESTART IDENTITY CASCADE;
TRUNCATE TABLE detalle_nota_credito RESTART IDENTITY CASCADE;
TRUNCATE TABLE nota_credito RESTART IDENTITY CASCADE;
TRUNCATE TABLE pago RESTART IDENTITY CASCADE;
TRUNCATE TABLE detalle_factura RESTART IDENTITY CASCADE;
TRUNCATE TABLE factura RESTART IDENTITY CASCADE;
TRUNCATE TABLE cliente_servicio RESTART IDENTITY CASCADE;
TRUNCATE TABLE servicio RESTART IDENTITY CASCADE;
TRUNCATE TABLE cliente RESTART IDENTITY CASCADE;
*/
-- =============================================================================
-- 2. SERVICIOS (16 Registros)
-- =============================================================================
INSERT INTO servicio (id, nombre, descripcion, precio, alicuota, activo) VALUES 
(1, 'Internet Hogar 100Mb', 'Conexión fibra óptica básica', 15000.00, 21.0, true),
(2, 'Internet Hogar 300Mb', 'Conexión fibra óptica media', 22000.00, 21.0, true),
(3, 'Internet Gamer 600Mb', 'Alta velocidad baja latencia', 35000.00, 21.0, true),
(4, 'Internet Empresas 1Gb', 'Enlace dedicado simétrico', 80000.00, 27.0, true),
(5, 'TV Clásica', 'Grilla de 60 canales', 8500.00, 21.0, true),
(6, 'TV Digital HD', 'Grilla completa + Pack Fútbol', 14500.00, 21.0, true),
(7, 'Línea Fija IP', 'Telefonía voz sobre IP', 4000.00, 27.0, true),
(8, 'Mantenimiento PC', 'Limpieza y optimización', 12000.00, 21.0, true),
(9, 'Soporte Servidores', 'Mantenimiento mensual servidores', 60000.00, 21.0, true),
(10, 'Hosting Web Básico', 'Alojamiento compartido', 5000.00, 27.0, true),
(11, 'Hosting VPS', 'Servidor privado virtual', 25000.00, 27.0, true),
(12, 'Alquiler Router WiFi 6', 'Hardware en comodato', 6000.00, 10.5, true),
(13, 'Alquiler Deco 4K', 'Decodificador Android TV', 7500.00, 10.5, true),
(14, 'Capacitación IT', 'Cursos online (Exento)', 20000.00, 0.0, true),
(15, 'Libros Digitales', 'Suscripción (Exento)', 4500.00, 0.0, true),
(16, 'Servicio Legacy 2020', 'Plan antiguo', 1000.00, 21.0, false);

-- =============================================================================
-- 3. CLIENTES (15 Registros)
-- =============================================================================
INSERT INTO cliente (id, dni, nombre, apellido, razon_social, cuit, email, telefono, direccion, condicion_fiscal, estado, activo, saldo_cuenta_corriente) VALUES 
(1, '10111111', 'Juan', 'Perez', 'Perez Sistemas SA', '30101111115', 'juan@perez.com', '11111111', 'Calle 1', 'RESPONSABLE_INSCRIPTO', 'ACTIVO', true, 0.00),
(2, '10222222', 'Maria', 'Lopez', 'Lopez Constructora SRL', '30102222225', 'maria@lopez.com', '22222222', 'Calle 2', 'RESPONSABLE_INSCRIPTO', 'ACTIVO', true, 0.00),
(3, '10333333', 'Carlos', 'Diaz', 'Diaz Logistica SA', '30103333335', 'carlos@diaz.com', '33333333', 'Calle 3', 'RESPONSABLE_INSCRIPTO', 'ACTIVO', true, 0.00),
(4, '10444444', 'Ana', 'Sosa', 'Sosa Agropecuaria', '30104444445', 'ana@sosa.com', '44444444', 'Calle 4', 'RESPONSABLE_INSCRIPTO', 'ACTIVO', true, 0.00),
(5, '10555555', 'Luis', 'Mora', 'Mora Tech Solutions', '30105555555', 'luis@mora.com', '55555555', 'Calle 5', 'RESPONSABLE_INSCRIPTO', 'SUSPENDIDO', false, 0.00),
(6, '20111111', 'Pedro', 'Gomez', 'Pedro Gomez Plomeria', '27201111115', 'pedro@gomez.com', '66666666', 'Av A 100', 'MONOTRIBUTISTA', 'ACTIVO', true, 0.00),
(7, '20222222', 'Sofia', 'Ruiz', 'Sofia Ruiz Diseño', '27202222225', 'sofia@ruiz.com', '77777777', 'Av B 200', 'MONOTRIBUTISTA', 'ACTIVO', true, 0.00),
(8, '20333333', 'Miguel', 'Torres', 'Torres Fletes', '20203333335', 'miguel@torres.com', '88888888', 'Av C 300', 'MONOTRIBUTISTA', 'ACTIVO', true, 0.00),
(9, '20444444', 'Laura', 'Vargas', 'Vargas Catering', '27204444445', 'laura@vargas.com', '99999999', 'Av D 400', 'MONOTRIBUTISTA', 'ACTIVO', true, 0.00),
(10, '20555555', 'Diego', 'Castro', 'Diego Castro Electricidad', '20205555555', 'diego@castro.com', '10101010', 'Av E 500', 'MONOTRIBUTISTA', 'ACTIVO', true, 0.00),
(11, '30111111', 'Roberto', 'Sanchez', 'Roberto Sanchez', '20301111115', 'roberto@gmail.com', '12121212', 'Barrio Norte 1', 'NO_RESPONSABLE', 'ACTIVO', true, 0.00),
(12, '30222222', 'Lucia', 'Mendez', 'Lucia Mendez', '27302222225', 'lucia@hotmail.com', '13131313', 'Barrio Norte 2', 'NO_RESPONSABLE', 'ACTIVO', true, 0.00),
(13, '30333333', 'Jorge', 'Fernandez', 'Jorge Fernandez', '20303333335', 'jorge@yahoo.com', '14141414', 'Barrio Norte 3', 'NO_RESPONSABLE', 'ACTIVO', true, 0.00),
(14, '40111111', 'Fundacion', 'Ayuda', 'Fundacion Ayuda ONG', '30401111115', 'admin@ong.org', '15151515', 'Centro 1', 'EXENTO', 'ACTIVO', true, 0.00),
(15, '40222222', 'Club', 'Social', 'Club Social Deportivo', '30402222225', 'info@club.com', '16161616', 'Centro 2', 'EXENTO', 'ACTIVO', true, 0.00);

-- =============================================================================
-- 4. ASIGNACIONES (20 Registros)
-- =============================================================================
INSERT INTO cliente_servicio (id_cliente, id_servicio, fecha_asignacion, activo) VALUES 
(1, 4, '2024-01-01', true), (1, 12, '2024-01-01', true),
(2, 3, '2024-02-01', true), (3, 2, '2024-02-10', true), (3, 13, '2024-02-10', true),
(4, 12, '2024-03-01', true), (5, 9, '2024-01-01', true),
(6, 1, '2024-01-15', true), (7, 2, '2024-04-01', true), (8, 1, '2024-05-01', true),
(9, 1, '2024-06-01', true), (10, 1, '2024-07-01', true),
(11, 1, '2024-01-01', true), (12, 2, '2024-01-01', true), (13, 1, '2024-01-01', true),
(14, 14, '2024-01-01', true), (15, 6, '2024-01-01', true),
(1, 11, '2024-01-01', false), (2, 5, '2024-01-01', false), (6, 5, '2024-01-01', false);

-- =============================================================================
-- 5. FACTURAS, DETALLES, PAGOS Y MOVIMIENTOS
-- =============================================================================

-- CASO 1: PAGADA TOTALMENTE (Cliente 1)
INSERT INTO factura (id, cliente_id, fecha_emision, fecha_vencimiento, total, monto_pagado, estado, anulada, tipo_comprobante) 
VALUES (1, 1, '2024-10-01 10:00:00', '2024-10-11', 101600.00, 101600.00, 'PAGADA', false, 'FACTURA_A');

INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) 
VALUES (1, 4, 1, 80000.00, 27.00, 21600.00, 101600.00);

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (1, 1, '2024-10-01 10:00:00', 'FACTURA', 'Factura N° 1', 101600.00, 'SISTEMA');

INSERT INTO pago (id, cliente_id, factura_id, fecha_pago, monto, medio_pago, observaciones, usuario_registro)
VALUES (1, 1, 1, '2024-10-05 12:00:00', 101600.00, 'TRANSFERENCIA', 'Pago total web', 'SISTEMA');

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, pago_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (1, 1, 1, '2024-10-05 12:00:00', 'PAGO', 'Pago Factura N° 1', 101600.00, 'SISTEMA');

-- CASO 2: PARCIALMENTE PAGADA (Cliente 2)
INSERT INTO factura (id, cliente_id, fecha_emision, fecha_vencimiento, total, monto_pagado, estado, anulada, tipo_comprobante) 
VALUES (2, 2, '2024-10-01 10:05:00', '2024-10-11', 42350.00, 20000.00, 'PARCIALMENTE_PAGADA', false, 'FACTURA_A');

INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) 
VALUES (2, 3, 1, 35000.00, 21.00, 7350.00, 42350.00);

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (2, 2, '2024-10-01 10:05:00', 'FACTURA', 'Factura N° 2', 42350.00, 'SISTEMA');

INSERT INTO pago (id, cliente_id, factura_id, fecha_pago, monto, medio_pago, observaciones, usuario_registro)
VALUES (2, 2, 2, '2024-10-06 15:30:00', 20000.00, 'EFECTIVO', 'Entrega parcial', 'CAJA');

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, pago_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (2, 2, 2, '2024-10-06 15:30:00', 'PAGO', 'Pago Parcial Fac N° 2', 20000.00, 'CAJA');

-- CASO 3: IMPAGA (Cliente 3)
INSERT INTO factura (id, cliente_id, fecha_emision, fecha_vencimiento, total, monto_pagado, estado, anulada, tipo_comprobante) 
VALUES (3, 3, '2024-10-01 10:10:00', '2024-10-11', 34907.50, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_A');

INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) VALUES 
(3, 2, 1, 22000.00, 21.00, 4620.00, 26620.00),
(3, 13, 1, 7500.00, 10.50, 787.50, 8287.50);

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (3, 3, '2024-10-01 10:10:00', 'FACTURA', 'Factura N° 3', 34907.50, 'SISTEMA');

-- CASOS 4 a 13 (VARIOS EMITIDOS)
INSERT INTO factura (id, cliente_id, fecha_emision, fecha_vencimiento, total, monto_pagado, estado, anulada, tipo_comprobante) VALUES 
(4, 4, NOW(), NOW() + INTERVAL '10 days', 6630.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_A'),
(5, 6, NOW(), NOW() + INTERVAL '10 days', 18150.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(6, 8, NOW(), NOW() + INTERVAL '10 days', 18150.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(7, 9, NOW(), NOW() + INTERVAL '10 days', 18150.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(8, 10, NOW(), NOW() + INTERVAL '10 days', 18150.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(9, 11, NOW(), NOW() + INTERVAL '10 days', 18150.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(10, 7, NOW(), NOW() + INTERVAL '10 days', 26620.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(11, 12, NOW(), NOW() + INTERVAL '10 days', 26620.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(12, 13, NOW(), NOW() + INTERVAL '10 days', 18150.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B'),
(13, 14, NOW(), NOW() + INTERVAL '10 days', 20000.00, 0.00, 'PENDIENTE_PAGO', false, 'FACTURA_B');

-- Detalles 4-13
INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) VALUES 
(4, 12, 1, 6000.00, 10.50, 630.00, 6630.00),
(5, 1, 1, 15000.00, 21.00, 3150.00, 18150.00),
(6, 1, 1, 15000.00, 21.00, 3150.00, 18150.00),
(7, 1, 1, 15000.00, 21.00, 3150.00, 18150.00),
(8, 1, 1, 15000.00, 21.00, 3150.00, 18150.00),
(9, 1, 1, 15000.00, 21.00, 3150.00, 18150.00),
(10, 2, 1, 22000.00, 21.00, 4620.00, 26620.00),
(11, 2, 1, 22000.00, 21.00, 4620.00, 26620.00),
(12, 1, 1, 15000.00, 21.00, 3150.00, 18150.00),
(13, 14, 1, 20000.00, 0.00, 0.00, 20000.00);

-- Movimientos 4-13
INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) VALUES 
(4, 4, NOW(), 'FACTURA', 'Factura N° 4', 6630.00, 'SISTEMA'),
(6, 5, NOW(), 'FACTURA', 'Factura N° 5', 18150.00, 'SISTEMA'),
(8, 6, NOW(), 'FACTURA', 'Factura N° 6', 18150.00, 'SISTEMA'),
(9, 7, NOW(), 'FACTURA', 'Factura N° 7', 18150.00, 'SISTEMA'),
(10, 8, NOW(), 'FACTURA', 'Factura N° 8', 18150.00, 'SISTEMA'),
(11, 9, NOW(), 'FACTURA', 'Factura N° 9', 18150.00, 'SISTEMA'),
(7, 10, NOW(), 'FACTURA', 'Factura N° 10', 26620.00, 'SISTEMA'),
(12, 11, NOW(), 'FACTURA', 'Factura N° 11', 26620.00, 'SISTEMA'),
(13, 12, NOW(), 'FACTURA', 'Factura N° 12', 18150.00, 'SISTEMA'),
(14, 13, NOW(), 'FACTURA', 'Factura N° 13', 20000.00, 'SISTEMA');


-- CASO 4: ANULACIÓN (Cliente 15)
INSERT INTO factura (id, cliente_id, fecha_emision, fecha_vencimiento, total, monto_pagado, estado, anulada, tipo_comprobante) 
VALUES (14, 15, NOW(), NOW() + INTERVAL '10 days', 17545.00, 0.00, 'ANULADA', true, 'FACTURA_B');

INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) 
VALUES (14, 6, 1, 14500.00, 21.00, 3045.00, 17545.00);

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (15, 14, NOW() - INTERVAL '1 hour', 'FACTURA', 'Factura N° 14', 17545.00, 'SISTEMA');

INSERT INTO nota_credito (id, cliente_id, factura_id, fecha_emision, fecha_registro, total, motivo, tipo_comprobante, usuario_responsable)
VALUES (1, 15, 14, NOW(), NOW(), 17545.00, 'Error Admin', 'NOTA_CREDITO_B', 'ADMIN');

INSERT INTO detalle_nota_credito (nota_credito_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal)
VALUES (1, 6, 1, 14500.00, 21.00, 3045.00, 17545.00);

INSERT INTO movimiento_cuenta_corriente (cliente_id, nota_credito_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (15, 1, NOW(), 'ANULACION', 'Nota Crédito N° 1', 17545.00, 'ADMIN');


-- CASO 5: OTRA ANULACIÓN (Cliente 1)
INSERT INTO factura (id, cliente_id, fecha_emision, fecha_vencimiento, total, monto_pagado, estado, anulada, tipo_comprobante) 
VALUES (15, 1, NOW(), NOW() + INTERVAL '10 days', 15000.00, 0.00, 'ANULADA', true, 'FACTURA_A');

INSERT INTO detalle_factura (factura_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal) 
VALUES (15, 8, 1, 12000.00, 21.00, 3000.00, 15000.00);

INSERT INTO movimiento_cuenta_corriente (cliente_id, factura_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (1, 15, NOW() - INTERVAL '1 hour', 'FACTURA', 'Factura N° 15', 15000.00, 'SISTEMA');

INSERT INTO nota_credito (id, cliente_id, factura_id, fecha_emision, fecha_registro, total, motivo, tipo_comprobante, usuario_responsable)
VALUES (2, 1, 15, NOW(), NOW(), 15000.00, 'Error Facturacion', 'NOTA_CREDITO_A', 'ADMIN');

INSERT INTO detalle_nota_credito (nota_credito_id, servicio_id, cantidad, precio_unitario, alicuota_iva, monto_iva, subtotal)
VALUES (2, 8, 1, 12000.00, 21.00, 3000.00, 15000.00);

INSERT INTO movimiento_cuenta_corriente (cliente_id, nota_credito_id, fecha_movimiento, tipo_movimiento, descripcion, monto, usuario_registro) 
VALUES (1, 2, NOW(), 'ANULACION', 'Nota Crédito N° 2', 15000.00, 'ADMIN');

-- =============================================================================
-- 6. ACTUALIZAR SALDOS CLIENTES (Calculados manualmente)
-- =============================================================================
UPDATE cliente SET saldo_cuenta_corriente = 0.00 WHERE id = 1;   -- Pagó todo, lo extra se anuló
UPDATE cliente SET saldo_cuenta_corriente = 22350.00 WHERE id = 2; -- Pagó parcial
UPDATE cliente SET saldo_cuenta_corriente = 34907.50 WHERE id = 3; -- Debe todo
UPDATE cliente SET saldo_cuenta_corriente = 6630.00 WHERE id = 4;
UPDATE cliente SET saldo_cuenta_corriente = 0.00 WHERE id = 5;
UPDATE cliente SET saldo_cuenta_corriente = 18150.00 WHERE id = 6;
UPDATE cliente SET saldo_cuenta_corriente = 26620.00 WHERE id = 7;
UPDATE cliente SET saldo_cuenta_corriente = 18150.00 WHERE id = 8;
UPDATE cliente SET saldo_cuenta_corriente = 18150.00 WHERE id = 9;
UPDATE cliente SET saldo_cuenta_corriente = 18150.00 WHERE id = 10;
UPDATE cliente SET saldo_cuenta_corriente = 18150.00 WHERE id = 11;
UPDATE cliente SET saldo_cuenta_corriente = 26620.00 WHERE id = 12;
UPDATE cliente SET saldo_cuenta_corriente = 18150.00 WHERE id = 13;
UPDATE cliente SET saldo_cuenta_corriente = 20000.00 WHERE id = 14;
UPDATE cliente SET saldo_cuenta_corriente = 0.00 WHERE id = 15;  -- Anuló

-- =============================================================================
-- 7. SECUENCIAS
-- =============================================================================
SELECT setval(pg_get_serial_sequence('servicio', 'id'), coalesce(max(id),0) + 1, false) FROM servicio;
SELECT setval(pg_get_serial_sequence('cliente', 'id'), coalesce(max(id),0) + 1, false) FROM cliente;
SELECT setval(pg_get_serial_sequence('cliente_servicio', 'id'), coalesce(max(id),0) + 1, false) FROM cliente_servicio;
SELECT setval(pg_get_serial_sequence('factura', 'id'), coalesce(max(id),0) + 1, false) FROM factura;
SELECT setval(pg_get_serial_sequence('detalle_factura', 'id'), coalesce(max(id),0) + 1, false) FROM detalle_factura;
SELECT setval(pg_get_serial_sequence('nota_credito', 'id'), coalesce(max(id),0) + 1, false) FROM nota_credito;
SELECT setval(pg_get_serial_sequence('detalle_nota_credito', 'id'), coalesce(max(id),0) + 1, false) FROM detalle_nota_credito;
SELECT setval(pg_get_serial_sequence('pago', 'id'), coalesce(max(id),0) + 1, false) FROM pago;
SELECT setval(pg_get_serial_sequence('movimiento_cuenta_corriente', 'id'), coalesce(max(id),0) + 1, false) FROM movimiento_cuenta_corriente;