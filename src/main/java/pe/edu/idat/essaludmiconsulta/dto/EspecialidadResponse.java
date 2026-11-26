package pe.edu.idat.essaludmiconsulta.dto;

import java.util.List;

// HU-01: especialidades junto con los establecimientos de salud donde se brindan.
public record EspecialidadResponse(
        Integer idEspecialidad,
        String nombre,
        String descripcion,
        List<String> establecimientos
) {
}
