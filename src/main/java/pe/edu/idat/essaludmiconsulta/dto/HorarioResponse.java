package pe.edu.idat.essaludmiconsulta.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// HU-03: cupo disponible para un médico y fecha seleccionados.
// DTO plano (no la entidad JPA) para evitar problemas de serialización
// con relaciones LAZY fuera de la transacción (ver auditoría del backend).
public record HorarioResponse(
        Long idHorario,
        String cmpMedico,
        String nombreMedico,
        Integer idEspecialidad,
        String nombreEspecialidad,
        String centroAsistencial,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        // RF-11: para que el frontend pueda mostrar "actualizado hace X".
        LocalDateTime fechaActualizacion,
        // HU-12: true si el cupo volvió a estar disponible por una
        // cancelación/reprogramación reciente (útil para destacarlo en la UI).
        Boolean liberadoPorCancelacion
) {
}
