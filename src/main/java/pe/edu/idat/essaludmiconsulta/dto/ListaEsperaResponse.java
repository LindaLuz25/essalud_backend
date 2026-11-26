package pe.edu.idat.essaludmiconsulta.dto;

import pe.edu.idat.essaludmiconsulta.model.enums.EstadoListaEspera;

import java.time.LocalDateTime;

// Igual razón que CitaResponse: evita exponer ListaEspera con sus
// relaciones LAZY (paciente, especialidad) fuera de la transacción.
public record ListaEsperaResponse(
        Long idListaEspera,
        Integer idEspecialidad,
        String nombreEspecialidad,
        EstadoListaEspera estado,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaNotificacion
) {
}
