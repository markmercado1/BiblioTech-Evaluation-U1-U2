package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import upeu.bibliotech_v2.exception.PrestamoNoEncontradoException;
import upeu.bibliotech_v2.exception.PrestamoNoPermitidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;
import upeu.bibliotech_v2.service.PrestamoService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Método del camino básico sobre devolverLibro() (RN-04).
 * Decisiones: D1 el préstamo existe, D2 el préstamo está ACTIVO.
 * V(G) = 2 decisiones + 1 = 3, por lo que hay 3 caminos independientes (un caso por camino).
 */
class CaminoBasicoDevolverLibroTest {

    private static final long PRESTAMO_ID = 5L;

    private PrestamoRepository prestamoRepository;
    private PrestamoService servicio;

    @BeforeEach
    void preparar() {
        prestamoRepository = mock(PrestamoRepository.class);
        servicio = new PrestamoService(
                mock(EstudianteRepository.class), mock(LibroRepository.class), prestamoRepository);
    }

    private Prestamo prestamo(String estado, Libro libro) {
        return new Prestamo(new Estudiante("70000001", "Ana", "ACTIVO"), libro, LocalDate.now(), estado);
    }

    @Test
    @DisplayName("Camino 1: D1 falso, el préstamo no existe")
    void camino1_prestamoNoExiste() {
        // Arrange
        when(prestamoRepository.findById(PRESTAMO_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(PrestamoNoEncontradoException.class,
                () -> servicio.devolverLibro(PRESTAMO_ID));
    }

    @Test
    @DisplayName("Camino 2: D1 verdadero y D2 falso, el préstamo ya fue devuelto")
    void camino2_prestamoYaDevuelto() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Libro", "Autor", 1);
        when(prestamoRepository.findById(PRESTAMO_ID))
                .thenReturn(Optional.of(prestamo("DEVUELTO", libro)));

        // Act
        PrestamoNoPermitidoException e = assertThrows(PrestamoNoPermitidoException.class,
                () -> servicio.devolverLibro(PRESTAMO_ID));

        // Assert
        assertEquals("El préstamo ya fue devuelto", e.getMessage());
        assertEquals(1, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Camino 3: D1 y D2 verdaderos, sube un ejemplar y el préstamo pasa a DEVUELTO")
    void camino3_devolucionExitosa() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Libro", "Autor", 1);
        when(prestamoRepository.findById(PRESTAMO_ID))
                .thenReturn(Optional.of(prestamo("ACTIVO", libro)));
        when(prestamoRepository.save(any(Prestamo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // Act
        Prestamo devuelto = servicio.devolverLibro(PRESTAMO_ID);

        // Assert
        assertEquals("DEVUELTO", devuelto.getEstado());
        assertEquals(2, libro.getEjemplaresDisponibles());
    }
}
