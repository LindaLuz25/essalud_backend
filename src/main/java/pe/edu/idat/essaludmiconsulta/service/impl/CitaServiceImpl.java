package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.CitaResponse;
import pe.edu.idat.essaludmiconsulta.dto.HorarioResponse;
import pe.edu.idat.essaludmiconsulta.exception.AfiliacionInactivaException;
import pe.edu.idat.essaludmiconsulta.exception.ConfirmacionFueraDePlazoException;
import pe.edu.idat.essaludmiconsulta.exception.CupoNoDisponibleException;
import pe.edu.idat.essaludmiconsulta.exception.PoliticaCancelacionException;
import pe.edu.idat.essaludmiconsulta.model.Cita;
import pe.edu.idat.essaludmiconsulta.model.CitaOpcionReprogramacion;
import pe.edu.idat.essaludmiconsulta.model.HorarioMedico;
import pe.edu.idat.essaludmiconsulta.model.ListaEspera;
import pe.edu.idat.essaludmiconsulta.model.Paciente;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoHorario;
import pe.edu.idat.essaludmiconsulta.repository.CitaRepository;
import pe.edu.idat.essaludmiconsulta.repository.HorarioMedicoRepository;
import pe.edu.idat.essaludmiconsulta.repository.PacienteRepository;
import pe.edu.idat.essaludmiconsulta.service.CitaService;
import pe.edu.idat.essaludmiconsulta.service.ListaEsperaService;
import pe.edu.idat.essaludmiconsulta.service.NotificacionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ListaEsperaService listaEsperaService;
    private final NotificacionService notificacionService;

    // RN-04: plazo mínimo de anticipación para cancelar o reprogramar una
    // cita, configurable por institución (application.properties).
    @Value("${citas.plazo-cancelacion-horas:24}")
    private long plazoCancelacionHoras;

    // RN nueva (extensión RF-05/HU-05, pendiente de sustentar en el
    // documento): según el diagnóstico, las citas se agendan con 2-4
    // meses de anticipación, así que confirmar el mismo día en que se
    // agenda no tiene sentido operativo. La confirmación solo se habilita
    // dentro de los N días previos a la fecha de atención.
    @Value("${citas.dias-anticipacion-confirmacion:7}")
    private long diasAnticipacionConfirmacion;

    // -----------------------------------------------------------------
    // AGENDAMIENTO
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public CitaResponse agendarCita(String dniPaciente, Long idHorario) {

        // Bloqueo pesimista: nadie más puede tomar este horario mientras
        // dura la transacción (evita doble reserva por alta concurrencia).
        // Este mismo bloqueo es lo que hace justo (no hay condición de
        // carrera) que "gane" el primero en pedirlo cuando varios
        // asegurados en espera intentan agendar el mismo cupo recién
        // liberado (RN-08).
        HorarioMedico horario = horarioMedicoRepository.buscarPorIdConBloqueo(idHorario)
                .orElseThrow(() -> new CupoNoDisponibleException("El horario no existe."));

        if (horario.getEstado() != EstadoHorario.DISPONIBLE) {
            throw new CupoNoDisponibleException("El cupo ya no está disponible.");
        }

        Paciente paciente = pacienteRepository.findById(dniPaciente)
                .orElseThrow(() -> new CupoNoDisponibleException("Paciente no encontrado."));

        // RN-01: solo asegurados con afiliación vigente pueden agendar.
        if (!Boolean.TRUE.equals(paciente.getAfiliacionActiva())) {
            throw new AfiliacionInactivaException(
                    "El asegurado no cuenta con afiliación vigente en EsSalud.");
        }

        horario.setEstado(EstadoHorario.RESERVADO);
        horario.setLiberadoPorCancelacion(false);
        horarioMedicoRepository.save(horario);

        Cita cita = Cita.builder()
                .paciente(paciente)
                .horario(horario)
                .estado(EstadoCita.PENDIENTE)
                .fechaRegistro(LocalDateTime.now())
                .build();
        Cita guardada = citaRepository.save(cita);

        // RF-05 / HU-05: confirmar el registro del agendamiento por correo.
        notificacionService.notificarCitaRegistrada(guardada);

        // Si el paciente estaba en la lista de espera de esta especialidad,
        // ya consiguió su cupo: no tiene sentido seguir notificándolo.
        listaEsperaService.cerrarEsperaSiExiste(
                dniPaciente, horario.getMedico().getEspecialidad().getIdEspecialidad());

        return toResponse(guardada);
    }

    // El área administrativa (EFI) confirma la cita luego de llamar por
    // teléfono al asegurado. Ya no hay validarTitularidad: quien ejecuta
    // esta acción es el administrativo autenticado con rol ADMIN, no el
    // paciente dueño de la cita (eso se valida en @PreAuthorize del
    // controller). Se mantiene la ventana de confirmación (RN nueva) como
    // resguardo para no confirmar citas todavía muy lejanas en el tiempo.
    @Override
    @Transactional
    public CitaResponse confirmarCita(Long idCita) {
        Cita cita = obtenerCitaOLanzar(idCita);
        validarVentanaConfirmacion(cita.getHorario());

        cita.setEstado(EstadoCita.CONFIRMADA);
        cita.setFechaConfirmacion(LocalDateTime.now());
        Cita guardada = citaRepository.save(cita);
        notificacionService.notificarCitaConfirmadaPorAdmin(guardada);
        return toResponse(guardada);
    }

    // -----------------------------------------------------------------
    // CANCELACIÓN -> DISPARA EL AVISO DE DISPONIBILIDAD
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public CitaResponse cancelarCita(Long idCita, String motivo, String dniSolicitante) {
        Cita cita = obtenerCitaOLanzar(idCita);

        // dniSolicitante == null significa que la cancelación viene del
        // propio sistema (job de no-confirmación 24h), no de un paciente
        // vía HTTP, así que no aplica la validación de titularidad NI la
        // de plazo mínimo (RN-04 protege al paciente de sí mismo, no
        // aplica cuando el propio sistema decide cancelar).
        if (dniSolicitante != null) {
            validarTitularidad(cita, dniSolicitante);
            validarPlazoMinimo(cita.getHorario());
        }

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(motivo);
        Cita guardada = citaRepository.save(cita);

        notificacionService.notificarCitaConfirmadaOCancelada(guardada);

        // El horario queda libre en la misma transacción (milisegundos),
        // no en un batch nocturno.
        liberarYAvisarDisponibilidad(guardada.getHorario().getIdHorario());

        return toResponse(guardada);
    }

    // -----------------------------------------------------------------
    // REPROGRAMACIÓN -> LIBERA EL CUPO ANTERIOR (mismo aviso RN-08)
    // Y RESERVA EL NUEVO, TODO EN LA MISMA TRANSACCIÓN.
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public CitaResponse reprogramarCita(Long idCita, Long idNuevoHorario, String dniSolicitante) {
        Cita cita = obtenerCitaOLanzar(idCita);
        validarTitularidad(cita, dniSolicitante);

        if (cita.getEstado() != EstadoCita.PENDIENTE && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new CupoNoDisponibleException(
                    "Solo se pueden reprogramar citas PENDIENTES o CONFIRMADAS.");
        }

        // RN-04: el plazo mínimo se valida contra la cita ORIGINAL, ya que
        // reprogramar implica abandonarla igual que una cancelación.
        validarPlazoMinimo(cita.getHorario());

        Long idHorarioAnterior = cita.getHorario().getIdHorario();
        if (idHorarioAnterior.equals(idNuevoHorario)) {
            throw new CupoNoDisponibleException("El nuevo horario debe ser distinto al actual.");
        }

        // Bloqueo pesimista del nuevo horario: misma protección de
        // concurrencia que agendarCita().
        HorarioMedico nuevoHorario = horarioMedicoRepository.buscarPorIdConBloqueo(idNuevoHorario)
                .orElseThrow(() -> new CupoNoDisponibleException("El nuevo horario no existe."));

        if (nuevoHorario.getEstado() != EstadoHorario.DISPONIBLE) {
            throw new CupoNoDisponibleException("El nuevo cupo ya no está disponible.");
        }

        nuevoHorario.setEstado(EstadoHorario.RESERVADO);
        nuevoHorario.setLiberadoPorCancelacion(false);
        horarioMedicoRepository.save(nuevoHorario);

        cita.setHorario(nuevoHorario);
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setFechaConfirmacion(null);
        Cita guardada = citaRepository.save(cita);

        notificacionService.notificarCitaConfirmadaOCancelada(guardada);

        // El horario anterior se libera y avisa a la lista de espera,
        // igual que una cancelación normal (RN-07/RN-08).
        liberarYAvisarDisponibilidad(idHorarioAnterior);

        return toResponse(guardada);
    }

    // -----------------------------------------------------------------
    // HU-11: PROPUESTA DE REPROGRAMACIÓN (iniciada por EFI / admin) —
    // ahora acepta VARIAS opciones de horario para que el paciente elija.
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public CitaResponse proponerReprogramacion(Long idCita, List<Long> idsHorarios, String motivo) {
        Cita cita = obtenerCitaOLanzar(idCita);

        if (cita.getEstado() != EstadoCita.PENDIENTE && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new CupoNoDisponibleException(
                    "Solo se puede proponer reprogramación para citas PENDIENTES o CONFIRMADAS.");
        }

        if (idsHorarios == null || idsHorarios.isEmpty()) {
            throw new CupoNoDisponibleException("Debe proponer al menos un horario.");
        }

        // Sin duplicados, preservando el orden en que se enviaron.
        Set<Long> idsUnicos = new LinkedHashSet<>(idsHorarios);

        Long idHorarioActual = cita.getHorario().getIdHorario();
        if (idsUnicos.contains(idHorarioActual)) {
            throw new CupoNoDisponibleException("Ninguna opción propuesta puede ser el horario actual.");
        }

        // Bloqueo pesimista de CADA horario propuesto: nadie más puede
        // tomarlos mientras el paciente decide. Si alguno ya no está
        // disponible, la excepción revierte toda la transacción (ninguno
        // queda reservado a medias).
        List<CitaOpcionReprogramacion> opciones = new ArrayList<>();
        for (Long idHorario : idsUnicos) {
            HorarioMedico horario = horarioMedicoRepository.buscarPorIdConBloqueo(idHorario)
                    .orElseThrow(() -> new CupoNoDisponibleException("Un horario propuesto no existe."));

            if (horario.getEstado() != EstadoHorario.DISPONIBLE) {
                throw new CupoNoDisponibleException(
                        "El horario propuesto para el " + horario.getFecha() + " " + horario.getHoraInicio()
                                + " ya no está disponible.");
            }

            horario.setEstado(EstadoHorario.RESERVADO);
            horario.setLiberadoPorCancelacion(false);
            horarioMedicoRepository.save(horario);

            opciones.add(CitaOpcionReprogramacion.builder().cita(cita).horario(horario).build());
        }

        cita.setEstado(EstadoCita.PENDIENTE_REPROGRAMACION);
        cita.getOpcionesReprogramacion().clear();
        cita.getOpcionesReprogramacion().addAll(opciones);
        cita.setMotivoReprogramacion(motivo);
        Cita guardada = citaRepository.save(cita);

        notificacionService.notificarPropuestaReprogramacion(guardada);

        return toResponse(guardada);
    }

    // -----------------------------------------------------------------
    // HU-11: EL PACIENTE ELIGE UNA OPCIÓN -> LIBERA EL HORARIO VIEJO Y
    // LAS OPCIONES NO ELEGIDAS, SE QUEDA CON LA ELEGIDA Y QUEDA
    // CONFIRMADA (aceptar ya es la confirmación).
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public CitaResponse aceptarReprogramacion(Long idCita, Long idHorarioElegido, String dniSolicitante) {
        Cita cita = obtenerCitaOLanzar(idCita);
        validarTitularidad(cita, dniSolicitante);

        if (cita.getEstado() != EstadoCita.PENDIENTE_REPROGRAMACION || cita.getOpcionesReprogramacion().isEmpty()) {
            throw new CupoNoDisponibleException("Esta cita no tiene una propuesta de reprogramación pendiente.");
        }

        CitaOpcionReprogramacion opcionElegida = cita.getOpcionesReprogramacion().stream()
                .filter(o -> o.getHorario().getIdHorario().equals(idHorarioElegido))
                .findFirst()
                .orElseThrow(() -> new CupoNoDisponibleException(
                        "La opción elegida no forma parte de las propuestas para esta cita."));

        Long idHorarioAnterior = cita.getHorario().getIdHorario();
        HorarioMedico horarioNuevo = opcionElegida.getHorario();

        // Las opciones no elegidas quedan RESERVADO por nada: se liberan
        // igual que un cupo cancelado (mismo aviso FIFO a lista de espera).
        List<Long> idsNoElegidos = cita.getOpcionesReprogramacion().stream()
                .map(o -> o.getHorario().getIdHorario())
                .filter(id -> !id.equals(idHorarioElegido))
                .toList();

        cita.setHorario(horarioNuevo);
        cita.getOpcionesReprogramacion().clear();
        cita.setEstado(EstadoCita.CONFIRMADA);
        cita.setFechaConfirmacion(LocalDateTime.now());
        Cita guardada = citaRepository.save(cita);

        notificacionService.notificarCitaConfirmadaOCancelada(guardada);

        // El horario original (el que ya no aplica) y las opciones que el
        // paciente no eligió siguen el mismo algoritmo FIFO de aviso a
        // lista de espera que una cancelación.
        liberarYAvisarDisponibilidad(idHorarioAnterior);
        idsNoElegidos.forEach(this::liberarYAvisarDisponibilidad);

        return toResponse(guardada);
    }

    // -----------------------------------------------------------------
    // HU-11: EL PACIENTE RECHAZA TODAS LAS OPCIONES -> LIBERA CADA
    // HORARIO PROPUESTO Y EL ORIGINAL, LA CITA QUEDA CANCELADA (el
    // horario original ya no es viable, por eso se propuso reprogramar).
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public CitaResponse rechazarReprogramacion(Long idCita, String dniSolicitante) {
        Cita cita = obtenerCitaOLanzar(idCita);
        validarTitularidad(cita, dniSolicitante);

        if (cita.getEstado() != EstadoCita.PENDIENTE_REPROGRAMACION || cita.getOpcionesReprogramacion().isEmpty()) {
            throw new CupoNoDisponibleException("Esta cita no tiene una propuesta de reprogramación pendiente.");
        }

        List<Long> idsPropuestos = cita.getOpcionesReprogramacion().stream()
                .map(o -> o.getHorario().getIdHorario())
                .toList();
        Long idHorarioOriginal = cita.getHorario().getIdHorario();
        String motivoPropuesta = cita.getMotivoReprogramacion();

        cita.setEstado(EstadoCita.CANCELADA);
        cita.getOpcionesReprogramacion().clear();
        cita.setMotivoCancelacion("Reprogramación rechazada por el paciente"
                + (motivoPropuesta != null && !motivoPropuesta.isBlank() ? " (motivo original: " + motivoPropuesta + ")" : ""));
        Cita guardada = citaRepository.save(cita);

        notificacionService.notificarCitaConfirmadaOCancelada(guardada);

        // Todas las opciones propuestas (nadie las usó) y el horario
        // original (ya no es viable) quedan libres y disparan el aviso
        // FIFO a la lista de espera.
        idsPropuestos.forEach(this::liberarYAvisarDisponibilidad);
        liberarYAvisarDisponibilidad(idHorarioOriginal);

        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> buscarActivasPorMedicoYRango(String cmpMedico, LocalDate desde, LocalDate hasta) {
        List<Cita> citas = (cmpMedico == null || cmpMedico.isBlank())
                ? citaRepository.buscarActivasPorRango(desde, hasta)
                : citaRepository.buscarActivasPorMedicoYRango(cmpMedico, desde, hasta);
        return citas.stream()
                .map(this::toResponse)
                .toList();
    }

    // RN-04: valida que falte al menos "plazoCancelacionHoras" para la
    // fecha/hora del horario, comparado contra el momento actual.
    private void validarPlazoMinimo(HorarioMedico horario) {
        LocalDateTime inicioCita = LocalDateTime.of(horario.getFecha(), horario.getHoraInicio());
        LocalDateTime limite = LocalDateTime.now().plusHours(plazoCancelacionHoras);
        if (inicioCita.isBefore(limite)) {
            throw new PoliticaCancelacionException(
                    "No se puede cancelar ni reprogramar la cita: falta menos de "
                            + plazoCancelacionHoras + " horas para la atención.");
        }
    }

    // RN nueva: la confirmación solo se habilita dentro de los N días
    // previos a la atención (no tiene sentido pedir "confirmar" el mismo
    // día que se agenda una cita que es recién dentro de 2-4 meses).
    private void validarVentanaConfirmacion(HorarioMedico horario) {
        LocalDateTime inicioCita = LocalDateTime.of(horario.getFecha(), horario.getHoraInicio());
        LocalDateTime habilitacion = inicioCita.minusDays(diasAnticipacionConfirmacion);
        if (LocalDateTime.now().isBefore(habilitacion)) {
            throw new ConfirmacionFueraDePlazoException(
                    "Todavía no puedes confirmar esta cita: la confirmación se habilita "
                            + diasAnticipacionConfirmacion + " días antes de la fecha de atención ("
                            + habilitacion.toLocalDate() + ").");
        }
    }

    // -----------------------------------------------------------------
    // RN-07 / RN-08: LIBERA EL CUPO
    // -----------------------------------------------------------------
    // A diferencia de una reasignación automática, aquí NADIE se queda
    // con el cupo por defecto: simplemente vuelve a estar DISPONIBLE
    // (visible para cualquiera en la búsqueda de horarios) y se notifica
    // por correo a TODOS los asegurados en espera activa de esa
    // especialidad. El que primero entre a la plataforma y lo agende se
    // lo queda — protegido por el bloqueo pesimista de agendarCita(), así
    // que no hay condición de carrera aunque varios lo intenten a la vez.
    @Override
    @Transactional
    public void liberarYReasignarCupo(Long idHorario) {
        liberarYAvisarDisponibilidad(idHorario);
    }

    private void liberarYAvisarDisponibilidad(Long idHorario) {
        HorarioMedico horario = horarioMedicoRepository.buscarPorIdConBloqueo(idHorario)
                .orElseThrow(() -> new CupoNoDisponibleException("El horario no existe."));

        horario.setEstado(EstadoHorario.DISPONIBLE);
        horario.setLiberadoPorCancelacion(true);
        horarioMedicoRepository.save(horario);

        Integer idEspecialidad = horario.getMedico().getEspecialidad().getIdEspecialidad();
        List<ListaEspera> enEspera = listaEsperaService.listarActivosPorEspecialidad(idEspecialidad);
        for (ListaEspera registro : enEspera) {
            notificacionService.notificarCupoLiberado(registro, horario);
        }
    }

    // -----------------------------------------------------------------
    // JOB PROGRAMADO: NO-CONFIRMACIÓN 24H ANTES
    // -----------------------------------------------------------------
    @Override
    @Transactional
    public void procesarCitasNoConfirmadas() {
        LocalDateTime limite = LocalDateTime.now().plusHours(24);

        List<Cita> pendientesPorVencer = citaRepository.buscarPendientesPorVencerAntesDe(limite);

        for (Cita cita : pendientesPorVencer) {
            cita.setEstado(EstadoCita.CANCELADA);
            cita.setMotivoCancelacion("Cancelación automática: sin confirmación 24h antes.");
            citaRepository.save(cita);

            notificacionService.notificarCitaConfirmadaOCancelada(cita);

            // Reutiliza el mismo aviso de disponibilidad (RN-08).
            liberarYAvisarDisponibilidad(cita.getHorario().getIdHorario());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listarCitasPorPaciente(String dni) {
        // @Transactional aquí es obligatorio: el mapeo a DTO (que toca
        // cita.horario.medico.especialidad, todas relaciones LAZY) debe
        // ocurrir con la sesión de Hibernate todavía abierta.
        return citaRepository.findByPaciente_DniOrderByFechaRegistroDesc(dni).stream()
                .map(this::toResponse)
                .toList();
    }

    private Cita obtenerCitaOLanzar(Long idCita) {
        return citaRepository.findById(idCita)
                .orElseThrow(() -> new CupoNoDisponibleException("La cita no existe."));
    }

    private void validarTitularidad(Cita cita, String dniSolicitante) {
        if (!cita.getPaciente().getDni().equals(dniSolicitante)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Esta cita no pertenece al paciente autenticado.");
        }
    }

    // Mapeo a DTO dentro de la transacción: evita exponer la entidad JPA
    // (y sus relaciones LAZY: paciente, horario, horario.medico,
    // horario.medico.especialidad) directamente en la respuesta HTTP.
    private CitaResponse toResponse(Cita cita) {
        return new CitaResponse(
                cita.getIdCita(),
                cita.getPaciente().getDni(),
                cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidoPaterno() + " "
                        + cita.getPaciente().getApellidoMaterno(),
                cita.getEstado(),
                cita.getFechaRegistro(),
                cita.getFechaConfirmacion(),
                cita.getMotivoCancelacion(),
                toHorarioResponse(cita.getHorario()),
                cita.getOpcionesReprogramacion().stream()
                        .map(o -> toHorarioResponse(o.getHorario()))
                        .toList(),
                cita.getMotivoReprogramacion());
    }

    private HorarioResponse toHorarioResponse(HorarioMedico h) {
        return new HorarioResponse(
                h.getIdHorario(),
                h.getMedico().getCmp(),
                h.getMedico().getNombres() + " " + h.getMedico().getApellidoPaterno() + " "
                        + h.getMedico().getApellidoMaterno(),
                h.getMedico().getEspecialidad().getIdEspecialidad(),
                h.getMedico().getEspecialidad().getNombre(),
                h.getMedico().getCentroAsistencial(),
                h.getFecha(),
                h.getHoraInicio(),
                h.getHoraFin(),
                h.getFechaActualizacion(),
                h.getLiberadoPorCancelacion());
    }
}

