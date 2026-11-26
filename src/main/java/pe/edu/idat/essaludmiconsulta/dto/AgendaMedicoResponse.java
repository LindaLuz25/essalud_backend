package pe.edu.idat.essaludmiconsulta.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

// HU-13 (provisional — ajustar al número oficial de la tesis si difiere):
// "Como asegurado, quiero consultar la agenda institucional vigente de un
// médico específico para visualizar los días y horarios de atención
// programados durante el mes consultado, con la finalidad de planificar
// oportunamente una futura atención médica, sin que esta consulta
// represente la reserva o asignación de un cupo para una cita."
//
// DTO plano (no la entidad JPA), mismo criterio que HorarioResponse: evita
// exponer relaciones LAZY fuera de la transacción. No incluye idHorario ni
// datos del paciente que ocupa el cupo: es solo informativo, no permite
// agendar desde aquí (para eso existe /api/v1/citas + HU-03).
public record AgendaMedicoResponse(
        LocalDate fecha,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        // true si ese bloque todavía tiene cupo libre (EstadoHorario.DISPONIBLE);
        // false si ya está reservado. Solo para que el asegurado planifique
        // cuándo intentar agendar, no habilita ninguna acción de reserva.
        Boolean disponible
) {
}
