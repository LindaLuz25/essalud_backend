package pe.edu.idat.essaludmiconsulta.service.impl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.BloqueAtencionResponse;
import pe.edu.idat.essaludmiconsulta.dto.MedicoResponse;
import pe.edu.idat.essaludmiconsulta.model.Medico;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoHorario;
import pe.edu.idat.essaludmiconsulta.repository.HorarioMedicoRepository;
import pe.edu.idat.essaludmiconsulta.repository.MedicoRepository;
import pe.edu.idat.essaludmiconsulta.service.MedicoService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listarPorEspecialidad(Integer idEspecialidad, LocalDate desde) {
        LocalDate fechaDesde = (desde != null) ? desde : LocalDate.now();

        return medicoRepository.findByEspecialidad_IdEspecialidadAndActivoTrue(idEspecialidad).stream()
                .map(medico -> toResponse(medico, fechaDesde))
                .toList();
    }

    private MedicoResponse toResponse(Medico medico, LocalDate desde) {
        // Deriva los bloques recurrentes (día de la semana + rango horario)
        // a partir de los cupos ya registrados para ese médico, sin importar
        // su estado (disponible/reservado): esto es solo para mostrar el
        // patrón de atención, no la disponibilidad puntual (eso es HU-03).
        List<BloqueAtencionResponse> bloques = horarioMedicoRepository
                .buscarDesdeFechaPorMedico(medico.getCmp(), desde).stream()
                .filter(h -> h.getEstado() != EstadoHorario.CANCELADO && h.getEstado() != EstadoHorario.BLOQUEADO)
                .map(h -> new BloqueAtencionResponse(
                        h.getFecha().getDayOfWeek(), h.getHoraInicio(), h.getHoraFin()))
                .distinct()
                .sorted(Comparator.comparing(BloqueAtencionResponse::dia)
                        .thenComparing(BloqueAtencionResponse::horaInicio))
                .toList();

        return new MedicoResponse(
                medico.getCmp(),
                medico.getNombres() + " " + medico.getApellidoPaterno() + " " + medico.getApellidoMaterno(),
                medico.getCentroAsistencial(),
                bloques);
    }
}
