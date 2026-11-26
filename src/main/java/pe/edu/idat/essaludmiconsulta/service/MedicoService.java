package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.MedicoResponse;

import java.time.LocalDate;
import java.util.List;

public interface MedicoService {
    List<MedicoResponse> listarPorEspecialidad(Integer idEspecialidad, LocalDate desde);
}
