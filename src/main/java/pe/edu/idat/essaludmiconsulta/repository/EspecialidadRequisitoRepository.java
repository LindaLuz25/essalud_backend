package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.EspecialidadRequisito;
import pe.edu.idat.essaludmiconsulta.model.EspecialidadRequisitoId;

import java.util.List;

@Repository
public interface EspecialidadRequisitoRepository
        extends JpaRepository<EspecialidadRequisito, EspecialidadRequisitoId> {

    List<EspecialidadRequisito> findByEspecialidad_IdEspecialidad(Integer idEspecialidad);
}