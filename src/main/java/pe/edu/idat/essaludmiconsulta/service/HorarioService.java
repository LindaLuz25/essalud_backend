package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.AgendaMedicoResponse;
import pe.edu.idat.essaludmiconsulta.dto.CrearHorarioRequest;
import pe.edu.idat.essaludmiconsulta.dto.HorarioResponse;

import java.time.LocalDate;
import java.util.List;

public interface HorarioService {

    /**
     * Búsqueda de cupos disponibles.
     * - Si se envía cmpMedico + fecha: cupos de ESE médico en ESA fecha exacta (HU-03).
     * - Si se envía idEspecialidad (sin cmpMedico): todos los cupos disponibles
     *   de la especialidad desde la fecha indicada (o desde hoy si no se envía).
     * Debe enviarse al menos uno de los dos criterios.
     */
    List<HorarioResponse> buscarDisponibles(Integer idEspecialidad, String cmpMedico,
                                            LocalDate fecha, LocalDate desde);

    /**
     * EFI: el área administrativa crea un nuevo cupo DISPONIBLE para un
     * médico (ej. para tener con qué ofrecer opciones al reprogramar una
     * cita cuando no hay suficientes horarios ya cargados).
     */
    HorarioResponse crearHorario(CrearHorarioRequest request);

    /**
     * HU-13: agenda institucional vigente de un médico para el mes
     * indicado (año + mes). Solo consulta — no reserva ni asigna ningún
     * cupo. Lanza IllegalArgumentException si el médico no existe.
     */
    List<AgendaMedicoResponse> consultarAgendaMensual(String cmpMedico, int anio, int mes);
}
