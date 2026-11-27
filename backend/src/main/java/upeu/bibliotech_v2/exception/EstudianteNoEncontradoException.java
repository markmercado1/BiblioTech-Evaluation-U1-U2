package upeu.bibliotech_v2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Se lanza cuando el estudiante consultado no existe. Responde 404 en la API. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EstudianteNoEncontradoException extends RuntimeException {

    public EstudianteNoEncontradoException() {
        super("Estudiante no existe");
    }
}
