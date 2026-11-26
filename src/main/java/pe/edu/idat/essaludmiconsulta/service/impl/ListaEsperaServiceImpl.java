package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.ListaEsperaResponse;
import pe.edu.idat.essaludmiconsulta.exception.CupoNoDisponibleException;
import pe.edu.idat.essaludmiconsulta.model.Especialidad;
import pe.edu.idat.essaludmiconsulta.model.ListaEspera;
import pe.edu.idat.essaludmiconsulta.model.Paciente;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoListaEspera;
import pe.edu.idat.essaludmiconsulta.repository.EspecialidadRepository;
import pe.edu.idat.essaludmiconsulta.repository.ListaEsperaRepository;
import pe.edu.idat.essaludmiconsulta.repository.PacienteRepository;
import pe.edu.idat.essaludmiconsulta.service.ListaEsperaService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListaEsperaServiceImpl implements ListaEsperaService {

    private final ListaEsperaRepository listaEsperaRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadRepository especialidadRepository;

    @Override
    @Transactional
    public ListaEsperaResponse anotarse(String dniPaciente, Integer idEspecialidad) {
        Paciente paciente = pacienteRepository.findById(dniPaciente)
                .orElseThrow(() -> new CupoNoDisponibleException("Paciente no encontrado."));
        Especialidad especialidad = especialidadRepository.findById(idEspecialidad)
                .orElseThrow(() -> new CupoNoDisponibleException("Especialidad no encontrada."));

        ListaEspera registro = ListaEspera.builder()
                .paciente(paciente)
                .especialidad(especialidad)
                .estado(EstadoListaEspera.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .build();

        return toResponse(listaEsperaRepository.save(registro));
    }

    @Override
    @Transactional
    public void retirarse(Long idListaEspera, String dniSolicitante) {
        ListaEspera registro = listaEsperaRepository.findById(idListaEspera)
                .orElseThrow(() -> new CupoNoDisponibleException("Inscripción no encontrada."));

        if (!registro.getPaciente().getDni().equals(dniSolicitante)) {
            throw new AccessDeniedException(
                    "Esta inscripción en lista de espera no pertenece al paciente autenticado.");
        }

        listaEsperaRepository.delete(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaEspera> listarActivosPorEspecialidad(Integer idEspecialidad) {
        return listaEsperaRepository.findByEspecialidad_IdEspecialidadAndEstadoOrderByFechaRegistroAsc(
                idEspecialidad, EstadoListaEspera.ACTIVO);
    }

    @Override
    @Transactional
    public void cerrarEsperaSiExiste(String dniPaciente, Integer idEspecialidad) {
        listaEsperaRepository.findByPaciente_DniAndEstado(dniPaciente, EstadoListaEspera.ACTIVO).stream()
                .filter(registro -> registro.getEspecialidad().getIdEspecialidad().equals(idEspecialidad))
                .forEach(registro -> {
                    registro.setEstado(EstadoListaEspera.ATENDIDO);
                    listaEsperaRepository.save(registro);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaEsperaResponse> listarPorPaciente(String dni) {
        // @Transactional aquí es obligatorio: toResponse() toca
        // registro.especialidad (LAZY) y debe hacerlo con la sesión de
        // Hibernate todavía abierta.
        return listaEsperaRepository.findByPaciente_DniAndEstado(dni, EstadoListaEspera.ACTIVO).stream()
                .map(this::toResponse)
                .toList();
    }

    private ListaEsperaResponse toResponse(ListaEspera registro) {
        return new ListaEsperaResponse(
                registro.getIdListaEspera(),
                registro.getEspecialidad().getIdEspecialidad(),
                registro.getEspecialidad().getNombre(),
                registro.getEstado(),
                registro.getFechaRegistro(),
                registro.getFechaNotificacion());
    }
}
