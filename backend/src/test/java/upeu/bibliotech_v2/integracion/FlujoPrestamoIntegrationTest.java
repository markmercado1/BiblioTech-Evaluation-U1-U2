package upeu.bibliotech_v2.integracion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.exception.PrestamoNoPermitidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;
import upeu.bibliotech_v2.service.EstudianteService;
import upeu.bibliotech_v2.service.LibroService;
import upeu.bibliotech_v2.service.PrestamoService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flujo completo con PostgreSQL de prueba: registrar préstamo baja un ejemplar,
 * sin ejemplares se rechaza y devolver lo restituye. Cada prueba crea sus propios datos
 * y se revierte al terminar (@Transactional).
 */
@SpringBootTest
@Transactional
class FlujoPrestamoIntegrationTest {

    @Autowired
    private PrestamoService prestamoService;
    @Autowired
    private LibroService libroService;
    @Autowired
    private EstudianteService estudianteService;
    @Autowired
    private LibroRepository libroRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private PrestamoRepository prestamoRepository;

    private Estudiante estudiante;

    @BeforeEach
    void crearEstudiante() {
        estudiante = estudianteService.registrar(
                new Estudiante(UUID.randomUUID().toString(), "Estudiante de prueba", null));
    }

    private Libro crearLibro(int ejemplares) {
        return libroService.registrar(
                new Libro(UUID.randomUUID().toString(), "Libro de prueba", "Autor de prueba", ejemplares));
    }

    @Test
    @DisplayName("Registrar un préstamo baja un ejemplar y deja el préstamo ACTIVO")
    void registrarPrestamoBajaUnEjemplar() {
        // Arrange
        Libro libro = crearLibro(2);

        // Act
        Prestamo prestamo = prestamoService.registrarPrestamo(estudiante.getId(), libro.getId());

        // Assert
        assertEquals("ACTIVO", prestamo.getEstado());
        assertEquals(1, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
        assertEquals(1, prestamoRepository.countByEstudianteIdAndEstado(estudiante.getId(), "ACTIVO"));
    }

    @Test
    @DisplayName("Sin ejemplares el préstamo se rechaza y no se crea")
    void sinEjemplaresSeRechazaElPrestamo() {
        // Arrange
        Libro libro = crearLibro(0);

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.registrarPrestamo(estudiante.getId(), libro.getId()));
        assertEquals(0, prestamoRepository.countByEstudianteIdAndEstado(estudiante.getId(), "ACTIVO"));
    }

    @Test
    @DisplayName("Devolver el libro restituye el ejemplar y el préstamo pasa a DEVUELTO")
    void devolverRestituyeElEjemplar() {
        // Arrange
        Libro libro = crearLibro(1);
        Prestamo prestamo = prestamoService.registrarPrestamo(estudiante.getId(), libro.getId());

        // Act
        Prestamo devuelto = prestamoService.devolverLibro(prestamo.getId());

        // Assert
        assertEquals("DEVUELTO", devuelto.getEstado());
        assertEquals(1, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
        assertEquals(0, prestamoRepository.countByEstudianteIdAndEstado(estudiante.getId(), "ACTIVO"));
    }

    @Test
    @DisplayName("El cuarto préstamo activo del mismo estudiante se rechaza")
    void elCuartoPrestamoActivoSeRechaza() {
        // Arrange
        Libro libro = crearLibro(10);
        for (int i = 0; i < 3; i++) {
            prestamoService.registrarPrestamo(estudiante.getId(), libro.getId());
        }

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.registrarPrestamo(estudiante.getId(), libro.getId()));
        assertEquals(7, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Un estudiante INACTIVO no puede pedir préstamos")
    void estudianteInactivoNoPuedePrestar() {
        // Arrange
        Libro libro = crearLibro(2);
        estudiante.setEstado(Estudiante.INACTIVO);
        estudianteRepository.save(estudiante);

        // Act + Assert
        assertThrows(PrestamoNoPermitidoException.class,
                () -> prestamoService.registrarPrestamo(estudiante.getId(), libro.getId()));
    }

    @Test
    @DisplayName("Registrar un libro con ISBN repetido se rechaza")
    void isbnRepetidoSeRechaza() {
        // Arrange
        Libro existente = crearLibro(1);

        // Act + Assert
        assertThrows(DatoInvalidoException.class,
                () -> libroService.registrar(new Libro(existente.getIsbn(), "Otro", "Otro autor", 1)));
    }

    @Test
    @DisplayName("Registrar un estudiante con DNI repetido se rechaza")
    void dniRepetidoSeRechaza() {
        // Arrange + Act + Assert
        assertThrows(DatoInvalidoException.class,
                () -> estudianteService.registrar(new Estudiante(estudiante.getDni(), "Otro", null)));
    }
}
