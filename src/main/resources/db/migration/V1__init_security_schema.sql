-- =====================================================================
-- BFF Seguridad - Esquema inicial
-- Base de datos : sg-seguridad
-- Esquema       : sec
-- Stack         : PostgreSQL 16+, hash BCrypt vía pgcrypto, UUID v4.
-- =====================================================================

CREATE SCHEMA IF NOT EXISTS sec;
SET search_path TO sec;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ---------------------------------------------------------------------
-- empresas: cliente del SaaS (multi-tenant a nivel de fila).
-- ---------------------------------------------------------------------
CREATE TABLE sec.empresas (
    id                  UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    ruc                 VARCHAR(11)  NOT NULL UNIQUE,
    razon_social        VARCHAR(200) NOT NULL,
    nombre_comercial    VARCHAR(200),
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
                        CHECK (estado IN ('ACTIVO','INACTIVO','SUSPENDIDO')),
    fecha_alta          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por          VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por      VARCHAR(50),
    fecha_modificacion  TIMESTAMP
);

-- ---------------------------------------------------------------------
-- sucursales: pertenecen a una empresa.
-- ---------------------------------------------------------------------
CREATE TABLE sec.sucursales (
    id                  UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id          UUID         NOT NULL REFERENCES sec.empresas(id) ON DELETE RESTRICT,
    codigo              VARCHAR(20)  NOT NULL,
    nombre              VARCHAR(150) NOT NULL,
    direccion           VARCHAR(300),
    distrito            VARCHAR(100),
    provincia           VARCHAR(100),
    departamento        VARCHAR(100),
    telefono            VARCHAR(20),
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
                        CHECK (estado IN ('ACTIVO','INACTIVO','CERRADA')),
    es_principal        BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_por          VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por      VARCHAR(50),
    fecha_modificacion  TIMESTAMP,
    CONSTRAINT uq_sucursales_empresa_codigo UNIQUE (empresa_id, codigo)
);
CREATE INDEX idx_sucursales_empresa ON sec.sucursales (empresa_id);

-- ---------------------------------------------------------------------
-- usuarios: cuentas que se autentican. SUPER_ADMIN va con empresa_id NULL.
-- ---------------------------------------------------------------------
CREATE TABLE sec.usuarios (
    id                       UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id               UUID         NULL REFERENCES sec.empresas(id) ON DELETE RESTRICT,
    nombre_usuario           VARCHAR(50)  NOT NULL UNIQUE,
    correo                   VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash          VARCHAR(255) NOT NULL,
    nombres                  VARCHAR(100) NOT NULL,
    apellidos                VARCHAR(100) NOT NULL,
    dni                      VARCHAR(15),
    telefono                 VARCHAR(20),
    canal_otp_preferido      VARCHAR(20)  NOT NULL DEFAULT 'EMAIL'
                             CHECK (canal_otp_preferido IN ('EMAIL','SMS','WHATSAPP')),
    mfa_habilitado           BOOLEAN      NOT NULL DEFAULT TRUE,
    estado                   VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO'
                             CHECK (estado IN ('ACTIVO','BLOQUEADO','INACTIVO','PENDIENTE_VERIFICACION')),
    intentos_fallidos        INT          NOT NULL DEFAULT 0,
    bloqueado_hasta          TIMESTAMP    NULL,
    ultimo_acceso            TIMESTAMP    NULL,
    contrasena_cambiada_en   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por               VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    fecha_creacion           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por           VARCHAR(50),
    fecha_modificacion       TIMESTAMP
);
CREATE INDEX idx_usuarios_empresa ON sec.usuarios (empresa_id);
CREATE INDEX idx_usuarios_estado  ON sec.usuarios (estado);

-- ---------------------------------------------------------------------
-- roles: catálogo cerrado. id en mayúsculas (alineado al claim del JWT).
-- ---------------------------------------------------------------------
CREATE TABLE sec.roles (
    id                  VARCHAR(30)  PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    descripcion         VARCHAR(300),
    es_sistema          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por          VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por      VARCHAR(50),
    fecha_modificacion  TIMESTAMP
);

-- ---------------------------------------------------------------------
-- usuarios_roles: N:N usuarios <-> roles.
-- ---------------------------------------------------------------------
CREATE TABLE sec.usuarios_roles (
    usuario_id    UUID        NOT NULL REFERENCES sec.usuarios(id) ON DELETE CASCADE,
    rol_id        VARCHAR(30) NOT NULL REFERENCES sec.roles(id)    ON DELETE RESTRICT,
    asignado_por  VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    asignado_en   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, rol_id)
);
CREATE INDEX idx_usuarios_roles_rol ON sec.usuarios_roles (rol_id);

