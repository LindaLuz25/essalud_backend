-- =====================================================================
-- Migración: reprogramación con MÚLTIPLES opciones de horario (HU-11)
-- Ejecutar sobre una base de datos que YA tiene aplicada
-- migracion_HU11_reprogramacion.sql (columna id_horario_propuesto en cita).
--
-- Qué hace:
--   1) Crea la tabla cita_opcion_reprogramacion, donde ahora viven las
--      opciones de horario propuestas por EFI (una cita puede tener
--      varias; antes solo se guardaba una en cita.id_horario_propuesto).
--   2) Migra cualquier propuesta pendiente que ya exista (si una cita
--      está PENDIENTE_REPROGRAMACION con id_horario_propuesto, se crea su
--      fila equivalente en la tabla nueva, para no perderla).
--   3) Elimina la columna id_horario_propuesto de cita (ya no se usa).
-- =====================================================================

USE miconsulta_db;

-- 1) Nueva tabla de opciones
CREATE TABLE IF NOT EXISTS cita_opcion_reprogramacion (
    id_opcion    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_cita      BIGINT NOT NULL,
    id_horario   BIGINT NOT NULL,
    CONSTRAINT fk_opcion_cita
        FOREIGN KEY (id_cita) REFERENCES cita(id_cita),
    CONSTRAINT fk_opcion_horario
        FOREIGN KEY (id_horario) REFERENCES horario_medico(id_horario),
    CONSTRAINT uq_opcion_cita_horario UNIQUE (id_cita, id_horario)
) ENGINE=InnoDB;

CREATE INDEX idx_opcion_cita ON cita_opcion_reprogramacion (id_cita);

-- 2) Migra propuestas pendientes existentes (si las hubiera) a la tabla nueva
INSERT INTO cita_opcion_reprogramacion (id_cita, id_horario)
SELECT id_cita, id_horario_propuesto
FROM cita
WHERE estado = 'PENDIENTE_REPROGRAMACION'
  AND id_horario_propuesto IS NOT NULL;

-- 3) Ya no se usa: la propuesta ahora vive en cita_opcion_reprogramacion
ALTER TABLE cita
    DROP FOREIGN KEY fk_cita_horario_propuesto,
    DROP COLUMN id_horario_propuesto;
