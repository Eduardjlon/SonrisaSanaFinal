-- ============================================================
-- BASE DE DATOS SISTEMA SONRISASANA
-- ESQUEMA COMPLETO + USUARIOS INICIALES
-- ============================================================

-- =========================================
-- 1. ROLES
-- =========================================
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(200)
    );

INSERT INTO roles (id, nombre, descripcion) VALUES
                                                (1, 'ADMINISTRADOR', 'Acceso total al sistema'),
                                                (2, 'ODONTOLOGO', 'Acceso a agenda, pacientes y citas'),
                                                (3, 'RECEPCIONISTA', 'Acceso a agenda y registro de pacientes')
    ON CONFLICT (id) DO NOTHING;

-- =========================================
-- 2. USUARIOS
-- =========================================
CREATE TABLE IF NOT EXISTS usuarios (
                                        id SERIAL PRIMARY KEY,
                                        nombre_completo VARCHAR(120) NOT NULL,
    username VARCHAR(40) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    rol_id INTEGER REFERENCES roles(id),

    dia_inicio VARCHAR(20),
    dia_fin VARCHAR(20),
    hora_inicio TIME,
    hora_fin TIME
    );

-- USUARIO ADMINISTRADOR
INSERT INTO usuarios (id, nombre_completo, username, email, password_hash, activo, rol_id)
VALUES (1, 'Administrador General', 'admin', 'admin@sonrisasana.com', 'admin123', true, 1)
    ON CONFLICT (id) DO NOTHING;

-- USUARIO ODONTÓLOGO
INSERT INTO usuarios (id, nombre_completo, username, email, password_hash, activo, rol_id)
VALUES (2, 'Dr. Jorge Odontología', 'odontologo', 'odontologo@sonrisasana.com', 'odonto123', true, 2)
    ON CONFLICT (id) DO NOTHING;

-- USUARIO RECEPCIONISTA
INSERT INTO usuarios (id, nombre_completo, username, email, password_hash, activo, rol_id)
VALUES (3, 'Recepcionista General', 'recepcion', 'recepcion@sonrisasana.com', 'recep123', true, 3)
    ON CONFLICT (id) DO NOTHING;

-- =========================================
-- 3. ESPECIALIDADES ODONTOLÓGICAS
-- =========================================
CREATE TABLE IF NOT EXISTS especialidades (
                                              id SERIAL PRIMARY KEY,
                                              nombre VARCHAR(80) UNIQUE NOT NULL
    );

INSERT INTO especialidades (nombre) VALUES
                                        ('General'),
                                        ('Ortodoncia'),
                                        ('Endodoncia'),
                                        ('Periodoncia'),
                                        ('Cirugía Bucal'),
                                        ('Implantología')
    ON CONFLICT (nombre) DO NOTHING;

-- =========================================
-- 4. ESTADOS DE CITA
-- =========================================
CREATE TABLE IF NOT EXISTS estado_cita (
                                           id SERIAL PRIMARY KEY,
                                           nombre VARCHAR(30) UNIQUE NOT NULL
    );

INSERT INTO estado_cita (nombre) VALUES
                                     ('PENDIENTE'),
                                     ('CONFIRMADA'),
                                     ('CANCELADA'),
                                     ('REPROGRAMADA'),
                                     ('ATENDIDA')
    ON CONFLICT (nombre) DO NOTHING;

-- =========================================
-- 5. PACIENTES
-- =========================================
CREATE TABLE IF NOT EXISTS pacientes (
                                         id SERIAL PRIMARY KEY,
                                         nombre_completo VARCHAR(120) NOT NULL,
    dpi VARCHAR(20) UNIQUE NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(150),
    alergias VARCHAR(250),
    condiciones_medicas VARCHAR(300),
    observaciones VARCHAR(300),
    fecha_creacion_expediente TIMESTAMP NOT NULL,
    ultima_actualizacion TIMESTAMP
    );

-- =========================================
-- 6. TRATAMIENTOS
-- =========================================
CREATE TABLE IF NOT EXISTS tratamientos (
                                            id SERIAL PRIMARY KEY,
                                            nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255),
    duracion_minutos INT NOT NULL,
    costo NUMERIC(10,2) NOT NULL,
    especialidad VARCHAR(80) REFERENCES especialidades(nombre)
    );