-- ---------------------------------------------------------------------
-- usuarios_sucursales: sucursales habilitadas para el usuario.
-- es_predeterminada=TRUE marca la sucursal activa al hacer login.
-- ---------------------------------------------------------------------
CREATE TABLE sec.usuarios_sucursales (
    usuario_id        UUID        NOT NULL REFERENCES sec.usuarios(id)   ON DELETE CASCADE,
    sucursal_id       UUID        NOT NULL REFERENCES sec.sucursales(id) ON DELETE RESTRICT,
    es_predeterminada BOOLEAN     NOT NULL DEFAULT FALSE,
    asignado_por      VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    asignado_en       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, sucursal_id)
);
CREATE UNIQUE INDEX uq_usuarios_sucursales_predeterminada
    ON sec.usuarios_sucursales (usuario_id) WHERE es_predeterminada = TRUE;

-- ---------------------------------------------------------------------
-- permisos_rol_recurso: modelo 3D rol x recurso x (acciones, alcance).
-- acciones: subset de {READ, CREATE, UPDATE, DELETE}.
-- alcance:  OWN_BRANCH | ALL_BRANCHES.
-- ---------------------------------------------------------------------
CREATE TABLE sec.permisos_rol_recurso (
    id                  UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    rol_id              VARCHAR(30)   NOT NULL REFERENCES sec.roles(id) ON DELETE CASCADE,
    recurso             VARCHAR(50)   NOT NULL
                        CHECK (recurso IN ('PRODUCT','STOCK','SALE','CLIENT','USER','BRANCH',
                                           'REPORT','SUPPLIER','PURCHASE_ORDER','PRESCRIPTION',
                                           'AUDIT','*')),
    acciones            VARCHAR(20)[] NOT NULL
                        CHECK (
                            array_length(acciones, 1) > 0
                            AND acciones <@ ARRAY['READ','CREATE','UPDATE','DELETE']::VARCHAR[]
                        ),
    alcance             VARCHAR(20)   NOT NULL
                        CHECK (alcance IN ('OWN_BRANCH','ALL_BRANCHES')),
    creado_por          VARCHAR(50)   NOT NULL DEFAULT 'SYSTEM',
    fecha_creacion      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por      VARCHAR(50),
    fecha_modificacion  TIMESTAMP,
    CONSTRAINT uq_permisos_rol_recurso UNIQUE (rol_id, recurso)
);
CREATE INDEX idx_permisos_rol ON sec.permisos_rol_recurso (rol_id);

-- ---------------------------------------------------------------------
-- tokens: registro de JTI emitidos. JwtAuthenticationFilter valida
-- contra esta tabla antes de construir el SecurityContext.
--
-- Modelo de sesión:
--   * Cada login genera una sesion_id (UUID).
--   * El access token y el refresh token emitidos en ese login comparten
--     la misma sesion_id.
--   * Al hacer logout se revocan TODOS los tokens activos de esa sesion_id
--     en una sola operación: el dispositivo queda incapaz de renovar.
--   * Logout global ("cerrar sesión en todos los dispositivos") = revocar
--     todos los tokens activos del usuario_id, sin filtrar por sesion_id.
--   * Branch switch revoca la sesion_id actual y emite una nueva.
-- ---------------------------------------------------------------------
CREATE TABLE sec.tokens (
    jti                 UUID        PRIMARY KEY,
    sesion_id           UUID        NOT NULL,
    usuario_id          UUID        NOT NULL REFERENCES sec.usuarios(id) ON DELETE CASCADE,
    empresa_id          UUID        NULL REFERENCES sec.empresas(id),
    sucursal_id         UUID        NULL REFERENCES sec.sucursales(id),
    tipo                VARCHAR(20) NOT NULL CHECK (tipo IN ('ACCESS','REFRESH')),
    jti_padre           UUID        NULL,
    huella_dispositivo  VARCHAR(255),
    direccion_ip        VARCHAR(45),
    agente_usuario      VARCHAR(500),
    emitido_en          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en           TIMESTAMP   NOT NULL,
    revocado            BOOLEAN     NOT NULL DEFAULT FALSE,
    revocado_en         TIMESTAMP   NULL,
    motivo_revocacion   VARCHAR(50) NULL
                        CHECK (motivo_revocacion IN
                              ('LOGOUT','LOGOUT_ALL','BRANCH_SWITCH','REFRESH_USED',
                               'ADMIN_REVOKE','PASSWORD_CHANGED','USER_BLOCKED',
                               'SECURITY_INCIDENT'))
);
CREATE INDEX idx_tokens_usuario        ON sec.tokens (usuario_id);
CREATE INDEX idx_tokens_sesion_activos ON sec.tokens (sesion_id) WHERE revocado = FALSE;
CREATE INDEX idx_tokens_activos        ON sec.tokens (jti) WHERE revocado = FALSE;
CREATE INDEX idx_tokens_expira         ON sec.tokens (expira_en);

