package upeu.bibliotech_v2.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.exception.LibroNoEncontradoException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.service.LibroService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** LibroService con el repositorio simulado: RN-01 (ISBN único y datos obligatorios). */
@ExtendWith(MockitoExtension.class)
class LibroServiceMockitoTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    @Test
    @DisplayName("Registrar libro válido con ISBN nuevo: se guarda")
    void registrarLibroValidoLoGuarda() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Clean Code", "Robert Martin", 2);
        when(libroRepository.existsByIsbn("ISBN-1")).thenReturn(false);
        when(libroRepository.save(libro)).thenReturn(libro);

        // Act
        Libro resultado = libroService.registrar(libro);

        // Assert
        assertSame(libro, resultado);
        verify(libroRepository).save(libro);
    }

    @Test
    @DisplayName("Registrar libro con ISBN repetido: se rechaza y no se guarda")
    void registrarLibroConIsbnRepetidoNoLoGuarda() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "Clean Code", "Robert Martin", 2);
        when(libroRepository.existsByIsbn("ISBN-1")).thenReturn(true);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> libroService.registrar(libro));

        // Assert
        assertEquals("El ISBN ya está registrado", e.getMessage());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar libro sin ISBN: se rechaza sin consultar ni guardar")
    void registrarLibroSinIsbnNoConsultaNiGuarda() {
        // Arrange
        Libro libro = new Libro("", "Clean Code", "Robert Martin", 2);

        // Act + Assert
        assertThrows(DatoInvalidoException.class, () -> libroService.registrar(libro));
        verify(libroRepository, never()).existsByIsbn(any());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Listar libros devuelve los libros del repositorio")
    void listarDevuelveLosLibros() {
        // Arrange
        List<Libro> libros = List.of(new Libro("ISBN-1", "A", "B", 1));
        when(libroRepository.findAll()).thenReturn(libros);

        // Act
        List<Libro> resultado = libroService.listar();

        // Assert
        assertEquals(libros, resultado);
    }

    @Test
    @DisplayName("Buscar un libro inexistente lanza LibroNoEncontradoException")
    void buscarLibroInexistenteLanzaExcepcion() {
        // Arrange
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(LibroNoEncontradoException.class, () -> libroService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Buscar un libro existente lo devuelve")
    void buscarLibroExistenteLoDevuelve() {
        // Arrange
        Libro libro = new Libro("ISBN-1", "A", "B", 1);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));

        // Act
        Libro resultado = libroService.buscarPorId(1L);

        // Assert
        assertSame(libro, resultado);
    }
}
