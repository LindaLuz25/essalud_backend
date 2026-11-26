package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.ActualizarContactoRequest;
import pe.edu.idat.essaludmiconsulta.dto.PacienteResponse;
import pe.edu.idat.essaludmiconsulta.exception.CupoNoDisponibleException;
import pe.edu.idat.essaludmiconsulta.model.Paciente;
import pe.edu.idat.essaludmiconsulta.repository.PacienteRepository;
import pe.edu.idat.essaludmiconsulta.service.PacienteService;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPerfil(String dni) {
        return toResponse(obtenerPacienteOLanzar(dni));
    }

    @Override
    @Transactional
    public PacienteResponse actualizarContacto(String dni, ActualizarContactoRequest request) {
        Paciente paciente = obtenerPacienteOLanzar(dni);

        paciente.setCorreo(request.correo());
        paciente.setTelefono(request.telefono());

        Paciente guardado = pacienteRepository.save(paciente);
        return toResponse(guardado);
    }

    private Paciente obtenerPacienteOLanzar(String dni) {
        return pacienteRepository.findById(dni)
                .orElseThrow(() -> new CupoNoDisponibleException("Paciente no encontrado."));
    }

    private PacienteResponse toResponse(Paciente p) {
        return new PacienteResponse(
                p.getDni(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                p.getFechaNacimiento(),
                p.getTipoSeguro(),
                p.getCorreo(),
                p.getTelefono());
    }
}