-- =========================================
-- 7. CITAS
-- =========================================
CREATE TABLE IF NOT EXISTS citas (
                                     id SERIAL PRIMARY KEY,
                                     paciente_id INT NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    odontologo_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tratamiento_id INT NOT NULL REFERENCES tratamientos(id) ON DELETE CASCADE,

    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,

    estado VARCHAR(30) REFERENCES estado_cita(nombre),
    notas VARCHAR(500),

    precio_base NUMERIC(10,2) NOT NULL DEFAULT 300.00,
    precio_tratamiento NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    total NUMERIC(10,2) NOT NULL DEFAULT 300.00
    );

-- =========================================
-- 8. HISTORIAL DE REPROGRAMACIONES
-- =========================================
CREATE TABLE IF NOT EXISTS historial_reprogramacion (
                                                        id SERIAL PRIMARY KEY,
                                                        cita_id INT NOT NULL REFERENCES citas(id) ON DELETE CASCADE,
    fecha_anterior_inicio TIMESTAMP,
    fecha_anterior_fin TIMESTAMP,
    fecha_nueva_inicio TIMESTAMP,
    fecha_nueva_fin TIMESTAMP,
    motivo VARCHAR(255),
    fecha_registro TIMESTAMP DEFAULT NOW()
    );

-- =========================================
-- 9. TIPOS DE ARCHIVO
-- =========================================
CREATE TABLE IF NOT EXISTS tipos_archivo (
                                             id SERIAL PRIMARY KEY,
                                             nombre VARCHAR(50) UNIQUE NOT NULL
    );

INSERT INTO tipos_archivo (nombre) VALUES
                                       ('IMAGEN'),
                                       ('PDF'),
                                       ('RADIOGRAFIA'),
                                       ('DOCUMENTO')
    ON CONFLICT (nombre) DO NOTHING;

-- =========================================
-- 10. ARCHIVOS CLÍNICOS
-- =========================================
CREATE TABLE IF NOT EXISTS archivos_clinicos (
                                                 id SERIAL PRIMARY KEY,
                                                 paciente_id INT NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    tipo_id INT NOT NULL REFERENCES tipos_archivo(id),
    nombre_archivo VARCHAR(150),
    url TEXT NOT NULL,
    fecha_carga TIMESTAMP DEFAULT NOW(),
    descripcion VARCHAR(255)
    );

-- =========================================
-- 11. CUPONES
-- =========================================
CREATE TABLE IF NOT EXISTS cupones (
                                       id SERIAL PRIMARY KEY,
                                       codigo VARCHAR(30) UNIQUE NOT NULL,
    descripcion VARCHAR(200),
    porcentaje INT CHECK (porcentaje BETWEEN 1 AND 100),
    activo BOOLEAN DEFAULT TRUE
    );

-- =========================================
-- 12. SEGUROS
-- =========================================
CREATE TABLE IF NOT EXISTS seguros (
                                       id SERIAL PRIMARY KEY,
                                       nombre VARCHAR(120) UNIQUE NOT NULL
    );

INSERT INTO seguros (nombre) VALUES
                                 ('INTEGRO'),
                                 ('G&T'),
                                 ('Seguros Universales')
    ON CONFLICT (nombre) DO NOTHING;

-- =========================================
-- 13. FACTURAS
-- =========================================
CREATE TABLE IF NOT EXISTS facturas (
                                        id SERIAL PRIMARY KEY,
                                        paciente_id INT REFERENCES pacientes(id) ON DELETE CASCADE,
    cita_id INT REFERENCES citas(id) ON DELETE SET NULL,

    subtotal NUMERIC(10,2) NOT NULL,
    descuento NUMERIC(10,2) DEFAULT 0,
    seguro_cubre NUMERIC(10,2) DEFAULT 0,
    total NUMERIC(10,2) NOT NULL,

    estado VARCHAR(30) DEFAULT 'PENDIENTE',
    fecha_emision TIMESTAMP DEFAULT NOW(),

    cupon_id INT REFERENCES cupones(id),
    seguro_id INT REFERENCES seguros(id)
    );

-- =========================================
-- 14. PAGOS PARCIALES
-- =========================================
CREATE TABLE IF NOT EXISTS pagos (
                                     id SERIAL PRIMARY KEY,
                                     factura_id INT REFERENCES facturas(id) ON DELETE CASCADE,
    monto NUMERIC(10,2) NOT NULL,
    metodo VARCHAR(50),
    fecha TIMESTAMP DEFAULT NOW()
    );
