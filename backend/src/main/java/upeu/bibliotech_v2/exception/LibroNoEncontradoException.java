package upeu.bibliotech_v2.exception;

/** Se lanza cuando el libro solicitado no existe. Responde 404. */
public class LibroNoEncontradoException extends RuntimeException {

    public LibroNoEncontradoException() {
        super("Libro no existe");
    }
}
