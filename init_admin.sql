-- ============================================================
-- CREACIÓN DE ROLES
-- ============================================================
INSERT INTO roles (id, nombre, descripcion) VALUES
    (1, 'ADMINISTRADOR', 'Acceso total al sistema'),
    (2, 'ODONTOLOGO', 'Acceso a agenda, pacientes y citas'),
    (3, 'RECEPCIONISTA', 'Acceso a agenda y registro de pacientes')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- USUARIO ADMINISTRADOR
-- ============================================================
INSERT INTO usuarios (id, nombre_completo, username, email, password_hash, activo, rol_id)
VALUES (
    1,
    'Administrador General',
    'admin',
    'admin@sonrisasana.com',
    'admin123',
    true,
    1
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- USUARIO ODONTÓLOGO
-- ============================================================
INSERT INTO usuarios (id, nombre_completo, username, email, password_hash, activo, rol_id)
VALUES (
    2,
    'Dr. Jorge Odontología',
    'odontologo',
    'odontologo@sonrisasana.com',
    'odonto123',
    true,
    2
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- USUARIO RECEPCIONISTA
-- ============================================================
INSERT INTO usuarios (id, nombre_completo, username, email, password_hash, activo, rol_id)
VALUES (
    3,
    'Recepcionista General',
    'recepcion',
    'recepcion@sonrisasana.com',
    'recep123',
    true,
    3
)
ON CONFLICT (id) DO NOTHING;
