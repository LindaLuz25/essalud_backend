package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.idat.essaludmiconsulta.model.enums.TipoSeguro;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @Column(name = "dni", length = 8, nullable = false, updatable = false)
    private String dni;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 80)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_seguro", nullable = false)
    private TipoSeguro tipoSeguro;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "centro_asistencial", length = 150)
    private String centroAsistencial;

    // RN-01: acceso al proceso de agendamiento permitido únicamente a
    // asegurados con afiliación vigente en EsSalud. En este prototipo
    // académico no existe integración real con el padrón de EsSalud, así
    // que el valor se administra directamente en la base de datos (igual
    // que "activo" en Médico/Especialidad) para poder demostrar la regla;
    // el autorregistro (AuthServiceImpl) siempre la crea en TRUE porque no
    // hay forma de verificarla en este alcance.
    @Column(name = "afiliacion_activa", nullable = false)
    private Boolean afiliacionActiva;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (afiliacionActiva == null) {
            afiliacionActiva = true;
        }
    }
}
