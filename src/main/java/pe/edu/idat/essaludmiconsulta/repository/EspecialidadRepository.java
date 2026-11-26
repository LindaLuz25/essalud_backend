package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.Especialidad;

import java.util.List;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {

    // HU-01: solo especialidades activas se muestran al asegurado.
    List<Especialidad> findByActivoTrueOrderByNombreAsc();
}

