package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.ListaEsperaResponse;
import pe.edu.idat.essaludmiconsulta.model.ListaEspera;

import java.util.List;

public interface ListaEsperaService {

    /** Inscribe al paciente en la cola de espera digital de una especialidad. */
    ListaEsperaResponse anotarse(String dniPaciente, Integer idEspecialidad);

    /**
     * Retira al paciente de la cola de espera (desistimiento voluntario).
     * Valida que el registro pertenezca al paciente solicitante antes de borrar.
     */
    void retirarse(Long idListaEspera, String dniSolicitante);

    /**
     * RN-08: todos los asegurados en espera ACTIVA de una especialidad
     * (para notificarles que se liberó un cupo). Contrato interno entre
     * servicios — nunca se serializa a HTTP directamente, por eso
     * devuelve la entidad.
     */
    List<ListaEspera> listarActivosPorEspecialidad(Integer idEspecialidad);

    /**
     * Si el paciente tenía una inscripción ACTIVA en la especialidad del
     * horario que acaba de agendar, la marca como ATENDIDO — ya no
     * necesita seguir recibiendo notificaciones de esa especialidad.
     * No lanza error si no tenía ninguna inscripción (caso normal).
     */
    void cerrarEsperaSiExiste(String dniPaciente, Integer idEspecialidad);

    List<ListaEsperaResponse> listarPorPaciente(String dni);
}

