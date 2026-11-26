package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

// HU-11 (extensión): cada horario que el área administrativa (EFI) ofrece
// como alternativa al proponer una reprogramación. Una cita en estado
// PENDIENTE_REPROGRAMACION puede tener VARIAS opciones — el paciente elige
// una sola al aceptar; las demás se liberan.
//
// Los horarios referenciados aquí quedan RESERVADO mientras la propuesta
// está pendiente (igual que el resto del sistema: bloqueo pesimista al
// crearlas, liberación explícita al aceptar/rechazar), para que nadie más
// los tome mientras el paciente decide.
@Entity
@Table(name = "cita_opcion_reprogramacion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_cita", "id_horario"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaOpcionReprogramacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Long idOpcion;

    // Ver el comentario equivalente en Cita.opcionesReprogramacion: se
    // excluye del equals/hashCode/toString para no recursar con el lado
    // inverso de la relación.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario", nullable = false)
    private HorarioMedico horario;
}
