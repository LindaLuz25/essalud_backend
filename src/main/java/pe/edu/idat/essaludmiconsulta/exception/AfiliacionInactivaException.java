package pe.edu.idat.essaludmiconsulta.exception;

// RN-01: el acceso al proceso de agendamiento está permitido únicamente
// a asegurados con afiliación vigente en EsSalud.
public class AfiliacionInactivaException extends RuntimeException {
    public AfiliacionInactivaException(String mensaje) {
        super(mensaje);
    }
}
