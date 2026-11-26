package pe.edu.idat.essaludmiconsulta.dto;

import pe.edu.idat.essaludmiconsulta.model.enums.TipoSeguro;

import java.time.LocalDate;

// Extensión "Mi Perfil" (fuera de las HU/RN/RF originales del documento,
// aprobada explícitamente por el usuario): datos del asegurado autenticado
// para la pantalla de perfil. Los datos de identidad (dni, nombres,
// apellidos, fechaNacimiento, tipoSeguro) se muestran de solo lectura;
// únicamente correo y telefono son editables (ver ActualizarContactoRequest).
public record PacienteResponse(
        String dni,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        LocalDate fechaNacimiento,
        TipoSeguro tipoSeguro,
        String correo,
        String telefono
) {
}
