**Profesor — Requisitos Detallados**

**Resumen Ejecutivo**:
- **Objetivo**: Definir requisitos funcionales y no funcionales del Módulo Profesor, con reglas de negocio, criterios de aceptación y mapeo a la implementación existente en el repo.
- **Alcance**: Gestión de sílabos, horarios, salones, asistencia, notas, avance de temario, reportes y comunicación sólo para profesores sobre sus cursos.

**Actores**:
- **Profesor**: usuario con rol `PROFESSOR` que gestiona sus cursos.
- **Administrador / Secretaría**: roles con permisos más amplios (fuera del alcance funcional primario, pero deben existir para excepciones).

**1. Autenticación y Auditoría**
- **Requisito**: Acceso mediante email institucional.   
- **Registro de IP y auditoría**: Registrar IP, usuario, timestamp y acción para operaciones sensibles (subir/sustituir sílabo, subir notas, registrar asistencia). **Criterio**: cada operación crítica genera un registro de auditoría.
- **Mapeo**: revisar `backend` middleware de seguridad y filtros de Spring Security; auditoría puede integrarse en servicios donde ocurre la acción (ej. `SyllabusService`, `AttendanceService`).

**2. Gestión del Sílabo (Obligatorio para operar)**
- **Reglas clave**:
  - Un solo archivo de sílabo por curso/ sección (el sistema almacena `syllabusId` en `CourseEntity` — campo `syllabus_id`).
  - Si no hay sílabo subido, el profesor no puede: registrar asistencia ni subir notas para ese curso/ sección.
  - Límite: debe subirse dentro de la primera semana (regla de negocio configurable por calendario/periodo).
- **Funciones**:
  - Subir sílabo (PDF) y metadatos: `POST /api/syllabus` (ya expuesto en `SyllabusController`).
  - Sustituir sílabo: mismo endpoint o `PUT`/`POST` con overwrite (el repositorio ya incluye `uploadSyllabus` y `updateTopics`).
  - Ver estado del sílabo: `GET /api/syllabus?courseId=...` (controlador `SyllabusController#getSyllabusByCourse`).
  - Descargar sílabo: `GET /api/syllabus/{syllabusId}/download` (ya existente).
  - El sílabo puede ser global (curso) o por sección — definir durante el upload (campo `courseId` vs `courseGroupId` si aplica).
- **Validaciones y reglas técnicas**:
  - Solo `application/pdf` permitido; tamaño máximo configurable (ej. 10 MB).
  - Generar versión/metadata: quien sube, fecha, tamaño, checksum.
  - Control de acceso: `syllabusService.assertReadAccess(authentication)` (ya está presente en controller).
  - Manejar excepciones específicas: `SyllabusStorageException`, `SyllabusAccessDeniedException`, `InvalidSyllabusException`.
- **Mapeo a código**:
  - Backend: `backend/src/main/java/.../presentation/SyllabusController.java`, `SyllabusService` (implementación), `CourseEntity` (`syllabusId`).
  - Frontend: `frontend/src/views/ProfessorSyllabusView.vue`, `frontend/src/services/syllabusService.ts`, `frontend/src/router/index.ts` (ruta `/professor/syllabus`).

**3. Horarios y Salones**
- **Funciones**:
  - Ver horario semanal y salones asignados (solo lectura para profesor).
  - Consultar disponibilidad de salones; ver salones libres en una hora específica.
  - Ver reservas automáticas creadas por el sistema.
  - Cancelar una reserva si las reglas del curso lo permiten (p. ej. dentro de ventana X horas antes).
  - Límite: máximo 2 reservas automáticas por semana por profesor (con opción a cancelar una).
- **Criterios**:
  - Interfaz muestra bloqueos cuando el profesor excede el límite.
  - Cancelación validada en backend contra reglas de negocio.
- **Mapeo**:
  - Frontend: `frontend/src/services/classroomService.ts`, vistas relacionadas a salones/horarios (`ClassroomScheduleView.vue`, `ClassroomManagementView.vue`).
  - Backend: revisar controladores de reserva/salas (buscar `Reservation` o `classroom` en backend si hace falta).

