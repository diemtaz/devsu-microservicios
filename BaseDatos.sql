-- ═══════════════════════════════════════════════════════════════════════════
-- BaseDatos.sql — Devsu Prueba Técnica
-- ═══════════════════════════════════════════════════════════════════════════
-- ─── SECCIÓN MS-CLIENTES ────────────────────────────────────────────────────
DO $$ 
BEGIN 
    IF (SELECT current_database()) = 'db_clientes' THEN 

        CREATE TABLE IF NOT EXISTS persona (
            id             BIGSERIAL PRIMARY KEY,
            nombre         VARCHAR(100) NOT NULL,
            genero         VARCHAR(20)  NOT NULL,
            edad           INTEGER      NOT NULL CHECK (edad > 0),
            identificacion VARCHAR(20)  NOT NULL UNIQUE,
            direccion      VARCHAR(200) NOT NULL,
            telefono       VARCHAR(20)  NOT NULL
        );

        CREATE TABLE IF NOT EXISTS cliente (
            persona_id  BIGINT       PRIMARY KEY REFERENCES persona(id) ON DELETE CASCADE,
            cliente_id  VARCHAR(50)  NOT NULL UNIQUE,
            contrasena  VARCHAR(100) NOT NULL,
            estado      BOOLEAN      NOT NULL DEFAULT TRUE
        );

    END IF; 
END $$;

-- ─── SECCIÓN MS-CUENTAS ─────────────────────────────────────────────────────
DO $$ 
BEGIN 
    IF (SELECT current_database()) = 'db_cuentas' THEN 

        -- cliente_ref: referencia local sincronizada desde MS-Clientes via RabbitMQ
        CREATE TABLE IF NOT EXISTS cliente_ref (
            id         BIGSERIAL   PRIMARY KEY,
            cliente_id VARCHAR(50) NOT NULL UNIQUE,
            nombre     VARCHAR(100) NOT NULL
        );

        -- cuenta
        CREATE TABLE IF NOT EXISTS cuenta (
            id               BIGSERIAL       PRIMARY KEY,
            numero_cuenta    VARCHAR(20)     NOT NULL UNIQUE,
            tipo_cuenta      VARCHAR(20)     NOT NULL,
            saldo_inicial    NUMERIC(15,2)   NOT NULL DEFAULT 0,
            saldo_disponible NUMERIC(15,2)   NOT NULL DEFAULT 0,
            estado           BOOLEAN         NOT NULL DEFAULT TRUE,
            cliente_id       VARCHAR(50)     NOT NULL
        );

        -- movimiento
        CREATE TABLE IF NOT EXISTS movimiento (
            id               BIGSERIAL       PRIMARY KEY,
            fecha            TIMESTAMP       NOT NULL DEFAULT NOW(),
            tipo_movimiento  VARCHAR(10)     NOT NULL,
            valor            NUMERIC(15,2)   NOT NULL,
            saldo            NUMERIC(15,2)   NOT NULL,
            cuenta_id        BIGINT          NOT NULL REFERENCES cuenta(id) ON DELETE CASCADE
        );

    END IF; 
END $$;