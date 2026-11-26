-- =====================================================================
-- Migración: segunda ronda de correcciones a informacion_general
-- (HU-08 / RF-10). Ejecutar una sola vez contra tu base de datos
-- existente (miconsulta_db), después de migracion_actualizar_informacion_general.sql.
--
-- Qué corrige:
--   1) "¿Puedo cancelar o reprogramar mi cita?" -> "¿Puedo cancelar mi
--      cita?": el frontend del paciente nunca tuvo un botón para que él
--      mismo reprogramara su cita (el endpoint existe en el backend,
--      pero no está conectado a ninguna pantalla) — decía "puedes
--      reprogramarla tú mismo" sin ser cierto. Se deja enfocada solo en
--      cancelar, que sí funciona.
--   2) "¿Qué es la lista de espera?" -> "¿Puedo ver la agenda de mi
--      médico de cabecera?": la página de Lista de Espera se sacó del
--      frontend del paciente (ya no hay dónde anotarse), así que esa
--      pregunta quedó engañosa. Se reemplaza por información sobre la
--      Agenda de Médico (HU-13), la sección nueva donde el asegurado
--      consulta los días/horarios de un médico específico sin reservar.
-- =====================================================================

USE miconsulta_db;

SET SQL_SAFE_UPDATES = 0;

UPDATE informacion_general
SET titulo = '¿Puedo cancelar mi cita?',
    contenido = 'Sí, puedes cancelarla tú mismo desde "Mis Citas", siempre que falten más de 24 horas para la atención. Si necesitas un nuevo horario, puedes agendar una cita nueva, o esperar a que el área administrativa te contacte si tu cita actual necesita reprogramarse.'
WHERE titulo = '¿Puedo cancelar o reprogramar mi cita?';

UPDATE informacion_general
SET titulo = '¿Puedo ver la agenda de mi médico de cabecera?',
    contenido = 'Sí. Si ya tienes un médico de tu preferencia, ingresa a la sección "Agenda de Médico" para consultar los días y horarios que tiene programados durante el mes. Es solo informativo: no reserva ni asigna ningún cupo, pero te sirve para planificar con anticipación cuándo intentar agendar tu próxima cita con él o ella.',
    categoria = 'AGENDAMIENTO',
    orden = 3
WHERE titulo = '¿Qué es la lista de espera?';

SET SQL_SAFE_UPDATES = 1;
