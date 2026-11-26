package pe.edu.idat.essaludmiconsulta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.idat.essaludmiconsulta.dto.InformacionResponse;
import pe.edu.idat.essaludmiconsulta.service.InformacionService;

import java.util.List;

// HU-08 / RF-10: información sobre el proceso de agendamiento y atención
// médica, para que el asegurado resuelva sus dudas sin acudir al módulo
// de admisión. Público (sin login), igual criterio que especialidades,
// médicos, horarios y requisitos.
@RestController
@RequestMapping("/api/v1/informacion")
@RequiredArgsConstructor
public class InformacionController {

    private final InformacionService informacionService;

    @GetMapping
    public ResponseEntity<List<InformacionResponse>> listar(
            @RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return ResponseEntity.ok(informacionService.listarPorCategoria(categoria));
        }
        return ResponseEntity.ok(informacionService.listarTodo());
    }
}

