package pe.edu.idat.essaludmiconsulta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.idat.essaludmiconsulta.dto.ListaEsperaResponse;
import pe.edu.idat.essaludmiconsulta.security.UserPrincipal;
import pe.edu.idat.essaludmiconsulta.service.ListaEsperaService;


import java.util.List;

@RestController
@RequestMapping("/api/v1/lista-espera")
@RequiredArgsConstructor
public class ListaEsperaController {

    private final ListaEsperaService listaEsperaService;

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<ListaEsperaResponse> anotarse(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestParam Integer idEspecialidad) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listaEsperaService.anotarse(principal.getDniPaciente(), idEspecialidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> retirarse(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id) {
        listaEsperaService.retirarse(id, principal.getDniPaciente());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<ListaEsperaResponse>> misInscripciones(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(listaEsperaService.listarPorPaciente(principal.getDniPaciente()));
    }
}


