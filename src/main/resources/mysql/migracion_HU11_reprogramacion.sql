-- HU-11: propuesta de reprogramación administrativa (EFI).
-- Correr una sola vez contra tu base de datos existente (miconsulta_db).

USE miconsulta_db;

ALTER TABLE cita
  ADD COLUMN id_horario_propuesto BIGINT NULL AFTER motivo_cancelacion,
  ADD COLUMN motivo_reprogramacion VARCHAR(255) NULL AFTER id_horario_propuesto,
  ADD CONSTRAINT fk_cita_horario_propuesto
      FOREIGN KEY (id_horario_propuesto) REFERENCES horario_medico(id_horario);

ALTER TABLE cita
  MODIFY COLUMN estado ENUM('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'ASISTIO', 'NO_ASISTIO', 'PENDIENTE_REPROGRAMACION')
  NOT NULL DEFAULT 'PENDIENTE';

-- Usuario administrativo de prueba para loguearse en frontend-efi.
-- Usuario: admin   |   Contraseña: Admin123!
-- (el hash ya está en formato bcrypt, generado para esa contraseña)
INSERT INTO usuario (username, password_hash, rol, activo)
VALUES ('admin', '$2b$10$uMmEAdY8y23WDg4Pe8Gml.cjapJGmlwG2Z.BmBsg2tqtw6sMnspUS', 'ADMIN', 1);
