package pe.edu.idat.essaludmiconsulta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pe.edu.idat.essaludmiconsulta.model.enums.TipoSeguro;

import java.time.LocalDate;

public record RegistroPacienteRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
        String dni,

        @NotBlank
        String nombres,

        @NotBlank
        String apellidoPaterno,

        @NotBlank
        String apellidoMaterno,

        @NotNull
        LocalDate fechaNacimiento,

        @NotNull
        TipoSeguro tipoSeguro,

        @Email
        String correo,

        String telefono,

        @NotBlank
        @Pattern(regexp = "^.{6,}$", message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {
}
