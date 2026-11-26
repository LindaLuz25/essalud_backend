package pe.edu.idat.essaludmiconsulta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.idat.essaludmiconsulta.dto.ActualizarContactoRequest;
import pe.edu.idat.essaludmiconsulta.dto.PacienteResponse;
import pe.edu.idat.essaludmiconsulta.security.UserPrincipal;
import pe.edu.idat.essaludmiconsulta.service.PacienteService;

// Extensión "Mi Perfil" (fuera de HU/RN/RF originales del documento,
// aprobada explícitamente por el usuario el 2026-07-19, pendiente de
// sustentar por escrito en el Word junto con las demás extensiones).
// Igual que en CitaController: el DNI se toma SIEMPRE del token JWT
// autenticado, nunca de un parámetro de la URL, para que un paciente
// no pueda leer/editar los datos de otro.
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> obtenerPerfil(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pacienteService.obtenerPerfil(principal.getDniPaciente()));
    }

    @PatchMapping("/me/contacto")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> actualizarContacto(@AuthenticationPrincipal UserPrincipal principal,
                                                               @Valid @RequestBody ActualizarContactoRequest request) {
        return ResponseEntity.ok(pacienteService.actualizarContacto(principal.getDniPaciente(), request));
    }
}

