package upeu.bibliotech_v2.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.bibliotech_v2.exception.LibroNoEncontradoException;
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
import static org.mockito.Mockito.*;

/** PrestamoService con repositorios simulados: RN-03 (registrar) y RN-04 (devolver). */
@ExtendWith(MockitoExtension.class)
class PrestamoServiceMockitoTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private LibroRepository libroRepository;
    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    private Estudiante estudianteActivo() {
        return new Estudiante("70000001", "Ana", "ACTIVO");
    }

    @Test
    @DisplayName("Registrar préstamo exitoso: baja un ejemplar y guarda libro y préstamo")
    void registrarPrestamoExitosoGuardaLibroYPrestamo() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Libro", "Autor", 2);
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo()));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, "ACTIVO")).thenReturn(0L);
        when(prestamoRepository.save(any(Prestamo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // Act
        Prestamo prestamo = prestamoService.registrarPrestamo(1L, 10L);

        // Assert
        assertEquals("ACTIVO", prestamo.getEstado());
        assertEquals(1, libro.getEjemplaresDisponibles());
        verify(libroRepository).save(libro);
        verify(prestamoRepository).save(any(Prestamo.class));
    }

    @Test
    @DisplayName("Registrar préstamo sin ejemplares: se rechaza y no se guarda nada")
    void registrarPrestamoSinEjemplaresNoGuarda() {
        // Arrange
        when(libroRepository.findById(10L))
                .thenReturn(Optional.of(new Libro("ISBN-1", "Libro", "Autor", 0)));

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.registrarPrestamo(1L, 10L));
        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar préstamo con libro inexistente: se rechaza y no se guarda nada")
    void registrarPrestamoConLibroInexistenteNoGuarda() {
        // Arrange
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(LibroNoEncontradoException.class,
                () -> prestamoService.registrarPrestamo(1L, 99L));
        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar préstamo con estudiante INACTIVO: se rechaza y no se guarda nada")
    void registrarPrestamoConEstudianteInactivoNoGuarda() {
        // Arrange
        when(libroRepository.findById(10L))
                .thenReturn(Optional.of(new Libro("ISBN-1", "Libro", "Autor", 2)));
        when(estudianteRepository.findById(1L))
                .thenReturn(Optional.of(new Estudiante("70000001", "Ana", "INACTIVO")));

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.registrarPrestamo(1L, 10L));
        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar préstamo con 3 préstamos activos: se rechaza y no se guarda nada")
    void registrarPrestamoConTresActivosNoGuarda() {
        // Arrange
        when(libroRepository.findById(10L))
                .thenReturn(Optional.of(new Libro("ISBN-1", "Libro", "Autor", 2)));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo()));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, "ACTIVO")).thenReturn(3L);

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.registrarPrestamo(1L, 10L));
        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validar préstamo (pantalla): con libro inexistente lanza excepción")
    void puedePrestarConLibroInexistenteLanzaExcepcion() {
        // Arrange
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo()));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, "ACTIVO")).thenReturn(0L);
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        RuntimeException e = assertThrows(RuntimeException.class,
                () -> prestamoService.puedePrestar(1L, 99L));

        // Assert
        assertEquals("Libro no existe", e.getMessage());
    }

    @Test
    @DisplayName("Devolver libro exitoso: sube un ejemplar y el préstamo pasa a DEVUELTO")
    void devolverLibroExitosoGuardaCambios() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Libro", "Autor", 0);
        Prestamo prestamo = new Prestamo(estudianteActivo(), libro, LocalDate.now(), "ACTIVO");
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(prestamo)).thenReturn(prestamo);

        // Act
        Prestamo resultado = prestamoService.devolverLibro(5L);

        // Assert
        assertEquals("DEVUELTO", resultado.getEstado());
        assertEquals(1, libro.getEjemplaresDisponibles());
        verify(libroRepository).save(libro);
        verify(prestamoRepository).save(prestamo);
    }

    @Test
    @DisplayName("Devolver un préstamo inexistente: se rechaza y no se guarda nada")
    void devolverPrestamoInexistenteNoGuarda() {
        // Arrange
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(PrestamoNoEncontradoException.class,
                () -> prestamoService.devolverLibro(99L));
        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devolver un préstamo ya DEVUELTO: se rechaza y no se guarda nada")
    void devolverPrestamoYaDevueltoNoGuarda() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Libro", "Autor", 1);
        Prestamo prestamo = new Prestamo(estudianteActivo(), libro, LocalDate.now(), "DEVUELTO");
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.devolverLibro(5L));
        verify(libroRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }
}
