package pe.edu.idat.essaludmiconsulta.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.idat.essaludmiconsulta.service.CitaService;

@Component
@RequiredArgsConstructor
public class CitaScheduler {

    private final CitaService citaService;

    // Corre cada minuto: detecta citas pendientes cuyo horario ya está
    // a menos de 24h y no fueron confirmadas por el paciente. Las cancela
    // y dispara la liberación/reasignación automática del cupo.
    @Scheduled(fixedRate = 60_000)
    public void ejecutarLiberacionPorNoConfirmacion() {
        citaService.procesarCitasNoConfirmadas();
    }
}
