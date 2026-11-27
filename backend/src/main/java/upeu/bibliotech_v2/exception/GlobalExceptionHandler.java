package upeu.bibliotech_v2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Convierte las excepciones de negocio en respuestas HTTP con un cuerpo con el campo "mensaje". */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DatoInvalidoException.class, PrestamoNoPermitidoException.class})
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(RuntimeException e) {
        return responder(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler({LibroNoEncontradoException.class,
            EstudianteNoEncontradoException.class,
            PrestamoNoEncontradoException.class})
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RuntimeException e) {
        return responder(HttpStatus.NOT_FOUND, e);
    }

    private ResponseEntity<Map<String, String>> responder(HttpStatus estado, RuntimeException e) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", e.getMessage()));
    }
}
