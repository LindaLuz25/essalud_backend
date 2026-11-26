package pe.edu.idat.essaludmiconsulta.dto;

public record RequisitoResponse(
        Integer idRequisito,
        String nombre,
        String descripcion,
        Boolean obligatorio
) {
}
