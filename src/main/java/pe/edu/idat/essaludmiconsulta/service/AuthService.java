package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.LoginRequest;
import pe.edu.idat.essaludmiconsulta.dto.LoginResponse;
import pe.edu.idat.essaludmiconsulta.dto.RegistroPacienteRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    /** Autoregistro público: crea el Paciente y su Usuario (rol PACIENTE) en una sola transacción. */
    LoginResponse registrarPaciente(RegistroPacienteRequest request);
}
