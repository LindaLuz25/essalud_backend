package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.EspecialidadResponse;
import pe.edu.idat.essaludmiconsulta.model.Especialidad;
import pe.edu.idat.essaludmiconsulta.model.Medico;
import pe.edu.idat.essaludmiconsulta.repository.EspecialidadRepository;
import pe.edu.idat.essaludmiconsulta.repository.MedicoRepository;
import pe.edu.idat.essaludmiconsulta.service.EspecialidadService;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final MedicoRepository medicoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarActivas() {
        return especialidadRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private EspecialidadResponse toResponse(Especialidad especialidad) {
        List<String> establecimientos = medicoRepository
                .findByEspecialidad_IdEspecialidadAndActivoTrue(especialidad.getIdEspecialidad())
                .stream()
                .map(Medico::getCentroAsistencial)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        return new EspecialidadResponse(
                especialidad.getIdEspecialidad(),
                especialidad.getNombre(),
                especialidad.getDescripcion(),
                establecimientos);
    }
}
