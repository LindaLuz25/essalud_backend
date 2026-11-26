package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.Medico;

import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, String> {

    List<Medico> findByEspecialidad_IdEspecialidadAndActivoTrue(Integer idEspecialidad);
}

