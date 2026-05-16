-- =====================================================================
-- BFF Seguridad - Data de prueba
-- Contraseña universal de los usuarios demo: Demo2026!
-- (hash BCrypt generado por pgcrypto -> compatible con BCryptPasswordEncoder)
-- =====================================================================

SET search_path TO sec;

-- ---------------------------------------------------------------------
-- 1) Roles del catálogo
-- ---------------------------------------------------------------------
INSERT INTO sec.roles (id, nombre, descripcion) VALUES
    ('SUPER_ADMIN',    'Super Administrador',         'Acceso total al sistema, multiempresa.'),
    ('ADMIN_EMPRESA',  'Administrador de Empresa',    'Administra todas las sucursales de su empresa.'),
    ('ADMIN_SUCURSAL', 'Administrador de Sucursal',   'Administra una sucursal específica.'),
    ('FARMACEUTICO',   'Farmacéutico',                'Atención al cliente, recetas y dispensación.'),
    ('CAJERO',         'Cajero',                      'Operación del POS y registro de ventas.'),
    ('AUDITOR',        'Auditor',                     'Solo lectura sobre operaciones y bitácora.');

-- ---------------------------------------------------------------------
-- 2) Permisos 3D (rol x recurso x acciones x alcance)
--    El SUPER_ADMIN se modela con recurso='*' para evitar 50 filas.
-- ---------------------------------------------------------------------
INSERT INTO sec.permisos_rol_recurso (rol_id, recurso, acciones, alcance) VALUES
    -- SUPER_ADMIN: comodín
    ('SUPER_ADMIN',    '*',              ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),

    -- ADMIN_EMPRESA: control total dentro de su empresa, todas las sucursales
    ('ADMIN_EMPRESA',  'USER',           ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'BRANCH',         ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'PRODUCT',        ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'STOCK',          ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'SALE',           ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'CLIENT',         ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'SUPPLIER',       ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'PURCHASE_ORDER', ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[], 'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'PRESCRIPTION',   ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'REPORT',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('ADMIN_EMPRESA',  'AUDIT',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),

    -- ADMIN_SUCURSAL: solo su sucursal
    ('ADMIN_SUCURSAL', 'USER',           ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'BRANCH',         ARRAY['READ','UPDATE']::VARCHAR[],                   'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'PRODUCT',        ARRAY['READ','UPDATE']::VARCHAR[],                   'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'STOCK',          ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'SALE',           ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'CLIENT',         ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'SUPPLIER',       ARRAY['READ']::VARCHAR[],                            'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'PURCHASE_ORDER', ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'PRESCRIPTION',   ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('ADMIN_SUCURSAL', 'REPORT',         ARRAY['READ']::VARCHAR[],                            'OWN_BRANCH'),

    -- FARMACEUTICO: dispensación, clientes y recetas en su sucursal
    ('FARMACEUTICO',   'BRANCH',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('FARMACEUTICO',   'PRODUCT',        ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('FARMACEUTICO',   'STOCK',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('FARMACEUTICO',   'CLIENT',         ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('FARMACEUTICO',   'PRESCRIPTION',   ARRAY['READ','CREATE','UPDATE']::VARCHAR[],          'OWN_BRANCH'),
    ('FARMACEUTICO',   'SALE',           ARRAY['READ','CREATE']::VARCHAR[],                   'OWN_BRANCH'),

    -- CAJERO: POS y consulta de stock cruzado (sin update)
    ('CAJERO',         'BRANCH',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('CAJERO',         'SALE',           ARRAY['READ','CREATE']::VARCHAR[],                   'OWN_BRANCH'),
    ('CAJERO',         'PRODUCT',        ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('CAJERO',         'STOCK',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('CAJERO',         'CLIENT',         ARRAY['READ','CREATE']::VARCHAR[],                   'OWN_BRANCH'),

    -- AUDITOR: solo lectura, en toda la empresa
    ('AUDITOR',        'BRANCH',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'SALE',           ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'PRODUCT',        ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'STOCK',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'CLIENT',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'SUPPLIER',       ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'PURCHASE_ORDER', ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'REPORT',         ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES'),
    ('AUDITOR',        'AUDIT',          ARRAY['READ']::VARCHAR[],                            'ALL_BRANCHES');

-- ---------------------------------------------------------------------
-- 3) Empresas demo
-- ---------------------------------------------------------------------
INSERT INTO sec.empresas (id, ruc, razon_social, nombre_comercial, estado) VALUES
    ('11111111-1111-1111-1111-111111111111', '20100100100', 'Botica San Juan S.A.C.',  'Botica San Juan',  'ACTIVO'),
    ('22222222-2222-2222-2222-222222222222', '20200200200', 'Farmacia Vida E.I.R.L.',  'Farmacia Vida',    'ACTIVO');

-- ---------------------------------------------------------------------
-- 4) Sucursales (2 por empresa)
-- ---------------------------------------------------------------------
INSERT INTO sec.sucursales (id, empresa_id, codigo, nombre, direccion, distrito, provincia, departamento, telefono, es_principal) VALUES
    -- Botica San Juan
    ('aaaa1111-aaaa-1111-aaaa-111111111111', '11111111-1111-1111-1111-111111111111',
     'BSJ-001', 'Botica San Juan - Centro',  'Av. Larco 123',      'Miraflores', 'Lima',     'Lima',     '014441111', TRUE),
    ('aaaa2222-aaaa-2222-aaaa-222222222222', '11111111-1111-1111-1111-111111111111',
     'BSJ-002', 'Botica San Juan - Norte',   'Av. Tupac Amaru 500','Comas',      'Lima',     'Lima',     '014442222', FALSE),
    -- Farmacia Vida
    ('bbbb1111-bbbb-1111-bbbb-111111111111', '22222222-2222-2222-2222-222222222222',
     'FV-001',  'Farmacia Vida - Plaza',     'Jr. Junín 250',      'Cercado',    'Arequipa', 'Arequipa', '054301111', TRUE),
    ('bbbb2222-bbbb-2222-bbbb-222222222222', '22222222-2222-2222-2222-222222222222',
     'FV-002',  'Farmacia Vida - Cayma',     'Av. Cayma 800',      'Cayma',      'Arequipa', 'Arequipa', '054302222', FALSE);

-- ---------------------------------------------------------------------
-- 5) Usuarios. Contraseña 'Demo2026!' hasheada con BCrypt cost=10.
--    El hash $2a$ producido por pgcrypto es interoperable con
--    Spring BCryptPasswordEncoder.
-- ---------------------------------------------------------------------
INSERT INTO sec.usuarios
    (id, empresa_id, nombre_usuario, correo, contrasena_hash, nombres, apellidos, dni, telefono, canal_otp_preferido) VALUES
    -- SUPER_ADMIN (sin empresa)
    ('00000000-0000-0000-0000-000000000001', NULL,
     'superadmin', 'superadmin@demo.local',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Super', 'Admin', '00000001', '999000001', 'EMAIL'),

    -- Botica San Juan
    ('11111111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     'admin.bsj', 'admin@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Lucía', 'Ramírez', '70011111', '999110001', 'EMAIL'),
    ('11111111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
     'jefe.bsj.centro', 'jefe.centro@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Carlos', 'Mendoza', '70011112', '999110002', 'WHATSAPP'),
    ('11111111-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111',
     'farma.bsj', 'farma@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'María', 'Quispe', '70011113', '999110003', 'EMAIL'),
    ('11111111-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111',
     'cajero.bsj', 'cajero@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Jorge', 'Vargas', '70011114', '999110004', 'SMS'),
    ('11111111-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111',
     'auditor.bsj', 'auditor@boticasanjuan.com',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Patricia', 'Torres', '70011115', '999110005', 'EMAIL'),

    -- Farmacia Vida
    ('22222222-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222',
     'admin.fv', 'admin@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Renato', 'Salas', '70022221', '999220001', 'EMAIL'),
    ('22222222-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222',
     'jefe.fv.plaza', 'jefe.plaza@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Andrea', 'Flores', '70022222', '999220002', 'EMAIL'),
    ('22222222-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222',
     'farma.fv', 'farma@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Diego', 'Huamán', '70022223', '999220003', 'WHATSAPP'),
    ('22222222-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222',
     'cajero.fv', 'cajero@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Sofía', 'Ríos', '70022224', '999220004', 'SMS'),
    ('22222222-0000-0000-0000-000000000005', '22222222-2222-2222-2222-222222222222',
     'auditor.fv', 'auditor@farmaciavida.pe',
     crypt('Demo2026!', gen_salt('bf', 10)),
     'Manuel', 'Cárdenas', '70022225', '999220005', 'EMAIL');

-- ---------------------------------------------------------------------
-- 6) Asignación de roles
-- ---------------------------------------------------------------------
INSERT INTO sec.usuarios_roles (usuario_id, rol_id) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN'),
    -- Botica San Juan
    ('11111111-0000-0000-0000-000000000001', 'ADMIN_EMPRESA'),
    ('11111111-0000-0000-0000-000000000002', 'ADMIN_SUCURSAL'),
    ('11111111-0000-0000-0000-000000000003', 'FARMACEUTICO'),
    ('11111111-0000-0000-0000-000000000004', 'CAJERO'),
    ('11111111-0000-0000-0000-000000000005', 'AUDITOR'),
    -- Farmacia Vida
    ('22222222-0000-0000-0000-000000000001', 'ADMIN_EMPRESA'),
    ('22222222-0000-0000-0000-000000000002', 'ADMIN_SUCURSAL'),
    ('22222222-0000-0000-0000-000000000003', 'FARMACEUTICO'),
    ('22222222-0000-0000-0000-000000000004', 'CAJERO'),
    ('22222222-0000-0000-0000-000000000005', 'AUDITOR');

-- ---------------------------------------------------------------------
-- 7) Asignación de sucursales (con sucursal predeterminada)
--    SUPER_ADMIN no tiene usuarios_sucursales: se resuelve dinámicamente
--    al hacer switch-branch contra cualquier sucursal.
-- ---------------------------------------------------------------------
INSERT INTO sec.usuarios_sucursales (usuario_id, sucursal_id, es_predeterminada) VALUES
    -- Botica San Juan: ADMIN_EMPRESA y AUDITOR ven ambas sucursales
    ('11111111-0000-0000-0000-000000000001', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE),
    ('11111111-0000-0000-0000-000000000001', 'aaaa2222-aaaa-2222-aaaa-222222222222', FALSE),
    ('11111111-0000-0000-0000-000000000005', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE),
    ('11111111-0000-0000-0000-000000000005', 'aaaa2222-aaaa-2222-aaaa-222222222222', FALSE),
    -- ADMIN_SUCURSAL, FARMACEUTICO y CAJERO -> solo Centro
    ('11111111-0000-0000-0000-000000000002', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE),
    ('11111111-0000-0000-0000-000000000003', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE),
    ('11111111-0000-0000-0000-000000000004', 'aaaa1111-aaaa-1111-aaaa-111111111111', TRUE),

    -- Farmacia Vida: ADMIN_EMPRESA y AUDITOR ven ambas sucursales
    ('22222222-0000-0000-0000-000000000001', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE),
    ('22222222-0000-0000-0000-000000000001', 'bbbb2222-bbbb-2222-bbbb-222222222222', FALSE),
    ('22222222-0000-0000-0000-000000000005', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE),
    ('22222222-0000-0000-0000-000000000005', 'bbbb2222-bbbb-2222-bbbb-222222222222', FALSE),
    -- ADMIN_SUCURSAL, FARMACEUTICO y CAJERO -> solo Plaza
    ('22222222-0000-0000-0000-000000000002', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE),
    ('22222222-0000-0000-0000-000000000003', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE),
    ('22222222-0000-0000-0000-000000000004', 'bbbb1111-bbbb-1111-bbbb-111111111111', TRUE);
