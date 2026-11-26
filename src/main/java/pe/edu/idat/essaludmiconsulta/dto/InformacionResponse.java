package pe.edu.idat.essaludmiconsulta.dto;

public record InformacionResponse(
        Integer idInformacion,
        String titulo,
        String contenido,
        String categoria
) {
}
