package pe.edu.idat.essaludmiconsulta.dto;

import pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita;

import java.time.LocalDateTime;
import java.util.List;

// Respuesta de todas las operaciones de citas (agendar, confirmar, cancelar,
// reprogramar, listar). Reemplaza a la entidad Cita en las respuestas HTTP:
// devolver la entidad directamente rompía con "no session" al serializar
// cita.horario.medico (LAZY, nunca inicializado dentro de la transacción,
// y con open-in-view=false la sesión ya está cerrada cuando Jackson
// serializa). El mapeo a este DTO ocurre DENTRO del método @Transactional
// del servicio, mientras la sesión sigue abierta.
public record CitaResponse(
        Long idCita,
        String dniPaciente,
        String nombrePaciente,
        EstadoCita estado,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaConfirmacion,
        String motivoCancelacion,
        HorarioResponse horario,
        // HU-11: solo no-vacía cuando estado == PENDIENTE_REPROGRAMACION.
        // El paciente elige UNA de estas opciones al aceptar.
        List<HorarioResponse> opcionesReprogramacion,
        String motivoReprogramacion
) {
}

