package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla puente: qué requisitos (radiografía, análisis, etc.) aplican
 * a cada especialidad, y si son obligatorios o solo recomendados.
 * Puramente informativo (Nivel 1): no bloquea el agendamiento,
 * solo se expone para que el front-end lo muestre al paciente.
 */
@Entity
@Table(name = "especialidad_requisito")
@IdClass(EspecialidadRequisitoId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspecialidadRequisito {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidad")
    private Especialidad especialidad;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_requisito")
    private Requisito requisito;

    @Column(name = "obligatorio", nullable = false)
    private Boolean obligatorio;
}
