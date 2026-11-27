package upeu.bibliotech_v2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import upeu.bibliotech_v2.exception.EstudianteNoEncontradoException;
import upeu.bibliotech_v2.model.Prestamo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba con PostgreSQL real que la consulta de préstamos activos funciona. */
@SpringBootTest
class PrestamoServiceActivosIntegrationTest {

    @Autowired
    private PrestamoService prestamoService;

    @Test
    void debeConsultarLosPrestamosActivosGuardadosEnLaBase() {
        // Arrange: los datos de prueba incluyen al estudiante 1 con dos préstamos ACTIVO.
        // Act
        List<Prestamo> resultado = prestamoService.listarPrestamosActivos(1L);

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> "ACTIVO".equals(p.getEstado())));
    }

    @Test
    void debeRechazarUnEstudianteInexistente() {
        assertThrows(EstudianteNoEncontradoException.class,
                () -> prestamoService.listarPrestamosActivos(999L));
    }
}
