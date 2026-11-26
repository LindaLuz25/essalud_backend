package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.InformacionGeneral;

import java.util.List;

@Repository
public interface InformacionGeneralRepository extends JpaRepository<InformacionGeneral, Integer> {

    List<InformacionGeneral> findByActivoTrueOrderByOrdenAsc();

    List<InformacionGeneral> findByActivoTrueAndCategoriaOrderByOrdenAsc(String categoria);
}
