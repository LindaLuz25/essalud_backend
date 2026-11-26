package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pe.edu.idat.essaludmiconsulta.model.enums.EstadoCita;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cita")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_paciente", nullable = false)
    private Paciente paciente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario", nullable = false, unique = true)
    private HorarioMedico horario;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    // HU-11: una o varias opciones de horario que el área administrativa
    // (EFI) propone como reemplazo mientras la cita está
    // PENDIENTE_REPROGRAMACION. Vacía en cualquier otro estado. Al
    // aceptar, la opción elegida pasa a ser el `horario` de la cita y el
    // resto se libera; al rechazar, todas se liberan.
    // @ToString/@EqualsAndHashCode excluidos: es el lado inverso de una
    // relación bidireccional con CitaOpcionReprogramacion, que a su vez
    // referencia de vuelta a Cita. Sin excluirlo, el equals/hashCode/
    // toString generado por Lombok en ambas entidades se llamaría uno a
    // otro sin fin (StackOverflowError) apenas alguno se loguee, compare
    // o se guarde en un Set/Map.
    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CitaOpcionReprogramacion> opcionesReprogramacion = new ArrayList<>();

    // HU-11: motivo ingresado por el administrativo en EFI (ej. "médico de
    // vacaciones"). Se conserva como historial incluso después de aceptar,
    // para que el paciente vea por qué cambió su cita.
    @Column(name = "motivo_reprogramacion", length = 255)
    private String motivoReprogramacion;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
