package pe.edu.idat.essaludmiconsulta.dto;

public record LoginResponse(
        String token,
        String tipo, // siempre "Bearer"
        String username,
        String rol
) {
}
