package upeu.bibliotech_v2.integracion;

import upeu.bibliotech_v2.service.PrestamoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración de PrestamoService con datos reales en PostgreSQL.
 * Cada prueba inserta sus propios datos y se revierte al terminar (@Transactional),
 * por lo que no depende del orden de ejecución ni deja registros.
 */
@SpringBootTest
@Transactional
class PrestamoServiceCincoCasosIntegrationTest {

    private static final long LIBRO_ID = 100L;

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private JdbcTemplate jdbc;

    private void crearEstudiante(long id, String estado) {
        jdbc.update("INSERT INTO estudiantes (id, nombre, estado) VALUES (?, ?, ?)",
                id, "Estudiante " + id, estado);
    }

    private void crearLibro(long id, int ejemplares) {
        jdbc.update("INSERT INTO libros (id, titulo, autor, ejemplares_disponibles) VALUES (?, ?, ?, ?)",
                id, "Libro " + id, "Autor de prueba", ejemplares);
    }

    private void crearPrestamosActivos(long estudianteId, long libroId, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            jdbc.update("INSERT INTO prestamos (id, estado, fecha_prestamo, estudiante_id, libro_id) "
                            + "VALUES (?, 'ACTIVO', CURRENT_DATE, ?, ?)",
                    1000L + estudianteId * 10 + i, estudianteId, libroId);
        }
    }

    @Test
    @DisplayName("CP01: estudiante inexistente lanza excepción")
    void cp01_estudianteNoExisteLanzaExcepcion() {
        // Arrange
        long estudianteInexistente = 9999L;
        crearLibro(LIBRO_ID, 5);

        // Act + Assert
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> prestamoService.puedePrestar(estudianteInexistente, LIBRO_ID));
        assertEquals("Estudiante no existe", excepcion.getMessage());
    }

    @Test
    @DisplayName("CP02: estudiante INACTIVO no puede prestar")
    void cp02_estudianteInactivoNoPuedePrestar() {
        // Arrange
        crearEstudiante(200L, "INACTIVO");
        crearLibro(LIBRO_ID, 5);

        // Act
        boolean resultado = prestamoService.puedePrestar(200L, LIBRO_ID);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("CP03: estudiante ACTIVO con 3 préstamos activos no puede prestar")
    void cp03_maximoDePrestamosAlcanzado() {
        // Arrange
        crearEstudiante(300L, "ACTIVO");
        crearLibro(LIBRO_ID, 5);
        crearPrestamosActivos(300L, LIBRO_ID, 3);

        // Act
        boolean resultado = prestamoService.puedePrestar(300L, LIBRO_ID);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("CP04: estudiante ACTIVO sin ejemplares disponibles no puede prestar")
    void cp04_sinEjemplaresDisponibles() {
        // Arrange
        crearEstudiante(400L, "ACTIVO");
        crearLibro(LIBRO_ID, 0);

        // Act
        boolean resultado = prestamoService.puedePrestar(400L, LIBRO_ID);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("CP05: estudiante ACTIVO, sin préstamos y con ejemplares, puede prestar")
    void cp05_prestamoAutorizado() {
        // Arrange
        crearEstudiante(500L, "ACTIVO");
        crearLibro(LIBRO_ID, 5);

        // Act
        boolean resultado = prestamoService.puedePrestar(500L, LIBRO_ID);

        // Assert
        assertTrue(resultado);
    }
}
