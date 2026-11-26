package pe.edu.idat.essaludmiconsulta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centraliza el formato de error que recibe el frontend (React) para
 * cualquier excepción de negocio o de validación, en vez de dejar que
 * Spring devuelva su página de error HTML por defecto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CupoNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarCupoNoDisponible(CupoNoDisponibleException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PoliticaCancelacionException.class)
    public ResponseEntity<Map<String, Object>> manejarPoliticaCancelacion(PoliticaCancelacionException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ConfirmacionFueraDePlazoException.class)
    public ResponseEntity<Map<String, Object>> manejarConfirmacionFueraDePlazo(ConfirmacionFueraDePlazoException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AfiliacionInactivaException.class)
    public ResponseEntity<Map<String, Object>> manejarAfiliacionInactiva(AfiliacionInactivaException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(AccessDeniedException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    // Último recurso: cualquier excepción no controlada explícitamente
    // (ej. errores de SQL/JDBC) devuelve un JSON claro con 500 en vez de
    // dejar que Spring haga el forward interno a "/error", que en este
    // proyecto termina devolviendo un 401 confuso (la ruta /error no
    // está permitAll en SecurityConfig y no reautentica el forward).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorNoControlado(Exception ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno no controlado: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}