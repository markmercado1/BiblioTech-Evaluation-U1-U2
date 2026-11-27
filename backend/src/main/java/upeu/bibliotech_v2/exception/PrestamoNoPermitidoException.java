package upeu.bibliotech_v2.exception;

/** Se lanza cuando una regla de RN-03 o RN-04 rechaza la operación. Responde 400. */
public class PrestamoNoPermitidoException extends RuntimeException {

    public PrestamoNoPermitidoException(String mensaje) {
        super(mensaje);
    }
}
