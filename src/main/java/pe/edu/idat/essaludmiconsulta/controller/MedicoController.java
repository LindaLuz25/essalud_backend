package pe.edu.idat.essaludmiconsulta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.idat.essaludmiconsulta.dto.MedicoResponse;
import pe.edu.idat.essaludmiconsulta.service.MedicoService;

import java.time.LocalDate;
import java.util.List;

// HU-02: médicos que atienden la especialidad seleccionada, junto con sus
// días y rangos horarios de atención. Público (sin login), mismo criterio
// que EspecialidadController y RequisitoController.
@RestController
@RequestMapping("/api/v1/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listarPorEspecialidad(
            @RequestParam Integer idEspecialidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        return ResponseEntity.ok(medicoService.listarPorEspecialidad(idEspecialidad, desde));
    }
}
