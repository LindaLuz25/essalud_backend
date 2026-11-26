package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.CitaResponse;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    /**
     * Agenda una nueva cita tomando un horario DISPONIBLE.
     * Debe fallar de forma controlada (CupoNoDisponibleException) si
     * otro paciente tomó el cupo en el mismo instante (concurrencia).
     */
    CitaResponse agendarCita(String dniPaciente, Long idHorario);

    /**
     * Confirma la cita. Acción del área administrativa (EFI) después de
     * llamar telefónicamente al asegurado — el paciente ya NO confirma
     * por su cuenta desde la web, solo puede cancelar. No valida
     * titularidad (quien llama es el administrativo, no el paciente),
     * pero sigue respetando la ventana de confirmación (RN nueva:
     * citas.dias-anticipacion-confirmacion).
     */
    CitaResponse confirmarCita(Long idCita);

    /**
     * Cancelación explícita por parte del paciente o del sistema.
     * Dispara el algoritmo de liberación inmediata del cupo.
     * @param dniSolicitante DNI del paciente autenticado (del token JWT);
     *                       se valida que sea el dueño de la cita.
     */
    CitaResponse cancelarCita(Long idCita, String motivo, String dniSolicitante);

    /**
     * RN-04/RN-07/HU-10: mueve una cita PENDIENTE o CONFIRMADA a un nuevo
     * horario DISPONIBLE, respetando el plazo mínimo de anticipación sobre
     * la cita ORIGINAL. El horario anterior se libera y pasa por el mismo
     * algoritmo de reasignación (FIFO) que una cancelación. La cita queda
     * en estado PENDIENTE sobre el nuevo horario (requiere reconfirmación).
     */
    CitaResponse reprogramarCita(Long idCita, Long idNuevoHorario, String dniSolicitante);

    /**
     * HU-11: el área administrativa (EFI) propone una o varias opciones de
     * horario para una cita PENDIENTE o CONFIRMADA (ej. el médico no
     * atenderá ese día) e indica el motivo. La cita pasa a
     * PENDIENTE_REPROGRAMACION y TODOS los horarios propuestos quedan
     * RESERVADO (nadie más puede tomarlos) mientras el paciente decide
     * cuál prefiere. No cambia el horario actual todavía — eso ocurre
     * recién si el paciente acepta una de las opciones.
     */
    CitaResponse proponerReprogramacion(Long idCita, List<Long> idsHorarios, String motivo);

    /**
     * HU-11: el paciente acepta UNA de las opciones propuestas. El horario
     * original se libera (mismo algoritmo FIFO de lista de espera que una
     * cancelación), las demás opciones no elegidas también se liberan, y
     * la cita pasa a tener el horario elegido y queda CONFIRMADA (aceptar
     * ya es, en sí, la confirmación).
     * @param idHorarioElegido horario elegido por el paciente; debe ser una
     *                         de las opciones propuestas para esta cita.
     * @param dniSolicitante DNI del paciente autenticado; se valida titularidad.
     */
    CitaResponse aceptarReprogramacion(Long idCita, Long idHorarioElegido, String dniSolicitante);

    /**
     * HU-11: el paciente rechaza TODAS las opciones propuestas. Cada
     * horario propuesto se libera (algoritmo FIFO habitual) y la cita
     * queda CANCELADA — en la práctica el horario original ya no es
     * viable (por eso se propuso reprogramar), así que no tiene sentido
     * dejarla "activa" sobre un horario que el médico no va a atender.
     * @param dniSolicitante DNI del paciente autenticado; se valida titularidad.
     */
    CitaResponse rechazarReprogramacion(Long idCita, String dniSolicitante);

    /**
     * HU-11 / EFI: citas activas (PENDIENTE o CONFIRMADA) en un rango de
     * fechas, para que el administrativo decida cuáles reprogramar (ej.
     * el médico avisó que no atenderá esos días) o confirmar
     * telefónicamente. Si cmpMedico es null o vacío, trae las de TODOS
     * los médicos en ese rango (vista por defecto de EFI, ej. "citas del
     * mes"); si viene informado, acota a ese médico.
     */
    List<CitaResponse> buscarActivasPorMedicoYRango(String cmpMedico, LocalDate desde, LocalDate hasta);

    /**
     * RN-07 / RN-08: libera en milisegundos el horario asociado a una cita
     * cancelada (o no confirmada) y, de forma atómica:
     *   1. Marca el horario como DISPONIBLE nuevamente (visible para
     *      cualquier asegurado en la búsqueda de horarios).
     *   2. Notifica por correo a TODOS los asegurados en espera activa de
     *      esa especialidad — no se le asigna a nadie automáticamente,
     *      cada uno debe agendarlo por su cuenta; gana quien lo haga
     *      primero (protegido por el bloqueo pesimista de agendarCita).
     */
    void liberarYReasignarCupo(Long idHorario);

    /**
     * Job programado (@Scheduled) que recorre las citas PENDIENTES cuyo
     * horario ocurre en menos de 24 horas y no han sido confirmadas por
     * el paciente, cancelándolas automáticamente y liberando el cupo.
     */
    void procesarCitasNoConfirmadas();

    List<CitaResponse> listarCitasPorPaciente(String dni);
}
