package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.AgendaMedicoResponse;
import pe.edu.idat.essaludmiconsulta.dto.CrearHorarioRequest;
import pe.edu.idat.essaludmiconsulta.dto.HorarioResponse;
import pe.edu.idat.essaludmiconsulta.exception.CupoNoDisponibleException;
import pe.edu.idat.essaludmiconsulta.model.HorarioMedico;
import pe.edu.idat.essaludmiconsulta.model.Medico;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoHorario;
import pe.edu.idat.essaludmiconsulta.repository.HorarioMedicoRepository;
import pe.edu.idat.essaludmiconsulta.repository.MedicoRepository;
import pe.edu.idat.essaludmiconsulta.service.HorarioService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioServiceImpl implements HorarioService {

    private final HorarioMedicoRepository horarioMedicoRepository;
    private final MedicoRepository medicoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HorarioResponse> buscarDisponibles(Integer idEspecialidad, String cmpMedico,
                                                   LocalDate fecha, LocalDate desde) {

        if (cmpMedico != null && fecha != null) {
            // HU-03: cupos de un médico y una fecha exactos.
            return horarioMedicoRepository
                    .findByMedico_CmpAndFechaAndEstadoOrderByHoraInicioAsc(cmpMedico, fecha, EstadoHorario.DISPONIBLE)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (idEspecialidad != null) {
            // Búsqueda por especialidad (usada también en HU-11/HU-12 del Sprint 3
            // para mostrar disponibilidad general, incluyendo cupos liberados).
            LocalDate fechaDesde = (desde != null) ? desde : LocalDate.now();
            return horarioMedicoRepository
                    .buscarDisponiblesPorEspecialidad(idEspecialidad, fechaDesde)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        throw new IllegalArgumentException(
                "Debe indicar 'idEspecialidad', o 'cmpMedico' junto con 'fecha'.");
    }

    @Override
    @Transactional
    public HorarioResponse crearHorario(CrearHorarioRequest request) {
        Medico medico = medicoRepository.findById(request.cmpMedico())
                .orElseThrow(() -> new IllegalArgumentException("El médico indicado no existe."));

        if (!Boolean.TRUE.equals(medico.getActivo())) {
            throw new IllegalArgumentException("El médico indicado no está activo.");
        }

        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        if (request.fecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede crear un horario en una fecha pasada.");
        }

        if (horarioMedicoRepository.existsByMedico_CmpAndFechaAndHoraInicio(
                request.cmpMedico(), request.fecha(), request.horaInicio())) {
            throw new CupoNoDisponibleException(
                    "Ya existe un horario para ese médico en esa fecha y hora.");
        }

        HorarioMedico horario = HorarioMedico.builder()
                .medico(medico)
                .fecha(request.fecha())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .estado(EstadoHorario.DISPONIBLE)
                .liberadoPorCancelacion(false)
                .version(0)
                .build();

        return toResponse(horarioMedicoRepository.save(horario));
    }

    // -----------------------------------------------------------------
    // HU-13: agenda institucional vigente de un médico para el mes
    // consultado — solo lectura, no reserva ni asigna ningún cupo.
    // -----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<AgendaMedicoResponse> consultarAgendaMensual(String cmpMedico, int anio, int mes) {
        Medico medico = medicoRepository.findById(cmpMedico)
                .orElseThrow(() -> new IllegalArgumentException("El médico indicado no existe."));

        if (!Boolean.TRUE.equals(medico.getActivo())) {
            throw new IllegalArgumentException("El médico indicado no está activo.");
        }

        YearMonth mesConsultado = YearMonth.of(anio, mes);
        LocalDate desde = mesConsultado.atDay(1);
        LocalDate hasta = mesConsultado.atEndOfMonth();

        return horarioMedicoRepository.buscarPorMedicoYRangoFechas(cmpMedico, desde, hasta).stream()
                // Mismo criterio que el patrón semanal de HU-02: un horario
                // CANCELADO/BLOQUEADO no es parte de la agenda vigente.
                .filter(h -> h.getEstado() != EstadoHorario.CANCELADO && h.getEstado() != EstadoHorario.BLOQUEADO)
                .map(this::toAgendaResponse)
                .toList();
    }

    private AgendaMedicoResponse toAgendaResponse(HorarioMedico h) {
        return new AgendaMedicoResponse(
                h.getFecha(),
                h.getFecha().getDayOfWeek(),
                h.getHoraInicio(),
                h.getHoraFin(),
                h.getEstado() == EstadoHorario.DISPONIBLE);
    }

    // Mapeo a DTO dentro de la transacción: evita exponer la entidad JPA
    // (y sus relaciones LAZY) directamente en la respuesta HTTP.
    private HorarioResponse toResponse(HorarioMedico h) {
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
