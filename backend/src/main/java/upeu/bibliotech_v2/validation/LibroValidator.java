package upeu.bibliotech_v2.validation;

import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Libro;

/** RN-01: ISBN, título y autor obligatorios; ejemplares mayores o iguales a 0. */
public final class LibroValidator {

    private LibroValidator() {
    }

    public static void validar(Libro libro) {
        if (libro == null) {
            throw new DatoInvalidoException("Los datos del libro son obligatorios");
        }
        if (estaVacio(libro.getIsbn())) {
            throw new DatoInvalidoException("El ISBN es obligatorio");
        }
        if (estaVacio(libro.getTitulo())) {
            throw new DatoInvalidoException("El título es obligatorio");
        }
        if (estaVacio(libro.getAutor())) {
            throw new DatoInvalidoException("El autor es obligatorio");
        }
        if (libro.getEjemplaresDisponibles() < 0) {
            throw new DatoInvalidoException("Los ejemplares no pueden ser negativos");
        }
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