-- ---------------------------------------------------------------------
-- codigos_otp: códigos OTP. codigo_hash en BCrypt, no en claro.
-- Verificación con SELECT ... FOR UPDATE (PESSIMISTIC_WRITE en JPA).
-- ---------------------------------------------------------------------
CREATE TABLE sec.codigos_otp (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id        UUID         NOT NULL REFERENCES sec.usuarios(id) ON DELETE CASCADE,
    codigo_hash       VARCHAR(255) NOT NULL,
    canal             VARCHAR(20)  NOT NULL CHECK (canal IN ('EMAIL','SMS','WHATSAPP')),
    proposito         VARCHAR(30)  NOT NULL
                      CHECK (proposito IN ('LOGIN_2FA','RESET_PASSWORD','CHANGE_EMAIL','CHANGE_PHONE')),
    intentos          INT          NOT NULL DEFAULT 0,
    intentos_maximos  INT          NOT NULL DEFAULT 3,
    expira_en         TIMESTAMP    NOT NULL,
    usado_en          TIMESTAMP    NULL,
    direccion_ip      VARCHAR(45),
    fecha_creacion    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_codigos_otp_usuario_proposito_activos
    ON sec.codigos_otp (usuario_id, proposito) WHERE usado_en IS NULL;
CREATE INDEX idx_codigos_otp_expira ON sec.codigos_otp (expira_en);

-- ---------------------------------------------------------------------
-- reset_sessions: sesiones temporales para el flujo forgot-password
-- (no autenticado). Estados: PENDIENTE → VERIFICADO → USADO.
-- ---------------------------------------------------------------------
CREATE TABLE sec.reset_sessions (
    id         UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID        NOT NULL REFERENCES sec.usuarios(id) ON DELETE CASCADE,
    estado     VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
               CHECK (estado IN ('PENDIENTE','VERIFICADO','USADO')),
    ip_origen  VARCHAR(45),
    creado_en  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en  TIMESTAMP   NOT NULL
);
CREATE INDEX idx_reset_sessions_usuario ON sec.reset_sessions (usuario_id);

-- ---------------------------------------------------------------------
-- bitacora_auditoria: tabla append-only para cambios de entidad y eventos
-- de seguridad (login, otp, branch switch, token revoked).
-- valor_anterior / valor_nuevo como JSONB para diferencias fáciles.
-- ---------------------------------------------------------------------
CREATE TABLE sec.bitacora_auditoria (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id      UUID         NULL,
    sucursal_id     UUID         NULL,
    usuario_id      UUID         NULL,
    nombre_usuario  VARCHAR(50)  NULL,
    direccion_ip    VARCHAR(45),
    agente_usuario  VARCHAR(500),
    tipo_entidad    VARCHAR(100) NOT NULL,
    entidad_id      VARCHAR(100) NOT NULL,
    accion          VARCHAR(40)  NOT NULL
                    CHECK (accion IN
                          ('CREATE','UPDATE','DELETE',
                           'LOGIN_SUCCESS','LOGIN_FAILED','LOGOUT',
                           'OTP_REQUESTED','OTP_VERIFIED','OTP_FAILED',
                           'TOKEN_ISSUED','TOKEN_REVOKED','TOKEN_REFRESHED',
                           'BRANCH_SWITCHED','USER_BLOCKED','USER_UNBLOCKED',
                           'PASSWORD_CHANGED','PERMISSION_DENIED')),
    valor_anterior  JSONB        NULL,
    valor_nuevo     JSONB        NULL,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_bitacora_empresa_fecha   ON sec.bitacora_auditoria (empresa_id, fecha_creacion DESC);
CREATE INDEX idx_bitacora_entidad         ON sec.bitacora_auditoria (tipo_entidad, entidad_id);
CREATE INDEX idx_bitacora_usuario_fecha   ON sec.bitacora_auditoria (usuario_id, fecha_creacion DESC);
CREATE INDEX idx_bitacora_accion          ON sec.bitacora_auditoria (accion, fecha_creacion DESC);
