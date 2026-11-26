package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.model.Cita;
import pe.edu.idat.essaludmiconsulta.model.HorarioMedico;
import pe.edu.idat.essaludmiconsulta.model.ListaEspera;

public interface NotificacionService {

    /**
     * RN-08: avisa a un asegurado en lista de espera que se liberó un cupo
     * para su especialidad. NO le reserva nada automáticamente — el
     * asegurado debe entrar a la plataforma y agendarlo él mismo, por
     * orden de llegada real (protegido por el bloqueo pesimista de
     * CitaService.agendarCita). Se envía a TODOS los que están esperando
     * esa especialidad cada vez que se libera un cupo, no solo al primero.
     */
    void notificarCupoLiberado(ListaEspera registroEnEspera, HorarioMedico horarioLiberado);

    /**
     * RF-05 / HU-05: confirma por correo el registro de la cita recién
     * agendada (detalle del agendamiento), para que el asegurado verifique
     * que quedó registrada correctamente. Se dispara en
     * CitaService.agendarCita(), separado de
     * notificarCitaConfirmadaOCancelada() porque ese otro método se usa
     * para cambios de estado posteriores (confirmar/cancelar/reprogramar),
     * no para el registro inicial.
     */
    void notificarCitaRegistrada(Cita cita);

    void notificarCitaConfirmadaOCancelada(Cita cita);

    /**
     * El área administrativa (EFI) confirmó la cita después de llamar por
     * teléfono al asegurado. Distinto de notificarCitaConfirmadaOCancelada()
     * porque el texto explica que la confirmación vino de esa llamada, no
     * de una acción del propio paciente en la plataforma (ya no puede
     * confirmar por su cuenta).
     */
    void notificarCitaConfirmadaPorAdmin(Cita cita);

    /**
     * HU-11: avisa al paciente que el área administrativa (EFI) propuso
     * reprogramar su cita, con el motivo y el nuevo horario propuesto.
     * El correo lo invita a ingresar a Mi Consulta a aceptar o rechazar
     * — no se le reprograma nada automáticamente.
     */
    void notificarPropuestaReprogramacion(Cita cita);
}

