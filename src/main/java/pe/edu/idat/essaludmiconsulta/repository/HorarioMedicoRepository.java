package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.HorarioMedico;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoHorario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {

    // Búsqueda pública de cupos disponibles por especialidad (para la pantalla de agendamiento)
    @Query("""
       SELECT h FROM HorarioMedico h
       JOIN FETCH h.medico m
       WHERE m.especialidad.idEspecialidad = :idEspecialidad
         AND h.estado = pe.edu.idat.essaludmiconsulta.model.enums.EstadoHorario.DISPONIBLE
         AND h.fecha >= :desde
       ORDER BY h.fecha ASC, h.horaInicio ASC
       """)
    List<HorarioMedico> buscarDisponiblesPorEspecialidad(@Param("idEspecialidad") Integer idEspecialidad,
                                                         @Param("desde") LocalDate desde);

    // Bloqueo pesimista: usado justo antes de reservar/reasignar un cupo
    // para evitar que dos peticiones concurrentes tomen el mismo horario
    // liberado (condición de carrera propia de la "liberación en milisegundos").
    //
    // Se escribe como query NATIVA con "FOR UPDATE" simple (sin @Lock ni
    // JPQL) a propósito: Hibernate en esta versión genera "FOR UPDATE OF
    // <alias>" para @Lock(PESSIMISTIC_WRITE), sintaxis que MariaDB no
    // soporta (a diferencia de MySQL 8.0.20+). Escribir el SQL a mano evita
    // que Hibernate decida la cláusula de bloqueo por nosotros.
    @Query(value = "SELECT * FROM horario_medico WHERE id_horario = :id FOR UPDATE", nativeQuery = true)
    Optional<HorarioMedico> buscarPorIdConBloqueo(@Param("id") Long id);

    List<HorarioMedico> findByEstadoAndFecha(EstadoHorario estado, LocalDate fecha);

    // HU-03: cupos disponibles para un médico y una fecha seleccionados.
    List<HorarioMedico> findByMedico_CmpAndFechaAndEstadoOrderByHoraInicioAsc(
            String cmpMedico, LocalDate fecha, EstadoHorario estado);

    // HU-02: base para derivar los días y rangos horarios de atención de un
    // médico (se agrupan/deduplican en el servicio, no aquí).
    @Query("""
       SELECT h FROM HorarioMedico h
       WHERE h.medico.cmp = :cmpMedico
         AND h.fecha >= :desde
       ORDER BY h.fecha ASC, h.horaInicio ASC
       """)
    List<HorarioMedico> buscarDesdeFechaPorMedico(@Param("cmpMedico") String cmpMedico,
                                                  @Param("desde") LocalDate desde);

    // EFI: evita crear dos veces el mismo cupo (choca con la restricción
    // única cmp_medico+fecha+hora_inicio de la tabla) — se valida antes de
    // insertar para devolver un mensaje de negocio claro en vez de un
    // error de SQL.
    boolean existsByMedico_CmpAndFechaAndHoraInicio(String cmpMedico, LocalDate fecha,
                                                    java.time.LocalTime horaInicio);

    // HU-13: agenda institucional vigente de un médico acotada a un rango
    // de fechas (el mes consultado). A diferencia de buscarDesdeFechaPorMedico
    // (HU-02, que no tiene límite superior y solo sirve para derivar el
    // patrón semanal), aquí sí importan las fechas concretas dentro del mes,
    // y se incluyen los horarios sin importar su estado (el filtrado de
    // CANCELADO/BLOQUEADO se hace en el servicio, igual que en HU-02).
    @Query("""
       SELECT h FROM HorarioMedico h
       WHERE h.medico.cmp = :cmpMedico
         AND h.fecha >= :desde AND h.fecha <= :hasta
       ORDER BY h.fecha ASC, h.horaInicio ASC
       """)
    List<HorarioMedico> buscarPorMedicoYRangoFechas(@Param("cmpMedico") String cmpMedico,
                                                     @Param("desde") LocalDate desde,
                                                     @Param("hasta") LocalDate hasta);
}

