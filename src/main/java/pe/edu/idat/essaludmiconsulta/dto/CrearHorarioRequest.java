package pe.edu.idat.essaludmiconsulta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

// EFI: el área administrativa crea un nuevo cupo (horario) para un médico,
// por ejemplo para tener opciones que ofrecer al proponer una
// reprogramación cuando no hay suficientes cupos DISPONIBLES ya cargados.
public record CrearHorarioRequest(
        @NotBlank
        String cmpMedico,

        @NotNull
        LocalDate fecha,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        LocalTime horaFin
) {
}
