package pe.edu.idat.essaludmiconsulta.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

// Bloque recurrente de atención derivado de los horarios registrados
// (mismo día de la semana + mismo rango horario), usado en HU-02.
public record BloqueAtencionResponse(
        DayOfWeek dia,
        LocalTime horaInicio,
        LocalTime horaFin
) {
}
