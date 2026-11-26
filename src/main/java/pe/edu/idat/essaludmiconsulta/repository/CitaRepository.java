package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.Cita;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPaciente_DniOrderByFechaRegistroDesc(String dni);

    Optional<Cita> findByHorario_IdHorarioAndEstadoIn(Long idHorario, List<EstadoCita> estados);


    @Query("""
       SELECT c FROM Cita c
       JOIN c.horario h
       WHERE c.estado = pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita.PENDIENTE
         AND FUNCTION('TIMESTAMP', h.fecha, h.horaInicio) <= :limite
       """)
    List<Cita> buscarPendientesPorVencerAntesDe(@Param("limite") LocalDateTime limite);

    // HU-11: base para la pantalla de EFI — el administrativo busca las
    // citas activas (PENDIENTE o CONFIRMADA) de un médico en un rango de
    // fechas, para decidir cuáles hay que reprogramar (ej. el médico
    // avisó que no atenderá esos días) o confirmar telefónicamente.
    @Query("""
       SELECT c FROM Cita c
       JOIN c.horario h
       WHERE h.medico.cmp = :cmpMedico
         AND h.fecha BETWEEN :desde AND :hasta
         AND c.estado IN (pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita.PENDIENTE,
                           pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita.CONFIRMADA)
       ORDER BY h.fecha ASC, h.horaInicio ASC
       """)
    List<Cita> buscarActivasPorMedicoYRango(@Param("cmpMedico") String cmpMedico,
                                            @Param("desde") LocalDate desde,
                                            @Param("hasta") LocalDate hasta);

    // Igual que buscarActivasPorMedicoYRango pero sin filtrar por médico:
    // vista por defecto de EFI (ej. "citas del mes" de todos los médicos),
    // para que el administrativo no tenga que elegir especialidad/médico
    // antes de ver algo. El filtro por médico sigue disponible para
    // acotar la búsqueda cuando lo necesite.
    @Query("""
       SELECT c FROM Cita c
       JOIN c.horario h
       WHERE h.fecha BETWEEN :desde AND :hasta
         AND c.estado IN (pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita.PENDIENTE,
                           pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita.CONFIRMADA)
       ORDER BY h.fecha ASC, h.horaInicio ASC
       """)
    List<Cita> buscarActivasPorRango(@Param("desde") LocalDate desde,
                                     @Param("hasta") LocalDate hasta);
}

