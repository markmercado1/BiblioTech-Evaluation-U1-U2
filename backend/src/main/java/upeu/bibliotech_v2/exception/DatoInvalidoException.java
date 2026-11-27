package upeu.bibliotech_v2.exception;

/** Se lanza cuando los datos de un registro incumplen una regla de negocio. Responde 400. */
public class DatoInvalidoException extends RuntimeException {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
