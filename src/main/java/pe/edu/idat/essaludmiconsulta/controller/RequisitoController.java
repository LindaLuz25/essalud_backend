package pe.edu.idat.essaludmiconsulta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.idat.essaludmiconsulta.dto.RequisitoResponse;
import pe.edu.idat.essaludmiconsulta.service.RequisitoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especialidades")
@RequiredArgsConstructor
public class RequisitoController {

    private final RequisitoService requisitoService;

    // Público (sin login): el paciente debe poder verlo ANTES de agendar,
    // incluso antes de registrarse. Ya cubierto por SecurityConfig:
    //   .requestMatchers(HttpMethod.GET, "/api/v1/especialidades/**").permitAll()
    // No requiere cambios adicionales en SecurityConfig.
    @GetMapping("/{idEspecialidad}/requisitos")
    public ResponseEntity<List<RequisitoResponse>> listarRequisitos(@PathVariable Integer idEspecialidad) {
        return ResponseEntity.ok(requisitoService.listarPorEspecialidad(idEspecialidad));
    }
}
