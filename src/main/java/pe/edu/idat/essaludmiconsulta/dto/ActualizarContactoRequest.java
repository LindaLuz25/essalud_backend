package pe.edu.idat.essaludmiconsulta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Extensión "Mi Perfil": solo permite corregir correo y teléfono, que son
// los únicos datos que EsSalud no podría validar/actualizar por el
// asegurado en un caso real (a diferencia de nombres, DNI, fecha de
// nacimiento o tipo de seguro, que dependen del padrón institucional y
// por eso se mantienen de solo lectura en este alcance).
public record ActualizarContactoRequest(
        @NotBlank
        @Email
        String correo,

        String telefono
) {
}
