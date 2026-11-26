package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.ListaEspera;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoListaEspera;

import java.util.List;

@Repository
public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {

    // RN-08: todos los asegurados en espera ACTIVA de una especialidad, en
    // orden de registro. Ya no se usa para "reclamar" un único registro con
    // bloqueo (no hay reasignación automática): se usa solo para notificar
    // a todos por igual cuando se libera un cupo. Por eso ya no necesita
    // @Lock ni query nativa con FOR UPDATE.
    List<ListaEspera> findByEspecialidad_IdEspecialidadAndEstadoOrderByFechaRegistroAsc(
            Integer idEspecialidad, EstadoListaEspera estado);

    List<ListaEspera> findByPaciente_DniAndEstado(String dni, EstadoListaEspera estado);
}
