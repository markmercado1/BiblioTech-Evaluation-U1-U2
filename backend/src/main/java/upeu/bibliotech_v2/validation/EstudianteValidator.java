package upeu.bibliotech_v2.validation;

import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;

/** RN-02: DNI y nombre obligatorios. */
public final class EstudianteValidator {

    private EstudianteValidator() {
    }

    public static void validar(Estudiante estudiante) {
        if (estudiante == null) {
            throw new DatoInvalidoException("Los datos del estudiante son obligatorios");
        }
        if (estudiante.getDni() == null || estudiante.getDni().isBlank()) {
            throw new DatoInvalidoException("El DNI es obligatorio");
        }
        if (estudiante.getNombre() == null || estudiante.getNombre().isBlank()) {
            throw new DatoInvalidoException("El nombre es obligatorio");
        }
    }
}
