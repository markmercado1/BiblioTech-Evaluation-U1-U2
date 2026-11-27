package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.validation.LibroValidator;

import static org.junit.jupiter.api.Assertions.*;

/** RN-01: pruebas unitarias (JUnit 5, patrón AAA) de las validaciones al registrar un libro. */
class LibroValidatorTest {

    @Test
    @DisplayName("CP01: libro con todos los datos válidos se acepta")
    void cp01_libroValidoSeAcepta() {
        // Arrange
        Libro libro = new Libro("978-0-00-000001-1", "Clean Code", "Robert Martin", 3);

        // Act + Assert
        assertDoesNotThrow(() -> LibroValidator.validar(libro));
    }

    @Test
    @DisplayName("CP02: libro sin ISBN se rechaza")
    void cp02_libroSinIsbnSeRechaza() {
        // Arrange
        Libro libro = new Libro("", "Clean Code", "Robert Martin", 3);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> LibroValidator.validar(libro));

        // Assert
        assertEquals("El ISBN es obligatorio", e.getMessage());
    }

    @Test
    @DisplayName("CP03: libro sin título se rechaza")
    void cp03_libroSinTituloSeRechaza() {
        // Arrange
        Libro libro = new Libro("978-0-00-000001-1", "   ", "Robert Martin", 3);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> LibroValidator.validar(libro));

        // Assert
        assertEquals("El título es obligatorio", e.getMessage());
    }

    @Test
    @DisplayName("CP04: libro sin autor se rechaza")
    void cp04_libroSinAutorSeRechaza() {
        // Arrange
        Libro libro = new Libro("978-0-00-000001-1", "Clean Code", null, 3);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> LibroValidator.validar(libro));

        // Assert
        assertEquals("El autor es obligatorio", e.getMessage());
    }

    @Test
    @DisplayName("CP05: libro con ejemplares negativos se rechaza")
    void cp05_ejemplaresNegativosSeRechazan() {
        // Arrange
        Libro libro = new Libro("978-0-00-000001-1", "Clean Code", "Robert Martin", -1);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> LibroValidator.validar(libro));

        // Assert
        assertEquals("Los ejemplares no pueden ser negativos", e.getMessage());
    }

    @Test
    @DisplayName("CP06 (borde): libro con 0 ejemplares se acepta y queda NO DISPONIBLE")
    void cp06_ceroEjemplaresEsBordeValido() {
        // Arrange
        Libro libro = new Libro("978-0-00-000001-1", "Clean Code", "Robert Martin", 0);

        // Act
        assertDoesNotThrow(() -> LibroValidator.validar(libro));

        // Assert
        assertEquals(Libro.NO_DISPONIBLE, libro.getEstado());
    }

    @Test
    @DisplayName("Estado calculado: con ejemplares mayores que 0 el libro está DISPONIBLE")
    void estadoEsDisponibleConEjemplares() {
        // Arrange
        Libro libro = new Libro("978-0-00-000001-1", "Clean Code", "Robert Martin", 1);

        // Act
        String estado = libro.getEstado();

        // Assert
        assertEquals(Libro.DISPONIBLE, estado);
    }

    @Test
    @DisplayName("Libro nulo se rechaza")
    void libroNuloSeRechaza() {
        // Arrange + Act + Assert
        assertThrows(DatoInvalidoException.class, () -> LibroValidator.validar(null));
    }
}
