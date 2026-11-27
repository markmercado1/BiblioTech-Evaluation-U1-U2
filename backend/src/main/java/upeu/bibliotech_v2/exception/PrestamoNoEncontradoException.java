package upeu.bibliotech_v2.exception;

/** Se lanza cuando el préstamo solicitado no existe. Responde 404. */
public class PrestamoNoEncontradoException extends RuntimeException {

    public PrestamoNoEncontradoException() {
        super("Préstamo no existe");
    }
}
