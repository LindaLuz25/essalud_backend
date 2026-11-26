package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.idat.essaludmiconsulta.model.enums.RolUsuario;

import java.time.LocalDateTime;

/**
 * Tabla de autenticación, independiente de los datos de negocio.
 * - Para un PACIENTE: username = DNI, y dniPaciente apunta al mismo DNI.
 * - Para un MEDICO: username = CMP, y cmpMedico apunta al mismo CMP.
 * - Para un ADMIN: username propio (ej. correo institucional), sin
 *   vínculo a paciente ni médico.
 */
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    // Relación opcional: solo aplica cuando rol = PACIENTE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_paciente")
    private Paciente paciente;

    // Relación opcional: solo aplica cuando rol = MEDICO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmp_medico")
    private Medico medico;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
}
