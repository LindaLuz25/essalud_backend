package pe.edu.idat.essaludmiconsulta.dto;

import java.util.List;

// HU-02: médicos que atienden la especialidad seleccionada, junto con sus
// días y rangos horarios de atención (derivados de los horarios vigentes).
public record MedicoResponse(
        String cmp,
        String nombreCompleto,
        String centroAsistencial,
        List<BloqueAtencionResponse> horariosAtencion
) {
}
