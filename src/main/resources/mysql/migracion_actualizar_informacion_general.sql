-- =====================================================================
-- Migración: actualiza el contenido de informacion_general (HU-08 / RF-10)
-- para que coincida con las reglas de negocio vigentes del sistema.
-- Ejecutar una sola vez contra tu base de datos existente (miconsulta_db).
--
-- Qué corrige:
--   1) "¿Qué pasa si no confirmo mi cita?" — antes solo mencionaba las 24h
--      de anticipación como si fueran el plazo para confirmar. En
--      realidad son dos reglas distintas: la opción de confirmar recién
--      se habilita a partir de los N días previos a la atención
--      (citas.dias-anticipacion-confirmacion, por defecto 7 — ver
--      CitaServiceImpl.validarVentanaConfirmacion), y el job automático
--      cancela la cita si sigue sin confirmar cuando faltan menos de 24h
--      (citas.plazo-cancelacion-horas — ver CitaScheduler).
--   2) "¿Puedo cancelar o reprogramar mi cita?" — se deja enfocada solo en
--      la reprogramación que hace el propio paciente (HU-10); el caso de
--      reprogramación propuesta por el área administrativa (HU-11) pasa a
--      tener su propia pregunta, para no mezclar ambos flujos.
--   3) Nueva pregunta: "¿Qué pasa si el centro de salud me propone
--      reprogramar mi cita?" (HU-11) — explica el flujo de propuesta de
--      EFI y dónde aceptar/rechazar (Mis Citas), a juego con el correo
--      que ahora envía NotificacionServiceImpl.notificarPropuestaReprogramacion.
-- =====================================================================

USE miconsulta_db;

-- MySQL Workbench activa por defecto "safe updates", que rechaza un
-- UPDATE/DELETE si el WHERE no usa una columna clave/indexada (aquí
-- filtramos por "titulo", que no es la PK). Se desactiva solo para esta
-- sesión y se reactiva al final; no cambia ninguna configuración global.
SET SQL_SAFE_UPDATES = 0;

UPDATE informacion_general
SET contenido = 'Podrás confirmar tu cita desde "Mis Citas" a partir de los 7 días previos a la fecha de tu atención (antes de eso, la opción de confirmar todavía no aparece habilitada). Si llegado ese plazo no la confirmas y faltan menos de 24 horas para la cita, esta se cancelará automáticamente y el cupo quedará disponible para otro asegurado.'
WHERE titulo = '¿Qué pasa si no confirmo mi cita?';

UPDATE informacion_general
SET contenido = 'Sí, puedes cancelarla o reprogramarla tú mismo desde "Mis Citas", siempre que falten más de 24 horas para la atención.'
WHERE titulo = '¿Puedo cancelar o reprogramar mi cita?';

-- Si ya existe una fila con este título (ej. corriste esta migración antes),
-- no la duplica.
INSERT INTO informacion_general (titulo, contenido, categoria, orden, activo)
SELECT * FROM (SELECT
    '¿Qué pasa si el centro de salud me propone reprogramar mi cita?' AS titulo,
    'Si el médico no va a atender en la fecha programada (por ejemplo, por vacaciones), el área administrativa te propondrá una o varias opciones de nuevo horario y te llegará un correo avisándote. Ingresa a EsSalud Mi Consulta y ve a "Mis Citas": ahí vas a ver tu cita marcada como "Reprogramación pendiente", con esas mismas opciones para que elijas la que prefieras y la aceptes, o rechaces todas si ninguna te sirve. Mientras no elijas una opción, tu cita queda pendiente.' AS contenido,
    'GESTION_CITAS' AS categoria,
    3 AS orden,
    1 AS activo
) AS nueva
WHERE NOT EXISTS (
    SELECT 1 FROM informacion_general
    WHERE titulo = '¿Qué pasa si el centro de salud me propone reprogramar mi cita?'
);

SET SQL_SAFE_UPDATES = 1;
