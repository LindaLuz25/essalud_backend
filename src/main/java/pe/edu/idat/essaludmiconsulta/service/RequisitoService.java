package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.RequisitoResponse;

import java.util.List;

public interface RequisitoService {
    List<RequisitoResponse> listarPorEspecialidad(Integer idEspecialidad);
}
