package pe.edu.idat.essaludmiconsulta.exception;

/**
 * Se lanza cuando dos peticiones concurrentes intentan tomar el mismo
 * horario ya reservado/cancelado, o cuando el horario solicitado no existe.
 */
public class CupoNoDisponibleException extends RuntimeException {
    public CupoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
