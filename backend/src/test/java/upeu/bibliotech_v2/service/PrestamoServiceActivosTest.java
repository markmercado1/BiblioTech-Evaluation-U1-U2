package upeu.bibliotech_v2.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.bibliotech_v2.exception.EstudianteNoEncontradoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de la funcionalidad "consulta de préstamos activos de un estudiante". */
@ExtendWith(MockitoExtension.class)
class PrestamoServiceActivosTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private LibroRepository libroRepository;
    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    @Test
    @DisplayName("Escenario válido: devuelve solo los préstamos ACTIVO del estudiante")
    void debeDevolverLosPrestamosActivosDelEstudiante() {
        // Arrange
        Estudiante estudiante = new Estudiante("Juan Perez", "ACTIVO");
        Libro libro = new Libro("Programacion Java", "Deitel", 3);
        List<Prestamo> activos = List.of(
                new Prestamo(estudiante, libro, LocalDate.of(2026, 9, 17), "ACTIVO"),
                new Prestamo(estudiante, libro, LocalDate.of(2026, 9, 24), "ACTIVO"));
        when(estudianteRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.findByEstudianteIdAndEstado(1L, "ACTIVO")).thenReturn(activos);

        // Act
        List<Prestamo> resultado = prestamoService.listarPrestamosActivos(1L);

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> "ACTIVO".equals(p.getEstado())));
        verify(prestamoRepository).findByEstudianteIdAndEstado(1L, "ACTIVO");
    }

    @Test
    @DisplayName("Escenario válido: un estudiante sin préstamos activos recibe una lista vacía")
    void debeDevolverListaVaciaSiNoTienePrestamosActivos() {
        // Arrange
        when(estudianteRepository.existsById(2L)).thenReturn(true);
        when(prestamoRepository.findByEstudianteIdAndEstado(2L, "ACTIVO")).thenReturn(List.of());

        // Act
        List<Prestamo> resultado = prestamoService.listarPrestamosActivos(2L);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Escenario inválido: un estudiante inexistente lanza excepción y no consulta préstamos")
    void debeLanzarExcepcionSiElEstudianteNoExiste() {
        // Arrange
        when(estudianteRepository.existsById(999L)).thenReturn(false);

        // Act + Assert
        EstudianteNoEncontradoException excepcion = assertThrows(
                EstudianteNoEncontradoException.class,
                () -> prestamoService.listarPrestamosActivos(999L));
        assertEquals("Estudiante no existe", excepcion.getMessage());
        verify(prestamoRepository, never()).findByEstudianteIdAndEstado(anyLong(), anyString());
    }
}
