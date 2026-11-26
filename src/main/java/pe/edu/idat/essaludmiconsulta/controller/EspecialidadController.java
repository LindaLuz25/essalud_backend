package pe.edu.idat.essaludmiconsulta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.idat.essaludmiconsulta.dto.EspecialidadResponse;
import pe.edu.idat.essaludmiconsulta.service.EspecialidadService;

import java.util.List;

// HU-01: consultar especialidades médicas junto con los establecimientos
// de salud donde se brindan. Público (sin login), igual que
// RequisitoController: el asegurado necesita ver la oferta antes de
// registrarse. Ya cubierto por SecurityConfig
// (.requestMatchers(HttpMethod.GET, "/api/v1/especialidades/**").permitAll()).
@RestController
@RequestMapping("/api/v1/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @GetMapping
    public ResponseEntity<List<EspecialidadResponse>> listar() {
        return ResponseEntity.ok(especialidadService.listarActivas());
    }
}
