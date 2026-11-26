package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.LoginRequest;
import pe.edu.idat.essaludmiconsulta.dto.LoginResponse;
import pe.edu.idat.essaludmiconsulta.dto.RegistroPacienteRequest;
import pe.edu.idat.essaludmiconsulta.exception.CupoNoDisponibleException;
import pe.edu.idat.essaludmiconsulta.model.Paciente;
import pe.edu.idat.essaludmiconsulta.model.Usuario;
import pe.edu.idat.essaludmiconsulta.model.enums.RolUsuario;
import pe.edu.idat.essaludmiconsulta.repository.PacienteRepository;
import pe.edu.idat.essaludmiconsulta.repository.UsuarioRepository;
import pe.edu.idat.essaludmiconsulta.security.JwtService;
import pe.edu.idat.essaludmiconsulta.security.UserPrincipal;
import pe.edu.idat.essaludmiconsulta.service.AuthService;



@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Delega la verificación de la contraseña en el AuthenticationManager
        // (usa el DaoAuthenticationProvider + BCrypt configurados en SecurityConfig).
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generarToken(principal);

        return new LoginResponse(token, "Bearer", principal.getUsername(),
                principal.getUsuario().getRol().name());
    }

    @Override
    @Transactional
    public LoginResponse registrarPaciente(RegistroPacienteRequest request) {

        if (pacienteRepository.existsById(request.dni())) {
            throw new CupoNoDisponibleException("Ya existe un paciente registrado con ese DNI.");
        }
        if (usuarioRepository.existsByUsername(request.dni())) {
            throw new CupoNoDisponibleException("Ya existe un usuario con ese DNI.");
        }

        Paciente paciente = Paciente.builder()
                .dni(request.dni())
                .nombres(request.nombres())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .fechaNacimiento(request.fechaNacimiento())
                .tipoSeguro(request.tipoSeguro())
                .correo(request.correo())
                .telefono(request.telefono())
                .build();
        pacienteRepository.save(paciente);

        Usuario usuario = Usuario.builder()
                .username(request.dni()) // login = DNI
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(RolUsuario.PACIENTE)
                .paciente(paciente)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(new UserPrincipal(usuario));

        return new LoginResponse(token, "Bearer", usuario.getUsername(), usuario.getRol().name());
    }
}


