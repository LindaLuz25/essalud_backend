package pe.edu.idat.essaludmiconsulta.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.idat.essaludmiconsulta.dto.AgendaMedicoResponse;
import pe.edu.idat.essaludmiconsulta.dto.CrearHorarioRequest;
import pe.edu.idat.essaludmiconsulta.dto.HorarioResponse;
import pe.edu.idat.essaludmiconsulta.service.HorarioService;

import java.time.LocalDate;
import java.util.List;

// HU-03: cupos disponibles para una fecha y un médico seleccionados.
// También sirve de base para HU-11/HU-12 del Sprint 3 (disponibilidad
// general por especialidad, incluyendo cupos liberados por cancelación).
// Público (sin login): el asegurado necesita ver la disponibilidad antes
// de registrarse o agendar.
@RestController
@RequestMapping("/api/v1/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @GetMapping
    public ResponseEntity<List<HorarioResponse>> buscarDisponibles(
            @RequestParam(required = false) Integer idEspecialidad,
            @RequestParam(required = false) String cmpMedico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        return ResponseEntity.ok(horarioService.buscarDisponibles(idEspecialidad, cmpMedico, fecha, desde));
    }

    // EFI: el área administrativa crea un nuevo cupo para un médico (ej.
    // para tener con qué ofrecer opciones al proponer una reprogramación
    // cuando no hay suficientes horarios ya cargados).
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioResponse> crear(@Valid @RequestBody CrearHorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.crearHorario(request));
    }

    // HU-13: agenda institucional vigente de un médico específico durante
    // el mes consultado (días y horarios programados). Público (sin
    // login), igual que el resto de consultas de disponibilidad — esto
    // NO reserva ni asigna ningún cupo, solo informa para planificar.
    @GetMapping("/agenda")
    public ResponseEntity<List<AgendaMedicoResponse>> consultarAgenda(
            @RequestParam String cmpMedico,
            @RequestParam int anio,
            @RequestParam int mes) {
        return ResponseEntity.ok(horarioService.consultarAgendaMensual(cmpMedico, anio, mes));
    }
}
