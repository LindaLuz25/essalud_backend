package pe.edu.idat.essaludmiconsulta.model.enums;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    ASISTIO,
    NO_ASISTIO,
    // HU-11: el área administrativa (EFI) propuso un nuevo horario para
    // esta cita (ej. el médico no atenderá ese día) y está a la espera de
    // que el paciente acepte o rechace desde Mi Consulta. El horario
    // propuesto y el motivo viven en Cita.horarioPropuesto/motivoReprogramacion.
    PENDIENTE_REPROGRAMACION
}
