# Requisitos por rol

## Estudiante
- Consulta únicamente sus propios datos académicos (notas, avance y asistencia). No tiene acceso a datos de otros estudiantes.
- Visualiza el temario/sílabo cargado por los profesores y el peso de cada actividad (temario) según el silabo.
- Solo puede matricularse a laboratorios; el resto del flujo (matrículas) ya está definido por la secretaría.
- Visualiza el porcentaje de avance del curso y el progreso del laboratorio cuando está matriculado.
- Recibe reportes de notas (alta, baja, media) que generan profesores y secretaría.
- Las matrículas ya existen en el sistema y se muestran solo los cursos en los que está inscrito; no se cruzan con otras áreas.
- Visualiza su promedio consolidado, estado de desempeño categorizado (ALTO, REGULAR o EN RIESGO o DESAPROBADO por cada curso) y las cuentas de cursos aprobados/pendientes.
- Gestiona la inscripción a laboratorios asociados a sus cursos, con validaciones de capacidad y mensajes de resultado (información o error) que explican por qué no puede matricularse si hay conflicto.

## Profesor
- Debe subir el silabo por curso/sección antes de poder marcar asistencia o realizar cualquier otra acción.
- Carga únicamente notas de teoría (no tareas ni actividades). El profesor de laboratorio solo visualiza notas, no las registra.
- Sube notas de exámenes y teoría; el sistema indica notas más alta, promedio y más baja.
- La asistencia se registra mientras esté en horario de clase (15 minutos de tolerancia para llegadas). Asistencia fuera de horario no se marca.
- Puede consultar horarios propios, disponibilidad de salones y estados de las clases (libre/ocupado). Puede reservar (hasta 2 reservas por semana) o cancelar clases siempre que estén libres.
- Cada reserva es automática (basada en capacidades/grupos existentes) pero el profesor puede cancelar si lo necesita.
- Genera reportes de sus cursos (notas y estadísticas) junto con la secretaría.
- Historial de rendimiento visualizable con histogramas, mapas de calor u otras representaciones.
- Antes de reservar en un horario específico puede ver la clase programada y qué salones u horarios están libres para validar la disponibilidad.
- Administra calificaciones únicamente en cursos teóricos usando un panel que alterna entre modo individual y masivo, permite filtrar por grupos asignados, bloquea grupos no habilitados para calificar y muestra estadísticas/resúmenes instantáneos antes de guardar.
- Descarga reportes de asistencia y calificaciones en Excel y genera un PDF con métricas clave desde el mismo panel para compartirlo o archivarlo.
- Controla el sílabo y sus temas por separado: sube un único PDF por curso (nombre canónico, tope 100MB), puede actualizarlo sin perder la lista de temas y administra el cronograma de temas con estados (UPCOMING, TODAY, COMPLETED, UNSCHEDULED) para mostrar progreso.

## Secretaria
- Administra grupos, salones y capacidad de laboratorios (máximo 20 personas). Define la capacidad que usa el sistema en las reservas automáticas.
- Lee listas de cursos, docentes y grupos desde el Excel maestro (sin horarios ni salones detallados; solo curso, docente y grupo).
- Visualiza el progreso de cualquier alumno y puede generar reportes en conjunto con los profesores.
- Consulta disponibilidad de salones en horarios determinados y qué salones están disponibles o ya ocupados.
- Modulo especializado para leer grupos y salones; no administra matriculas, ya que esos datos los ingresan los alumnos.
- Accede a las listas oficiales de docentes y cursos para respaldar asignaciones y coordinar con los docentes los trocos de los grupos y capacidad de laboratorios.

## Administrador
- Tiene acceso total: controla roles (profesores, secretaría, alumnos) y gestiona todo el sistema (usuarios, contraseñas y niveles de acceso).
- Gestiona las reservas de clase automáticas pero puede intervenir; controla la generación de reportes.
- Administra la configuración de plataformas, capacidades y frameworks que el sistema debe soportar.
- Ve estadísticas globales (notas alta/baja/media, asistencia, avance, utilización de salones).
- Crea, actualiza y elimina cuentas de profesores y secretarias mediante los endpoints administrativos que exponen las altas con validaciones.

## Requisitos generales del sistema
- Autenticación: todos los usuarios tienen usuario y contraseña; también se puede generar cuenta a partir del correo electrónico.
- Los usuarios reciben notificaciones que pueden consultarse y marcar como leídas desde el panel principal.
- Las reservas de clase son automáticas y deben respetar las matriculas de laboratorio (solo aparecen los laboratorios asociados a cursos inscrito y no deben cruzarse con otros cursos).
- Las reservas se consultan por aula y fecha, se pueden filtrar, permiten ver la agenda semanal y verificar disponibilidad (matriz semanales) antes de reservar.
- El sistema almacena la IP del profesor al cargar información crítica (opcional, verificar si se justifica).
- Solo existe un archivo por silabo y cada sección, curso o profesor puede tener su propio silabo.
- El sílabo solo acepta un PDF oficial (nombre normalizado, <100MB) y mantiene un listado de temas/fechas que guarda estados (UPCOMING, TODAY, COMPLETED, UNSCHEDULED) para clarificar el avance.
- Se requiere mostrar el avance semanal (qué están haciendo cada uno) y permitir ver qué temas quedan: hay 8 temas en el curso, 73 semanas en total, con 5 temas iniciales (títulos de capítulos) y la posibilidad de marcar cuando se termina un tema para calcular porcentaje de avance.
- No se manejan tareas ni classroom; se centra en exámenes y teoría.
- La secretaría y los profesores pueden subir reportes y estadísticas; la secretaría de cualquier curso y el profesor solo de sus cursos.
- El sistema debe mantener un flujo sencillo (sin procesos complicados) mientras obliga al cumplimiento de requisitos críticos (como subir el silabo).
- Solo se permite un máximo de dos reservas por semana y el profesor puede cancelar si lo necesita; las reservas automáticas respetan los límites de capacidad definidos por la secretaría.
- La logística de horarios se alimenta desde un único Excel maestro (curso, docente, grupo) sin detalle de sala u hora, pero permite consultar disponibilidad.
- Los reportes exportables incluyen Excel (asistencia y notas) y PDF con resumen textual/gráficos de barras para mostrar la distribución de asistencias.
- El registro de asistencias engloba sesiones completas con todo consignado, geolocalización del docente, hora programada y el estatus individual de cada estudiante.
- El sistema se asegura de que cada laboratorio cumpla la capacidad máxima impuesta por la secretaría antes de confirmar inscripciones o reservas.

## Preguntas abiertas
- ¿El temario se procesa automáticamente o los profesores lo cargan manualmente?