**4. Asistencia**
- **Requisitos**:
  - Registrar asistencia del día para su clase; el sistema valida que el profesor esté en el horario correspondiente.
  - Tolerancia de 15 minutos para registrar la asistencia del profesor (configurable).
  - No puede marcar asistencia si no subió el sílabo (enforce en backend `AttendanceService` antes de crear sesión).
  - Historial de asistencia visible por curso.
  - Estado alumno: sólo `PRESENTE` / `AUSENTE` (no hay tardanza para alumnos).
- **Endpoints y UI**:
  - Frontend: `frontend/src/views/ProfessorAttendanceView.vue`, `frontend/src/services/attendanceService.ts`, `frontend/src/composables/useProfessorAttendanceWorkspace.ts`.
  - Backend: implementar/usar `AttendanceService`, endpoints para `markProfessorAttendance`, `createSession`, `getHistory`.
- **Auditoría**: registrar IP, timestamp y quién marcó la asistencia.

**5. Notas**
- **Profesor de Teoría**:
  - Puede subir notas (examen, teoría) conforme al modelo definido (el sistema puede usar un único archivo por evaluación si así está configurado).
  - Ver estadísticas: nota más alta, más baja, promedio y lista de estudiantes faltantes en cada evaluación.
- **Profesor de Laboratorio**:
  - Solo puede visualizar notas (no modificarlas).
- **Reglas**:
  - Validar rol y `canGrade` por grupo antes de permitir edición (`gradeService` ya contempla `canGrade`).
  - Control de estado de la carga: `DRAFT` vs `SUBMITTED`.
- **Mapeo**:
  - Frontend: `frontend/src/views/ProfessorGradesView.vue`, `ProfessorExamUploadsView.vue`, `frontend/src/services/gradeService.ts`, `frontend/src/stores/grades.ts`.
  - Backend: endpoints de `GradeController`/servicio (buscar en `backend` si existe). Revisar `docs/professor-grading-interface.md` para detalles.

**6. Temario y Avance**
- **Requisitos**:
  - Ver listado de temas (hasta 8 temas por curso) con semanas asignadas (~73 semanas totales, configurable por calendario académico).
  - Actualizar avance por tema si el sistema lo permite; alternativa: avance calculado automáticamente por calendario/fechas de sesiones.
  - Mostrar avance porcentual del curso.
- **Mapeo**:
  - Backend: `Syllabus` model y DTO `SyllabusResponse.TopicScheduleStatus` (ver `CourseDetailsResponse.java` y `SyllabusResponse` en backend).
  - Frontend: `frontend/src/services/courseService.ts`, `ProfessorSyllabusView.vue`.

**7. Tareas Puntuales**
- **Nota**: no es un Classroom; el sistema no debe confundirse con una plataforma de tareas individuales salvo si se decide implementar tareas puntuales en el futuro (especificar alcance si se requiere).

**8. Reportes**
- **Requisitos**:
  - El profesor puede generar reportes de sus cursos: estadísticas de notas (alta, baja, media), histogramas, gráficos circulares, mapa de calor, reporte de asistencia, avance del sílabo.
  - Exportar a PDF/Excel si el sistema lo permite.
- **Mapeo**:
  - Frontend: `frontend/src/views/ReportsView.vue`, `frontend/src/services/reportService.ts`.
  - Backend: endpoints que agregan estadísticas por curso/periodo (revisar `reportService` en backend si existe).

**9. Comunicación / Notificaciones**
- **Requisitos**:
  - Notificaciones internas: recordatorios para subir sílabos, cambios en horario, reservas automáticas creadas, fecha de revisión de grupos.
  - Envío de avisos generales al curso (habilitable por configuración).
- **Implementación**:
  - Integrar con componente de notificaciones del sistema; registrar notificaciones en backend para historial.

**10. Restricciones y Seguridad**
- **Acceso limitado**: el profesor no puede ver información personal de alumnos que no corresponda, ni cursos que no sean suyos.
- **Acciones prohibidas**: manipular matrículas, reservar salones fuera de las reglas, subir actividades fuera del flujo.
- **Validaciones**: verificar autoría en cada endpoint (ej. `syllabusService.assertReadAccess(authentication)`).

