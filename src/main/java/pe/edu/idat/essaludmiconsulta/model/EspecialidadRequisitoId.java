package pe.edu.idat.essaludmiconsulta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clave compuesta para EspecialidadRequisito (tabla puente N:N).
 * Los nombres de campo deben coincidir con los atributos @Id de la entidad,
 * y los tipos deben coincidir con la PK de cada entidad relacionada
 * (Especialidad.idEspecialidad e Integer, Requisito.idRequisito e Integer).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadRequisitoId implements Serializable {
    private Integer especialidad;
    private Integer requisito;
}
