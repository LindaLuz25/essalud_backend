package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.InformacionResponse;

import java.util.List;

public interface InformacionService {

    List<InformacionResponse> listarTodo();

    List<InformacionResponse> listarPorCategoria(String categoria);
}
