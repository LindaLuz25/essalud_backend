package pe.edu.idat.essaludmiconsulta.exception;

// RN nueva (extensión RF-05/HU-05, pendiente de sustentar en el
// documento): las citas se agendan con 2-4 meses de anticipación (según
// el diagnóstico del Capítulo II), así que confirmar inmediatamente al
// agendar no tiene sentido operativo. La confirmación solo se habilita
// dentro de los N días previos a la fecha de atención
// (citas.dias-anticipacion-confirmacion en application.properties).
public class ConfirmacionFueraDePlazoException extends RuntimeException {
    public ConfirmacionFueraDePlazoException(String mensaje) {
        super(mensaje);
    }
}
