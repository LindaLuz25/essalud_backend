package pe.edu.idat.essaludmiconsulta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.idat.essaludmiconsulta.dto.CitaResponse;
import pe.edu.idat.essaludmiconsulta.security.UserPrincipal;
import pe.edu.idat.essaludmiconsulta.service.CitaService;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // El DNI se toma del token JWT autenticado, NUNCA de un parámetro del
    // cliente: así un paciente no puede agendar una cita a nombre de otro.
    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponse> agendar(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam Long idHorario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citaService.agendarCita(principal.getDniPaciente(), idHorario));
    }

    // El paciente ya NO confirma su propia cita: en la práctica, el área
    // administrativa llama por teléfono al asegurado y confirma desde EFI
    // una vez que le contestó. Por eso este endpoint es ADMIN-only y no
    // recibe el DNI del paciente (no aplica validación de titularidad).
    @PatchMapping("/{idCita}/confirmar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CitaResponse> confirmar(@PathVariable Long idCita) {
        return ResponseEntity.ok(citaService.confirmarCita(idCita));
    }

    @PatchMapping("/{idCita}/cancelar")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponse> cancelar(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable Long idCita,
                                                 @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(citaService.cancelarCita(idCita, motivo, principal.getDniPaciente()));
    }

    // HU-10 / RN-04 / RN-07: reprograma la cita a un nuevo horario
    // disponible, respetando el plazo mínimo de anticipación sobre la
    // cita original y liberando/reasignando el cupo anterior.
    @PatchMapping("/{idCita}/reprogramar")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponse> reprogramar(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long idCita,
                                                    @RequestParam Long idNuevoHorario) {
        return ResponseEntity.ok(
                citaService.reprogramarCita(idCita, idNuevoHorario, principal.getDniPaciente()));
    }

    // El propio paciente ve solo su historial (no requiere pasar el DNI en la URL).
    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<CitaResponse>> misCitas(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(citaService.listarCitasPorPaciente(principal.getDniPaciente()));
    }

    // Uso administrativo/staff: consultar el historial de cualquier paciente por DNI.
    @GetMapping("/paciente/{dni}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CitaResponse>> listarPorPaciente(@PathVariable String dni) {
        return ResponseEntity.ok(citaService.listarCitasPorPaciente(dni));
    }

    // HU-11 / EFI: el administrativo busca las citas activas en un rango
    // de fechas, para decidir cuáles reprogramar (ej. el médico avisó que
    // no atenderá esos días) o confirmar telefónicamente. cmpMedico es
    // opcional: sin él, trae las de TODOS los médicos en ese rango (vista
    // por defecto "citas del mes" de EFI); con él, acota a ese médico.
    @GetMapping("/admin/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CitaResponse>> buscarActivasPorMedico(
            @RequestParam(required = false) String cmpMedico,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(citaService.buscarActivasPorMedicoYRango(cmpMedico, desde, hasta));
    }

    // HU-11 / EFI: el administrativo propone una o varias opciones de
    // horario para una cita (ej. médico de vacaciones), con motivo. La
    // cita queda PENDIENTE_REPROGRAMACION a la espera de que el paciente
    // elija una de las opciones.
    @PatchMapping("/{idCita}/proponer-reprogramacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CitaResponse> proponerReprogramacion(@PathVariable Long idCita,
                                                                @RequestParam List<Long> idsHorarios,
                                                                @RequestParam String motivo) {
        return ResponseEntity.ok(citaService.proponerReprogramacion(idCita, idsHorarios, motivo));
    }

    // HU-11: el paciente acepta UNA de las opciones propuestas por EFI.
    @PatchMapping("/{idCita}/aceptar-reprogramacion")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponse> aceptarReprogramacion(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable Long idCita,
                                                               @RequestParam Long idHorarioElegido) {
        return ResponseEntity.ok(
                citaService.aceptarReprogramacion(idCita, idHorarioElegido, principal.getDniPaciente()));
    }

    // HU-11: el paciente rechaza la propuesta de reprogramación de EFI.
    @PatchMapping("/{idCita}/rechazar-reprogramacion")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponse> rechazarReprogramacion(@AuthenticationPrincipal UserPrincipal principal,
                                                                @PathVariable Long idCita) {
        return ResponseEntity.ok(citaService.rechazarReprogramacion(idCita, principal.getDniPaciente()));
    }
}
