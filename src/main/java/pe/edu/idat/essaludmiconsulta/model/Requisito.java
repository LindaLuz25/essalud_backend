package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Catálogo maestro de requisitos previos que un paciente podría necesitar
 * antes de una cita (ej: radiografía, análisis de sangre).
 * Reutilizable entre varias especialidades vía especialidad_requisito.
 */
@Entity
@Table(name = "requisito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Requisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_requisito")
    private Integer idRequisito;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;
}
