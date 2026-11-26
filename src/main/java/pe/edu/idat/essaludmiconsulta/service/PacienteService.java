package pe.edu.idat.essaludmiconsulta.service;

import pe.edu.idat.essaludmiconsulta.dto.ActualizarContactoRequest;
import pe.edu.idat.essaludmiconsulta.dto.PacienteResponse;

// Extensión "Mi Perfil" (fuera de HU/RN/RF originales, aprobada
// explícitamente por el usuario el 2026-07-19): el asegurado necesita
// poder corregir su correo/teléfono si los cargó mal en el registro,
// porque de eso dependen las notificaciones de RF-05/RN-08.
public interface PacienteService {

    PacienteResponse obtenerPerfil(String dni);

    PacienteResponse actualizarContacto(String dni, ActualizarContactoRequest request);
}
