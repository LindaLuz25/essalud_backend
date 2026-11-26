package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.EspecialidadResponse;

import java.util.List;

public interface EspecialidadService {
    List<EspecialidadResponse> listarActivas();
}
