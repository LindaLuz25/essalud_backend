package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoHorario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "horario_medico",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cmp_medico", "fecha", "hora_inicio"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long idHorario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmp_medico", nullable = false)
    private Medico medico;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoHorario estado;

    // HU-12: distingue un cupo que acaba de volver al pool público por una
    // cancelación/reprogramación (para que el frontend lo pueda destacar),
    // de uno que nunca fue tomado. Se apaga apenas alguien lo reserva.
    @Column(name = "liberado_por_cancelacion", nullable = false)
    @Builder.Default
    private Boolean liberadoPorCancelacion = false;

    // Bloqueo optimista: evita que dos peticiones concurrentes reserven
    // el mismo cupo liberado en milisegundos (condición de carrera).
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
