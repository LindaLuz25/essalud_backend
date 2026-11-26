package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.Requisito;

@Repository
public interface RequisitoRepository extends JpaRepository<Requisito, Integer> {
}
