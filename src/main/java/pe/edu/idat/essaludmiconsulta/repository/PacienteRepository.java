package pe.edu.idat.essaludmiconsulta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.idat.essaludmiconsulta.model.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {
    // El ID es el DNI (String de 8 dígitos)
}

