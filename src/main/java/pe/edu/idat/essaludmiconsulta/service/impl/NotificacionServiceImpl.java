package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pe.edu.idat.essaludmiconsulta.model.Cita;
import pe.edu.idat.essaludmiconsulta.model.HorarioMedico;
import pe.edu.idat.essaludmiconsulta.model.ListaEspera;
import pe.edu.idat.essaludmiconsulta.service.NotificacionService;

// RF-05 / RN-08: confirma el registro de la cita y avisa de cupos
// liberados, ambos por correo electrónico registrado por el asegurado.
//
// Diseño: el envío NUNCA lanza una excepción hacia el llamador. Si el
// paciente no tiene correo registrado, o el servidor SMTP falla, se
// registra en el log y el flujo de negocio (agendar/cancelar/reprogramar)
// continúa con normalidad — una notificación fallida no debe revertir
// una transacción de citas ya confirmada en base de datos.
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final JavaMailSender mailSender;

    @Override
    public void notificarCupoLiberado(ListaEspera registroEnEspera, HorarioMedico horarioLiberado) {
        String correo = registroEnEspera.getPaciente().getCorreo();
        if (correo == null || correo.isBlank()) {
            log.warn("No se pudo notificar cupo liberado: paciente {} no tiene correo registrado.",
                    registroEnEspera.getPaciente().getDni());
            return;
        }

        String asunto = "EsSalud Mi Consulta: se liberó un cupo para "
                + registroEnEspera.getEspecialidad().getNombre();
        String cuerpo = "Hola " + registroEnEspera.getPaciente().getNombres() + ",\n\n"
                + "Se liberó un cupo para " + registroEnEspera.getEspecialidad().getNombre()
                + " que podría interesarte:\n"
                + "Fecha y hora: " + horarioLiberado.getFecha() + " " + horarioLiberado.getHoraInicio() + "\n\n"
                + "Este cupo NO está reservado para ti — ingresa a la plataforma y agéndalo tú mismo "
                + "cuanto antes, porque otro asegurado en espera podría tomarlo primero.";

        enviar(correo, asunto, cuerpo);
    }

    @Override
    public void notificarCitaRegistrada(Cita cita) {
        String correo = cita.getPaciente().getCorreo();
        if (correo == null || correo.isBlank()) {
            log.warn("No se pudo notificar el registro de la cita {}: paciente {} no tiene correo registrado.",
                    cita.getIdCita(), cita.getPaciente().getDni());
            return;
        }

        String asunto = "EsSalud Mi Consulta: confirmamos el registro de tu cita médica";
        String cuerpo = "Hola " + cita.getPaciente().getNombres() + ",\n\n"
                + "Tu cita médica fue registrada correctamente:\n"
                + "Especialidad: " + cita.getHorario().getMedico().getEspecialidad().getNombre() + "\n"
                + "Médico: " + cita.getHorario().getMedico().getNombres() + " "
                + cita.getHorario().getMedico().getApellidoPaterno() + "\n"
                + "Fecha y hora: " + cita.getHorario().getFecha() + " " + cita.getHorario().getHoraInicio() + "\n\n"
                + "Recuerda confirmarla antes de la fecha de atención desde la plataforma Mi Consulta.";

        enviar(correo, asunto, cuerpo);
    }

    @Override
    public void notificarCitaConfirmadaOCancelada(Cita cita) {
        String correo = cita.getPaciente().getCorreo();
        if (correo == null || correo.isBlank()) {
            log.warn("No se pudo notificar cambio de estado de cita {}: paciente {} no tiene correo registrado.",
                    cita.getIdCita(), cita.getPaciente().getDni());
            return;
        }

        String asunto = "EsSalud Mi Consulta: actualización de tu cita médica";
        String cuerpo = "Hola " + cita.getPaciente().getNombres() + ",\n\n"
                + "El estado de tu cita ha cambiado a: " + cita.getEstado() + "\n"
                + "Fecha y hora: " + cita.getHorario().getFecha() + " " + cita.getHorario().getHoraInicio() + "\n"
                + (cita.getMotivoCancelacion() != null ? "Motivo: " + cita.getMotivoCancelacion() + "\n" : "")
                + "\nSi tienes dudas, ingresa a la plataforma Mi Consulta.";

        enviar(correo, asunto, cuerpo);
    }

    @Override
    public void notificarCitaConfirmadaPorAdmin(Cita cita) {
        String correo = cita.getPaciente().getCorreo();
        if (correo == null || correo.isBlank()) {
            log.warn("No se pudo notificar la confirmación telefónica de la cita {}: paciente {} no tiene correo registrado.",
                    cita.getIdCita(), cita.getPaciente().getDni());
            return;
        }

        String asunto = "EsSalud Mi Consulta: tu cita médica quedó confirmada";
        String cuerpo = "Hola " + cita.getPaciente().getNombres() + ",\n\n"
                + "Nos comunicamos contigo por teléfono para confirmar tu cita médica y ha quedado CONFIRMADA:\n"
                + "Especialidad: " + cita.getHorario().getMedico().getEspecialidad().getNombre() + "\n"
                + "Médico: " + cita.getHorario().getMedico().getNombres() + " "
                + cita.getHorario().getMedico().getApellidoPaterno() + "\n"
                + "Fecha y hora: " + cita.getHorario().getFecha() + " " + cita.getHorario().getHoraInicio() + "\n\n"
                + "Puedes verla como \"Confirmada\" en EsSalud Mi Consulta, en el apartado \"Mis Citas\". "
                + "Si ya no puedes asistir, ingresa ahí para cancelarla.";

        enviar(correo, asunto, cuerpo);
    }

    @Override
    public void notificarPropuestaReprogramacion(Cita cita) {
        String correo = cita.getPaciente().getCorreo();
        if (correo == null || correo.isBlank()) {
            log.warn("No se pudo notificar la propuesta de reprogramación de la cita {}: paciente {} no tiene correo registrado.",
                    cita.getIdCita(), cita.getPaciente().getDni());
            return;
        }

        StringBuilder opciones = new StringBuilder();
        int i = 1;
        for (var opcion : cita.getOpcionesReprogramacion()) {
            HorarioMedico h = opcion.getHorario();
            opciones.append("  ").append(i++).append(") ")
                    .append(h.getFecha()).append(" ").append(h.getHoraInicio())
                    .append(" — ").append(h.getMedico().getNombres()).append(" ")
                    .append(h.getMedico().getApellidoPaterno()).append("\n");
        }

        String asunto = "EsSalud Mi Consulta: propuesta de reprogramación de tu cita médica";
        String cuerpo = "Hola " + cita.getPaciente().getNombres() + ",\n\n"
                + "Tu cita médica de " + cita.getHorario().getMedico().getEspecialidad().getNombre()
                + " del " + cita.getHorario().getFecha() + " " + cita.getHorario().getHoraInicio()
                + " tiene que reprogramarse.\n"
                + "Motivo: " + (cita.getMotivoReprogramacion() != null ? cita.getMotivoReprogramacion() : "No especificado") + "\n\n"
                + "Opciones disponibles para tu nueva cita:\n" + opciones
                + "\nIngresa a Essalud Mi Consulta y dirígete a la sección \"Mis Citas\" (pestaña Activas): "
                + "ahí vas a ver esta cita marcada como \"Reprogramación pendiente\", con las opciones de arriba "
                + "listas para elegir. Selecciona la que más te convenga y presiona \"Confirmar esta opción\" "
                + "para aceptarla, o \"Rechazar todas\" si ninguna te sirve.\n"
                + "Si no respondes, tu cita quedará pendiente de confirmación hasta que lo hagas.";

        enviar(correo, asunto, cuerpo);
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        } catch (MailException ex) {
            // No se relanza: un problema con el servidor SMTP no debe
            // afectar el resultado de la operación de negocio ya persistida.
            log.error("No se pudo enviar el correo de notificación a {}: {}", destinatario, ex.getMessage());
        }
    }
}
