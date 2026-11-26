package pe.edu.idat.essaludmiconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Catálogo de contenido informativo general sobre el proceso de
 * agendamiento y atención médica (HU-08 / RF-10), para que el asegurado
 * resuelva dudas frecuentes sin acudir al módulo de admisión.
 * Es un catálogo de solo lectura para el asegurado: no hay CRUD de
 * administración en este alcance (igual que especialidad/médico/horario,
 * el contenido se administra directamente en la base de datos).
 */
@Entity
@Table(name = "informacion_general")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformacionGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informacion")
    private Integer idInformacion;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
