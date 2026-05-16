-- =====================================================================
-- Pre-condición ejecutada por Spring antes que Hibernate cree las tablas.
-- Crea el schema y las extensiones necesarias (pgcrypto para BCrypt vía
-- crypt()/gen_salt(), uuid-ossp para uuid_generate_v4()).
-- =====================================================================

CREATE SCHEMA IF NOT EXISTS sec;

CREATE EXTENSION IF NOT EXISTS "pgcrypto"  WITH SCHEMA sec;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA sec;
