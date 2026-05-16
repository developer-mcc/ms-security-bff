-- =====================================================================
-- Data de prueba cargada en cada arranque (ddl-auto: create-drop).
-- Contraseña universal de los usuarios demo: Demo2026!
-- (BCrypt vía pgcrypto -> compatible con Spring BCryptPasswordEncoder)
-- =====================================================================

SET search_path TO sec;

-- ---------------------------------------------------------------------
-- 1) Roles del catálogo
-- ---------------------------------------------------------------------
INSERT INTO sec.roles (id, nombre, descripcion, es_sistema, creado_por, fecha_creacion) VALUES
    ('SUPER_ADMIN',    'Super Administrador',         'Acceso total al sistema, multiempresa.',          TRUE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('ADMIN_EMPRESA',  'Administrador de Empresa',    'Administra todas las sucursales de su empresa.',  TRUE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('ADMIN_SUCURSAL', 'Administrador de Sucursal',   'Administra una sucursal específica.',             TRUE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('FARMACEUTICO',   'Farmacéutico',                'Atención al cliente, recetas y dispensación.',    TRUE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('CAJERO',         'Cajero',                      'Operación del POS y registro de ventas.',         TRUE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('AUDITOR',        'Auditor',                     'Solo lectura sobre operaciones y bitácora.',      TRUE, 'SYSTEM', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 2) Permisos 3D (rol x recurso x acciones x alcance)
--    SUPER_ADMIN -> recurso='*' (comodín, evaluado por el PermissionEvaluator).
-- ---------------------------------------------------------------------
INSERT INTO sec.permisos_rol_recurso (id, rol_id, recurso, acciones, alcance, creado_por, fecha_creacion) VALUES
    -- SUPER_ADMIN: comodín
    (uuid_generate_v4(), 'SUPER_ADMIN',    '*',              ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),

    -- ADMIN_EMPRESA: control total dentro de su empresa, todas las sucursales
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'USER',           ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'BRANCH',         ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'PRODUCT',        ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'STOCK',          ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'SALE',           ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'CLIENT',         ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'SUPPLIER',       ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'PURCHASE_ORDER', ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'PRESCRIPTION',   ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'REPORT',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_EMPRESA',  'AUDIT',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),

    -- ADMIN_SUCURSAL: solo su sucursal
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'USER',           ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'BRANCH',         ARRAY['READ','UPDATE']::VARCHAR[],                   'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'PRODUCT',        ARRAY['READ','UPDATE']::VARCHAR[],                   'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'STOCK',          ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'SALE',           ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'CLIENT',         ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'SUPPLIER',       ARRAY['READ']::VARCHAR[],                            'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'PURCHASE_ORDER', ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'PRESCRIPTION',   ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'ADMIN_SUCURSAL', 'REPORT',         ARRAY['READ']::VARCHAR[],                            'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),

    -- FARMACEUTICO: dispensación, clientes y recetas en su sucursal
    (uuid_generate_v4(), 'FARMACEUTICO',   'BRANCH',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'FARMACEUTICO',   'PRODUCT',        ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'FARMACEUTICO',   'STOCK',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'FARMACEUTICO',   'CLIENT',         ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'FARMACEUTICO',   'PRESCRIPTION',   ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'FARMACEUTICO',   'SALE',           ARRAY['READ','CREATE']::VARCHAR[],                   'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),

    -- CAJERO: POS y consulta de stock cruzado (sin update)
    (uuid_generate_v4(), 'CAJERO',         'BRANCH',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'CAJERO',         'SALE',           ARRAY['READ','CREATE']::VARCHAR[],                   'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'CAJERO',         'PRODUCT',        ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'CAJERO',         'STOCK',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'CAJERO',         'CLIENT',         ARRAY['READ','CREATE']::VARCHAR[],                   'OWN_BRANCH',   'SYSTEM', CURRENT_TIMESTAMP),

    -- AUDITOR: solo lectura, en toda la empresa
    (uuid_generate_v4(), 'AUDITOR',        'BRANCH',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'SALE',           ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'PRODUCT',        ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'STOCK',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'CLIENT',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'SUPPLIER',       ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'PURCHASE_ORDER', ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'REPORT',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'AUDITOR',        'AUDIT',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES', 'SYSTEM', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 3) Empresas demo
-- ---------------------------------------------------------------------
INSERT INTO sec.empresas (id, ruc, razon_social, nombre_comercial, estado, fecha_alta, creado_por, fecha_creacion) VALUES
    ('11111111-1111-1111-1111-111111111111', '20100100100', 'Botica San Juan S.A.C.',  'Botica San Juan',  'ACTIVO', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', '20200200200', 'Farmacia Vida E.I.R.L.',  'Farmacia Vida',    'ACTIVO', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 4) Sucursales (2 por empresa)
-- ---------------------------------------------------------------------
INSERT INTO sec.sucursales (id, empresa_id, codigo, nombre, direccion, distrito, provincia, departamento, telefono, estado, es_principal, creado_por, fecha_creacion) VALUES
    ('aaaa1111-aaaa-1111-aaaa-111111111111', '11111111-1111-1111-1111-111111111111',
     'BSJ-001', 'Botica San Juan - Centro',  'Av. Larco 123',      'Miraflores', 'Lima',     'Lima',     '014441111', 'ACTIVO', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('aaaa2222-aaaa-2222-aaaa-222222222222', '11111111-1111-1111-1111-111111111111',
     'BSJ-002', 'Botica San Juan - Norte',   'Av. Tupac Amaru 500','Comas',      'Lima',     'Lima',     '014442222', 'ACTIVO', FALSE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('bbbb1111-bbbb-1111-bbbb-111111111111', '22222222-2222-2222-2222-222222222222',
     'FV-001',  'Farmacia Vida - Plaza',     'Jr. Junín 250',      'Cercado',    'Arequipa', 'Arequipa', '054301111', 'ACTIVO', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('bbbb2222-bbbb-2222-bbbb-222222222222', '22222222-2222-2222-2222-222222222222',
     'FV-002',  'Farmacia Vida - Cayma',     'Av. Cayma 800',      'Cayma',      'Arequipa', 'Arequipa', '054302222', 'ACTIVO', FALSE, 'SYSTEM', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 5) Usuarios. Contraseña 'Demo2026!' hasheada con BCrypt cost=10.
-- ---------------------------------------------------------------------
INSERT INTO sec.usuarios
    (id, empresa_id, nombre_usuario, correo, contrasena_hash, nombres, apellidos, dni, telefono,
     canal_otp_preferido, mfa_habilitado, estado, intentos_fallidos, contrasena_cambiada_en, creado_por, fecha_creacion) VALUES
    -- SUPER_ADMIN (sin empresa)
    ('00000000-0000-0000-0000-000000000001', NULL,
     'superadmin', 'superadmin@demo.local',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Super', 'Admin', '00000001', '999000001', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),

    -- Botica San Juan
    ('11111111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     'admin.bsj', 'admin@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Lucía', 'Ramírez', '70011111', '999110001', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
     'jefe.bsj.centro', 'jefe.centro@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Carlos', 'Mendoza', '70011112', '999110002', 'WHATSAPP', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111',
     'farma.bsj', 'farma@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'María', 'Quispe', '70011113', '999110003', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111',
     'cajero.bsj', 'cajero@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Jorge', 'Vargas', '70011114', '999110004', 'SMS', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111',
     'auditor.bsj', 'auditor@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Patricia', 'Torres', '70011115', '999110005', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),

    -- Farmacia Vida
    ('22222222-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222',
     'admin.fv', 'admin@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Renato', 'Salas', '70022221', '999220001', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222',
     'jefe.fv.plaza', 'jefe.plaza@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Andrea', 'Flores', '70022222', '999220002', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222',
     'farma.fv', 'farma@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Diego', 'Huamán', '70022223', '999220003', 'WHATSAPP', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222',
     'cajero.fv', 'cajero@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Sofía', 'Ríos', '70022224', '999220004', 'SMS', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000005', '22222222-2222-2222-2222-222222222222',
     'auditor.fv', 'auditor@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Manuel', 'Cárdenas', '70022225', '999220005', 'EMAIL', TRUE, 'ACTIVO', 0, CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 6) Asignación de roles
-- ---------------------------------------------------------------------
INSERT INTO sec.usuarios_roles (usuario_id, rol_id, asignado_por, asignado_en) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN',    'SYSTEM', CURRENT_TIMESTAMP),
    -- Botica San Juan
    ('11111111-0000-0000-0000-000000000001', 'ADMIN_EMPRESA',  'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000002', 'ADMIN_SUCURSAL', 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000003', 'FARMACEUTICO',   'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000004', 'CAJERO',         'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000005', 'AUDITOR',        'SYSTEM', CURRENT_TIMESTAMP),
    -- Farmacia Vida
    ('22222222-0000-0000-0000-000000000001', 'ADMIN_EMPRESA',  'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000002', 'ADMIN_SUCURSAL', 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000003', 'FARMACEUTICO',   'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000004', 'CAJERO',         'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000005', 'AUDITOR',        'SYSTEM', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------
-- 7) Asignación de sucursales (con sucursal predeterminada)
-- ---------------------------------------------------------------------
INSERT INTO sec.usuarios_sucursales (usuario_id, sucursal_id, es_predeterminada, asignado_por, asignado_en) VALUES
    -- Botica San Juan
    ('11111111-0000-0000-0000-000000000001', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000001', 'aaaa2222-aaaa-2222-aaaa-222222222222', FALSE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000005', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000005', 'aaaa2222-aaaa-2222-aaaa-222222222222', FALSE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000002', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000003', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('11111111-0000-0000-0000-000000000004', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),

    -- Farmacia Vida
    ('22222222-0000-0000-0000-000000000001', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000001', 'bbbb2222-bbbb-2222-bbbb-222222222222', FALSE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000005', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000005', 'bbbb2222-bbbb-2222-bbbb-222222222222', FALSE, 'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000002', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000003', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP),
    ('22222222-0000-0000-0000-000000000004', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE,  'SYSTEM', CURRENT_TIMESTAMP);
