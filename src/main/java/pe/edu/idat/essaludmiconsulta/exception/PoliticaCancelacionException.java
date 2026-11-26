package pe.edu.idat.essaludmiconsulta.exception;

// RN-04: la cancelación o reprogramación de una cita debe respetar el
// plazo mínimo de anticipación establecido institucionalmente.
public class PoliticaCancelacionException extends RuntimeException {
    public PoliticaCancelacionException(String mensaje) {
        super(mensaje);
    }
}
