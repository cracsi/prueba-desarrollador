-- Pólizas de ejemplo
INSERT INTO poliza (id, numero, tipo, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima, estado, version)
VALUES (1, 'IND-0001', 'INDIVIDUAL', 'Carlos Pérez (arrendatario)', '2026-01-01', '2026-12-31', 12, 1500000.00, 18000000.00, 'ACTIVA', 0);

INSERT INTO poliza (id, numero, tipo, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima, estado, version)
VALUES (2, 'COL-0001', 'COLECTIVA', 'Inmobiliaria Los Andes S.A.S.', '2026-01-01', '2026-12-31', 12, 2000000.00, 24000000.00, 'ACTIVA', 0);

INSERT INTO poliza (id, numero, tipo, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima, estado, version)
VALUES (3, 'IND-0002', 'INDIVIDUAL', 'Ana Gómez (arrendataria)', '2025-06-01', '2026-05-31', 12, 1200000.00, 14400000.00, 'CANCELADA', 0);

-- Riesgos
INSERT INTO riesgo (id, poliza_id, direccion_inmueble, arrendatario, arrendador, estado, version)
VALUES (1, 1, 'Calle 100 # 15-20, Apto 301', 'Carlos Pérez', 'Luis Martínez', 'ACTIVO', 0);

INSERT INTO riesgo (id, poliza_id, direccion_inmueble, arrendatario, arrendador, estado, version)
VALUES (2, 2, 'Carrera 7 # 72-10, Apto 502', 'María Rodríguez', 'Jorge Silva', 'ACTIVO', 0);

INSERT INTO riesgo (id, poliza_id, direccion_inmueble, arrendatario, arrendador, estado, version)
VALUES (3, 2, 'Calle 85 # 11-40, Apto 1203', 'Pedro Ramírez', 'Sofía Castro', 'ACTIVO', 0);

INSERT INTO riesgo (id, poliza_id, direccion_inmueble, arrendatario, arrendador, estado, version)
VALUES (4, 3, 'Avenida 19 # 120-30, Casa 4', 'Ana Gómez', 'Ricardo López', 'CANCELADO', 0);

-- Reiniciar secuencias después de los datos semilla
ALTER TABLE poliza ALTER COLUMN id RESTART WITH 100;
ALTER TABLE riesgo ALTER COLUMN id RESTART WITH 100;
