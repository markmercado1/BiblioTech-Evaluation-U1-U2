-- Datos de prueba para BiblioTech CI

INSERT INTO estudiantes (id, dni, nombre, estado)
VALUES (1, '70000001', 'Juan Perez', 'ACTIVO');

INSERT INTO libros (id, isbn, titulo, autor, ejemplares_disponibles)
VALUES (1, '978-0-00-000001-1', 'Programacion Java', 'Deitel', 3);

INSERT INTO prestamos
    (id, estado, fecha_prestamo, estudiante_id, libro_id)
VALUES
    (1, 'ACTIVO', '2026-09-17', 1, 1),
    (12, 'ACTIVO', '2026-09-24', 1, 1);

-- Avanzar las secuencias de los IDs para que los registros nuevos no choquen con los de arriba
SELECT setval(pg_get_serial_sequence('estudiantes', 'id'), (SELECT MAX(id) FROM estudiantes));
SELECT setval(pg_get_serial_sequence('libros', 'id'), (SELECT MAX(id) FROM libros));
SELECT setval(pg_get_serial_sequence('prestamos', 'id'), (SELECT MAX(id) FROM prestamos));