**Requisitos No Funcionales (recomendados)**
- **Disponibilidad**: 99.9% para funciones de consulta; ventanas de mantenimiento notificadas.
- **Performance**: tiempos de respuesta < 500 ms para peticiones comunes (lista de cursos, syllabus), < 2s para generación de reportes complejos.
- **Seguridad**: usar HTTPS, roles y scopes, logging de auditoría, protección CSRF, limitación de tamaño y tipo de archivos.
- **Accesibilidad**: componentes UI cumpliendo WCAG 2.1 AA.
- **Internacionalización**: UI y mensajes traducibles (soporte `es`/`en`).
- **Pruebas**: unitarias para `SyllabusService`, `AttendanceService`, e2e para flujos críticos (subir sílabo, registrar asistencia, subir notas).

**Criterios de Aceptación (ejemplos concretos)**
- Subir Sílabo:
  - Dado un profesor con rol `PROFESSOR`, cuando sube un PDF válido para su curso dentro de la primera semana, entonces el sistema lo acepta y actualiza `course.syllabusId`.
  - Dado que no hay sílabo, intentar registrar asistencia retorna 400 con mensaje explicativo.
- Registrar Asistencia Profesor:
  - Dado que el profesor está autenticado y dentro de la ventana de 15 minutos, puede marcar su asistencia; fuera de la ventana recibe error.
- Notas:
  - Profesor de teoría puede subir notas si `canGrade=true`; profesor de laboratorio ve pero no edita.

**Mapeo de Endpoints y Archivos Clave**
- Backend relevantes:
  - `backend/src/main/java/.../presentation/SyllabusController.java` — endpoints para upload, updateTopics, delete, download.
  - DTOs: `CourseDetailsResponse.java`, `SyllabusResponse` (topic status logic).
  - Entidad: `backend/src/main/java/.../infrastructure/repository/jpa/CourseEntity.java` (campo `syllabusId`).
  - Excepciones: `SyllabusStorageException`, `SyllabusNotFoundException`, `SyllabusAccessDeniedException`.
- Frontend relevantes:
  - `frontend/src/views/ProfessorSyllabusView.vue` — UI para gestión de sílabos.
  - `frontend/src/views/ProfessorAttendanceView.vue` — registro de asistencia, validación de 15 minutos.
  - `frontend/src/views/ProfessorGradesView.vue`, `ProfessorExamUploadsView.vue` — gestión de notas/estadísticas.
  - Servicios: `frontend/src/services/syllabusService.ts`, `frontend/src/services/attendanceService.ts`, `frontend/src/services/gradeService.ts`, `frontend/src/services/professorService.ts`, `frontend/src/services/professorScheduleService.ts`.

**Recomendaciones de Implementación / Gap Analysis**
- Validar que la regla «subir sílabo en la primera semana» esté implementada en `SyllabusService`; si no, añadir comprobación contra fechas del periodo/curso.
- Asegurar que backend impida acciones si falta sílabo (ej. `AttendanceService#createSession` y `GradeService#submitGrade` deben chequear `course.getSyllabusId()`).
- Añadir campos de auditoría (actor, acción, IP, userAgent, timestamp) en los servicios críticos.
- Implementar pruebas unitarias y un `e2e` que cubra: subir sílabo → marcar asistencia → subir notas.

**Sugerencias Adicionales (no solicitadas explícitamente pero recomendadas)**
- Versionado del sílabo: mantener historial y permitir rollback.
- Workflow de revisión: permitir enviado para revisión por Secretaría antes de habilitarlo para registrar notas/asistencia.
- Notificaciones programadas: recordatorios automatizados (cron) para profesores que no subieron sílabo en la semana 1.
- Dashboard de salud/uso: métricas de uploads, número de incidencias por curso.

**Próximos pasos sugeridos**
- Revisar `SyllabusService` y `AttendanceService` en backend e implementar validaciones faltantes.
- Añadir pruebas automáticas para los flujos críticos.
- Si quieres, genero un diagrama de casos de uso UML para el Profesor o redacto los requisitos para Secretaría/Administrador.

---
Documento generado automáticamente a partir del estado actual del repositorio. Archivos referenciados para revisión: `frontend/src/views/ProfessorSyllabusView.vue`, `frontend/src/views/ProfessorAttendanceView.vue`, `frontend/src/views/ProfessorGradesView.vue`, `frontend/src/services/syllabusService.ts`, `backend/src/main/java/.../presentation/SyllabusController.java`, `backend/src/main/java/.../infrastructure/repository/jpa/CourseEntity.java`.
