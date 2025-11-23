package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.valueobject.CourseScheduleSlot;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.StudentCourseRepository;
import com.application.sisacadepcc.presentation.dto.ClassroomOptionResponse;
import com.application.sisacadepcc.presentation.dto.CreateLabSectionRequest;
import com.application.sisacadepcc.presentation.dto.LabScheduleSlotDto;
import com.application.sisacadepcc.presentation.dto.LabSectionResponse;
import com.application.sisacadepcc.presentation.dto.LabSlotSuggestionResponse;
import com.application.sisacadepcc.presentation.dto.UpdateLabSectionRequest;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SecretaryLabManagementService {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("H:mm")
            .toFormatter(Locale.ROOT);

    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final ClassroomRepository classroomRepository;
    private final ExcelScheduleService excelScheduleService;

    public SecretaryLabManagementService(CourseRepository courseRepository,
                                         StudentCourseRepository studentCourseRepository,
                                         ClassroomRepository classroomRepository,
                                         ExcelScheduleService excelScheduleService) {
        this.courseRepository = courseRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.classroomRepository = classroomRepository;
        this.excelScheduleService = excelScheduleService;
    }

    public List<Course> listTheoryCourses() {
        return courseRepository.findAll().stream()
                .filter(course -> course.getCourseType() == null || CourseType.THEORY.equals(course.getCourseType()))
                .sorted(Comparator.comparing(Course::getAnio, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Course::getName))
                .toList();
    }

        public List<ClassroomOptionResponse> listClassrooms(boolean labsOnly) {
        return classroomRepository.findAll().stream()
            .filter(classroom -> !labsOnly || classroom.isLab())
            .sorted(Comparator.comparing(
                classroom -> safeName(classroom.getDisplayName()),
                String.CASE_INSENSITIVE_ORDER))
            .map(ClassroomOptionResponse::from)
            .toList();
        }

    public List<LabSectionResponse> listLabSections(Long theoryCourseId) {
        if (theoryCourseId == null) {
            return List.of();
        }
        return courseRepository.findByLabPrerequisiteCourseId(theoryCourseId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LabSectionResponse createLabSection(CreateLabSectionRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.theoryCourseId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un curso teórico");
        }
        Course theoryCourse = courseRepository.findById(request.theoryCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Curso teórico no encontrado"));

        if (!CourseType.THEORY.equals(Optional.ofNullable(theoryCourse.getCourseType()).orElse(CourseType.THEORY))) {
            throw new IllegalArgumentException("El curso seleccionado no es teórico");
        }

        List<CourseScheduleSlot> scheduleSlots = mapScheduleSlots(request.scheduleSlots());
        validateScheduleSlots(theoryCourse, scheduleSlots);
        validateClassroomAssignments(scheduleSlots);

        Course labCourse = new Course();
        labCourse.setName(resolveLabName(request.name(), theoryCourse));
        labCourse.setCreditNumber(theoryCourse.getCreditNumber());
        labCourse.setGroupLetter(resolveGroupLetter(request.groupLetter(), theoryCourse.getGroupLetter()));
        labCourse.setCourseType(CourseType.LAB);
        labCourse.setAnio(theoryCourse.getAnio());
        labCourse.setLabPrerequisiteCourseId(theoryCourse.getCourseId());
        labCourse.setLabCapacity(resolveCapacity(request.labCapacity()));
        labCourse.setScheduleSlots(scheduleSlots);

        Course saved = courseRepository.save(labCourse);
        return mapToResponse(saved);
    }

    public LabSectionResponse updateLabSection(Long labCourseId, UpdateLabSectionRequest request) {
        if (labCourseId == null) {
            throw new IllegalArgumentException("Laboratorio inválido");
        }
        Objects.requireNonNull(request, "request");

        Course existing = courseRepository.findById(labCourseId)
            .orElseThrow(() -> new IllegalArgumentException("Laboratorio no encontrado"));

        if (!CourseType.LAB.equals(existing.getCourseType())) {
            throw new IllegalStateException("Solo se pueden actualizar cursos de laboratorio");
        }

        if (request.name() != null && !request.name().isBlank()) {
            existing.setName(request.name().trim());
        }

        if (request.groupLetter() != null && !request.groupLetter().isBlank()) {
            existing.setGroupLetter(resolveGroupLetter(request.groupLetter(), existing.getGroupLetter()));
        }

        if (request.labCapacity() != null) {
            existing.setLabCapacity(resolveCapacity(request.labCapacity()));
        }

        if (request.scheduleSlots() != null && !request.scheduleSlots().isEmpty()) {
            Course theoryCourse = courseRepository.findById(existing.getLabPrerequisiteCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Curso teórico asociado no encontrado"));
            List<CourseScheduleSlot> slots = mapScheduleSlots(request.scheduleSlots());
            validateScheduleSlots(theoryCourse, slots);
            validateClassroomAssignments(slots);
            existing.setScheduleSlots(slots);
        }

        Course saved = courseRepository.save(existing);
        return mapToResponse(saved);
    }

    public void deleteLabSection(Long labCourseId) {
        if (labCourseId == null) {
            throw new IllegalArgumentException("Laboratorio inválido");
        }
        courseRepository.findById(labCourseId)
            .orElseThrow(() -> new IllegalArgumentException("Laboratorio no encontrado"));

        long enrolled = studentCourseRepository.countByCourseId(labCourseId);
        if (enrolled > 0) {
            throw new IllegalStateException("No se puede eliminar un laboratorio con estudiantes inscritos");
        }

        courseRepository.deleteById(labCourseId);
    }

    public List<LabSlotSuggestionResponse> getSlotSuggestions(Long theoryCourseId) {
        if (theoryCourseId == null) {
            return List.of();
        }

        Course theoryCourse = courseRepository.findById(theoryCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Curso teórico no encontrado"));

        return excelScheduleService.findByCourse(theoryCourse.getName(), null, CourseType.LAB).stream()
                .map(slot -> new LabSlotSuggestionResponse(
                        slot.getClassroomName(),
                        slot.getDayOfWeek(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        slot.getGroupLetter(),
                        slot.getCourseName()
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public LabSectionResponse mapToResponse(Course labCourse) {
        long enrolled = studentCourseRepository.countByCourseId(labCourse.getCourseId());
        return LabSectionResponse.from(labCourse, enrolled);
    }

    private char resolveGroupLetter(String requestedGroup, char fallback) {
        if (requestedGroup == null || requestedGroup.isBlank()) {
            return fallback == 0 ? 'A' : fallback;
        }
        return Character.toUpperCase(requestedGroup.trim().charAt(0));
    }

    private String resolveLabName(String requestedName, Course theoryCourse) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName.trim();
        }
        return theoryCourse.getName() + " - Laboratorio";
    }

    private Integer resolveCapacity(Integer requestedCapacity) {
        if (requestedCapacity == null || requestedCapacity <= 0) {
            return null;
        }
        return requestedCapacity;
    }

    private List<CourseScheduleSlot> mapScheduleSlots(List<LabScheduleSlotDto> slotDtos) {
        if (slotDtos == null || slotDtos.isEmpty()) {
            throw new IllegalArgumentException("Debes definir al menos un horario");
        }

        List<CourseScheduleSlot> slots = new ArrayList<>();
        for (LabScheduleSlotDto dto : slotDtos) {
            if (dto == null) {
                continue;
            }
            CourseScheduleSlot slot = new CourseScheduleSlot(
                    normalizeString(dto.classroomName()),
                    normalizeDay(dto.dayOfWeek()),
                    sanitizeTime(dto.startTime()),
                    sanitizeTime(dto.endTime())
            );
            if (!slot.isComplete()) {
                throw new IllegalArgumentException("Los horarios deben incluir aula, día y horas válidas");
            }
            slots.add(slot);
        }

        if (slots.isEmpty()) {
            throw new IllegalArgumentException("Debes definir al menos un horario válido");
        }

        return slots;
    }

    private void validateScheduleSlots(Course theoryCourse, List<CourseScheduleSlot> requestedSlots) {
        // Excel-based validation disabled: accept requested slots as long as they pass
        // the classroom availability checks executed later in the pipeline.
        if (requestedSlots == null || requestedSlots.isEmpty()) {
            throw new IllegalArgumentException("Debes definir al menos un horario válido");
        }
    }

    private void validateClassroomAssignments(List<CourseScheduleSlot> requestedSlots) {
        if (requestedSlots == null || requestedSlots.isEmpty()) {
            return;
        }

        Map<String, Classroom> classroomIndex = buildClassroomIndex();

        for (CourseScheduleSlot slot : requestedSlots) {
            Classroom classroom = resolveClassroom(classroomIndex, slot.getClassroomName());
            slot.setClassroomName(classroom.getDisplayName());
            if (!classroom.isLab()) {
                throw new IllegalArgumentException("El aula " + classroom.getDisplayName() + " no es un laboratorio");
            }
            if (isSlotOccupied(classroom, slot)) {
                throw new IllegalArgumentException("El aula " + classroom.getDisplayName()
                        + " ya está ocupada el " + slot.getDayOfWeek()
                        + " " + slot.getStartTime() + "-" + slot.getEndTime());
            }
        }
    }

    private Map<String, Classroom> buildClassroomIndex() {
        return classroomRepository.findAll().stream()
                .collect(Collectors.toMap(
                        classroom -> normalizeClassroomKey(classroom.getDisplayName()),
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private Classroom resolveClassroom(Map<String, Classroom> index, String classroomName) {
        String key = normalizeClassroomKey(classroomName);
        Classroom classroom = index.get(key);
        if (classroom == null) {
            throw new IllegalArgumentException("El aula " + classroomName + " no existe en la base de datos");
        }
        return classroom;
    }

    private String normalizeClassroomKey(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT)
                .replace("LABORATORIO", "LAB")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");

        if (normalized.startsWith("LAB ")) {
            String digits = normalized.substring(4).replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                int number = Integer.parseInt(digits);
                return "LAB " + String.format(Locale.ROOT, "%02d", number);
            }
        }

        if (normalized.startsWith("AULA ")) {
            String digits = normalized.substring(5).replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                return "AULA " + digits;
            }
        }

        return normalized;
    }

    private boolean isSlotOccupied(Classroom classroom, CourseScheduleSlot slot) {
        List<OccupiedSchedule> schedules = classroom.getOccupiedSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }
        return schedules.stream().anyMatch(existing -> existing.occupiesTimeSlot(
                slot.getDayOfWeek(),
                slot.getStartTime(),
                slot.getEndTime()
        ));
    }

    private String safeName(String value) {
        return value == null ? "" : value;
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Horarios inválidos");
        }
        String sanitized = raw.trim();
        if (!sanitized.contains(":")) {
            sanitized = sanitized.substring(0, Math.min(2, sanitized.length())) + ":" + sanitized.substring(Math.min(2, sanitized.length()));
        }
        return LocalTime.parse(sanitized, TIME_FORMATTER);
    }

    private String sanitizeTime(String raw) {
        LocalTime time = parseTime(raw);
        return time.toString();
    }

    private String normalizeString(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private String normalizeDay(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
