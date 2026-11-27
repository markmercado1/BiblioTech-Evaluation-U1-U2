package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.validation.EstudianteValidator;

import static org.junit.jupiter.api.Assertions.*;

/** RN-02: pruebas unitarias (JUnit 5, patrón AAA) de las validaciones al registrar un estudiante. */
class EstudianteValidatorTest {

    @Test
    @DisplayName("Estudiante con DNI y nombre se acepta")
    void estudianteValidoSeAcepta() {
        // Arrange
        Estudiante estudiante = new Estudiante("70000001", "Juan Perez", null);

        // Act + Assert
        assertDoesNotThrow(() -> EstudianteValidator.validar(estudiante));
    }

    @Test
    @DisplayName("Estudiante sin DNI se rechaza")
    void estudianteSinDniSeRechaza() {
        // Arrange
        Estudiante estudiante = new Estudiante("", "Juan Perez", null);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> EstudianteValidator.validar(estudiante));

        // Assert
        assertEquals("El DNI es obligatorio", e.getMessage());
    }

    @Test
    @DisplayName("Estudiante sin nombre se rechaza")
    void estudianteSinNombreSeRechaza() {
        // Arrange
        Estudiante estudiante = new Estudiante("70000001", null, null);

        // Act
        DatoInvalidoException e = assertThrows(DatoInvalidoException.class,
                () -> EstudianteValidator.validar(estudiante));

        // Assert
        assertEquals("El nombre es obligatorio", e.getMessage());
    }

    @Test
    @DisplayName("Estudiante nulo se rechaza")
    void estudianteNuloSeRechaza() {
        // Arrange + Act + Assert
        assertThrows(DatoInvalidoException.class, () -> EstudianteValidator.validar(null));
    }
}
