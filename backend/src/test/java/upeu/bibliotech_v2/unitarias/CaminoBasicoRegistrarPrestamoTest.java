package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import upeu.bibliotech_v2.exception.LibroNoEncontradoException;
import upeu.bibliotech_v2.exception.PrestamoNoPermitidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;
import upeu.bibliotech_v2.service.PrestamoService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Método del camino básico sobre registrarPrestamo() (RN-03).
 * Decisiones: D1 libro existe, D2 hay ejemplares, D3 estudiante ACTIVO, D4 menos de 3 préstamos activos.
 * V(G) = 4 decisiones + 1 = 5, por lo que hay 5 caminos independientes (un caso por camino).
 */
class CaminoBasicoRegistrarPrestamoTest {

    private static final long ESTUDIANTE_ID = 1L;
    private static final long LIBRO_ID = 10L;

    private EstudianteRepository estudianteRepository;
    private LibroRepository libroRepository;
    private PrestamoRepository prestamoRepository;
    private PrestamoService servicio;

    @BeforeEach
    void preparar() {
        estudianteRepository = mock(EstudianteRepository.class);
        libroRepository = mock(LibroRepository.class);
        prestamoRepository = mock(PrestamoRepository.class);
        servicio = new PrestamoService(estudianteRepository, libroRepository, prestamoRepository);
    }

    @Test
    @DisplayName("Camino 1: D1 falso, el libro no existe")
    void camino1_libroNoExiste() {
        // Arrange
        when(libroRepository.findById(LIBRO_ID)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(LibroNoEncontradoException.class,
                () -> servicio.registrarPrestamo(ESTUDIANTE_ID, LIBRO_ID));
    }

    @Test
    @DisplayName("Camino 2: D1 verdadero y D2 falso, el libro no tiene ejemplares")
    void camino2_sinEjemplares() {
        // Arrange
        when(libroRepository.findById(LIBRO_ID))
                .thenReturn(Optional.of(new Libro("ISBN-1", "Libro", "Autor", 0)));

        // Act
        PrestamoNoPermitidoException e = assertThrows(PrestamoNoPermitidoException.class,
                () -> servicio.registrarPrestamo(ESTUDIANTE_ID, LIBRO_ID));

        // Assert
        assertEquals("El libro no tiene ejemplares disponibles", e.getMessage());
    }

    @Test
    @DisplayName("Camino 3: D2 verdadero y D3 falso, el estudiante está INACTIVO")
    void camino3_estudianteInactivo() {
        // Arrange
        when(libroRepository.findById(LIBRO_ID))
                .thenReturn(Optional.of(new Libro("ISBN-1", "Libro", "Autor", 2)));
        when(estudianteRepository.findById(ESTUDIANTE_ID))
                .thenReturn(Optional.of(new Estudiante("70000001", "Ana", "INACTIVO")));

        // Act
        PrestamoNoPermitidoException e = assertThrows(PrestamoNoPermitidoException.class,
                () -> servicio.registrarPrestamo(ESTUDIANTE_ID, LIBRO_ID));

        // Assert
        assertEquals("El estudiante no está activo", e.getMessage());
    }

    @Test
    @DisplayName("Camino 4: D3 verdadero y D4 falso, el estudiante ya tiene 3 préstamos activos")
    void camino4_maximoDePrestamos() {
        // Arrange
        when(libroRepository.findById(LIBRO_ID))
                .thenReturn(Optional.of(new Libro("ISBN-1", "Libro", "Autor", 2)));
        when(estudianteRepository.findById(ESTUDIANTE_ID))
                .thenReturn(Optional.of(new Estudiante("70000001", "Ana", "ACTIVO")));
        when(prestamoRepository.countByEstudianteIdAndEstado(ESTUDIANTE_ID, "ACTIVO"))
                .thenReturn(3L);

        // Act
        PrestamoNoPermitidoException e = assertThrows(PrestamoNoPermitidoException.class,
                () -> servicio.registrarPrestamo(ESTUDIANTE_ID, LIBRO_ID));

        // Assert
        assertEquals("El estudiante alcanzó el máximo de 3 préstamos activos", e.getMessage());
    }

    @Test
    @DisplayName("Camino 5: D1, D2, D3 y D4 verdaderos, el préstamo se registra y baja un ejemplar")
    void camino5_prestamoExitoso() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Libro", "Autor", 2);
        when(libroRepository.findById(LIBRO_ID)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(ESTUDIANTE_ID))
                .thenReturn(Optional.of(new Estudiante("70000001", "Ana", "ACTIVO")));
        when(prestamoRepository.countByEstudianteIdAndEstado(ESTUDIANTE_ID, "ACTIVO"))
                .thenReturn(2L);
        when(prestamoRepository.save(any(Prestamo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // Act
        Prestamo prestamo = servicio.registrarPrestamo(ESTUDIANTE_ID, LIBRO_ID);

        // Assert
        assertEquals("ACTIVO", prestamo.getEstado());
        assertEquals(1, libro.getEjemplaresDisponibles());
    }
}
