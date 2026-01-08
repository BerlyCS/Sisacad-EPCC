package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.CourseSchedule;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.model.valueobject.ScheduleType;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import com.application.sisacadepcc.domain.repository.EnrollmentRepository;
import com.application.sisacadepcc.presentation.dto.CourseScheduleSlotRequest;
import com.application.sisacadepcc.presentation.dto.CreateCourseGroupRequest;
import com.application.sisacadepcc.presentation.dto.ProfessorScheduleEntry;
import com.application.sisacadepcc.service.dto.CourseDetails;
import com.application.sisacadepcc.service.dto.CourseImportResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository repository;
    private final com.application.sisacadepcc.domain.repository.CourseGroupRepository courseGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final SyllabusService syllabusService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, Integer> DAY_ORDER = Map.of(
            "LUNES", 1,
            "MARTES", 2,
            "MIERCOLES", 3,
            "MIÉRCOLES", 3,
            "JUEVES", 4,
            "VIERNES", 5,
            "SABADO", 6,
            "SÁBADO", 6,
            "DOMINGO", 7
    );

    public CourseService(CourseRepository repository,
                         com.application.sisacadepcc.domain.repository.CourseGroupRepository courseGroupRepository,
                         EnrollmentRepository enrollmentRepository,
                         ClassroomRepository classroomRepository,
                         SyllabusService syllabusService) {
        this.repository = repository;
        this.courseGroupRepository = courseGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.classroomRepository = classroomRepository;
        this.syllabusService = syllabusService;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Map<Long, Long> getEnrollmentCountsByCourse() {
        Map<Long, Long> totals = new HashMap<>();
        for (CourseGroup group : courseGroupRepository.findAll()) {
            if (group == null) {
                continue;
            }
            Long courseId = group.getCourseId();
            Long groupId = group.getId();
            if (courseId == null || groupId == null) {
                continue;
            }
            long count = enrollmentRepository.countByCourseGroupId(groupId);
            totals.merge(courseId, count, Long::sum);
        }
        return totals;
    }

    public Course createCourse(Course course) {
        validateCourseWeights(course);
        return repository.save(course);
    }

    public CourseImportResult importCourses(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un archivo CSV o Excel con cursos");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        List<CourseImportRow> rawRows;
        try {
            InputStream inputStream = file.getInputStream();
            if (filename.endsWith(".csv")) {
                rawRows = parseCsv(inputStream);
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rawRows = parseExcel(inputStream);
            } else {
                throw new IllegalArgumentException("Formato no soportado. Use CSV o Excel (.xlsx)");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo", ex);
        }

        List<Course> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (CourseImportRow row : rawRows) {
            if (row == null || row.columns().isEmpty()) {
                continue;
            }

            if (row.rowNumber() == 1 && isHeaderRow(row.columns())) {
                continue;
            }

            processed++;
            Course course = toCourse(row, errors);
            if (course == null) {
                skipped++;
                continue;
            }

            Integer code = course.getCourseCode();
            if (code != null && repository.findByCourseCode(code.longValue()).isPresent()) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": el código de curso " + code + " ya existe, se omitió.");
                continue;
            }

            try {
                validateCourseWeights(course);
                created.add(repository.save(course));
            } catch (IllegalArgumentException ex) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": " + ex.getMessage());
            }
        }

        return new CourseImportResult(created, errors, processed, skipped);
    }

    public List<Course> getCoursesForProfessor(Long professorId) {
        if (professorId == null) {
            return List.of();
        }

        // Collect courses from groups but ensure uniqueness by courseId
        return courseGroupRepository.findByTeacherId(professorId).stream()
            .map(CourseGroup::getCourse)
            .filter(Objects::nonNull)
            .filter(course -> course.getCourseId() != null)
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(course -> course.getCourseId(), course -> course, (a, b) -> a, java.util.LinkedHashMap::new),
                map -> map.values().stream().toList()
            ));
    }

    public List<ProfessorScheduleEntry> getScheduleForProfessor(Long professorId) {
        if (professorId == null) {
            return List.of();
        }

        List<CourseGroup> groups = courseGroupRepository.findByTeacherId(professorId);
        if (groups.isEmpty()) {
            return List.of();
        }

        List<ProfessorScheduleEntry> entries = new ArrayList<>();
        for (CourseGroup group : groups) {
            if (group == null || group.getCourseSchedules() == null) {
                continue;
            }

            Course course = resolveCourse(group);
            for (CourseSchedule courseSchedule : group.getCourseSchedules()) {
                if (courseSchedule == null || courseSchedule.getSchedule() == null) {
                    continue;
                }

                Schedule schedule = courseSchedule.getSchedule();
                entries.add(new ProfessorScheduleEntry(
                        resolveCourseId(course, courseSchedule),
                        resolveCourseCode(course),
                        buildCourseDisplayName(course, group),
                        group.getLetter(),
                        group.getType(),
                        normalizeDay(schedule.getDayOfWeek()),
                        formatTime(schedule.getStartTime()),
                        formatTime(schedule.getEndTime()),
                        resolveClassroomName(courseSchedule.getClassroomId())
                ));
            }
        }

        entries.sort(Comparator
                .comparing((ProfessorScheduleEntry entry) -> DAY_ORDER.getOrDefault(entry.getDayOfWeek(), Integer.MAX_VALUE))
                .thenComparing(ProfessorScheduleEntry::getStartTime, Comparator.nullsLast(String::compareTo))
                .thenComparing(ProfessorScheduleEntry::getCourseName));

        return entries;
    }

    public Optional<CourseDetails> getCourseDetails(Long courseId) {
        if (courseId == null) {
            return Optional.empty();
        }

        Optional<Course> courseOptional = repository.findById(courseId);
        if (courseOptional.isEmpty()) {
            return Optional.empty();
        }

        Course course = courseOptional.get();
        CourseGroup representativeGroup = resolveRepresentativeGroup(courseId, course)
                .orElseGet(() -> createPlaceholderGroup(course));
        return Optional.of(buildCourseDetails(representativeGroup));
    }

    public List<CourseGroup> getCourseGroups(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return courseGroupRepository.findByCourseId(courseId);
    }

    public Optional<CourseGroup> createCourseGroup(Long courseId, CreateCourseGroupRequest request) {
        if (courseId == null || request == null) {
            return Optional.empty();
        }

        Course course = repository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("El curso especificado no existe"));

        String normalizedLetter = normalizeLetter(request.letter());
        if (normalizedLetter == null || normalizedLetter.isBlank()) {
            throw new IllegalArgumentException("La sección debe tener una letra identificadora");
        }

        int capacity = Optional.ofNullable(request.capacity())
                .filter(value -> value > 0)
                .orElseThrow(() -> new IllegalArgumentException("La capacidad debe ser mayor a cero"));

        CourseType type = resolveCourseType(request.type());

        CourseGroup group = new CourseGroup();
        group.setCourse(course);
        group.setCourseId(courseId);
        group.setLetter(normalizedLetter);
        group.setType(type);
        group.setMaxCapacity(capacity);
        group.setAvailableCapacity(capacity);
        group.setCourseSchedules(buildScheduleAssignments(request.scheduleSlots(), courseId));

        CourseGroup saved = courseGroupRepository.save(group);
        return Optional.ofNullable(saved);
    }

    public List<CourseGroup> getLabGroupsForCourse(Long courseId) {
        return getCourseGroups(courseId).stream()
                .filter(group -> group.getType() == CourseType.LAB)
                .collect(Collectors.toList());
    }

    public Optional<CourseGroup> updateGroupCapacity(Long groupId, Integer capacity) {
        if (groupId == null || capacity == null || capacity < 0) {
            return Optional.empty();
        }
        return courseGroupRepository.findById(groupId)
                .map(group -> {
                    group.setMaxCapacity(capacity);
                    int enrolled = (int) enrollmentRepository.countByCourseGroupId(group.getId());
                    group.setAvailableCapacity(Math.max(0, capacity - enrolled));
                    return courseGroupRepository.save(group);
                });
    }

    /**
     * Return all distinct teacher IDs assigned to course groups.
     * Useful for administrative UIs and quick validation.
     */
    public java.util.List<Long> getAllGroupTeacherIds() {
        return courseGroupRepository.findAll().stream()
            .map(CourseGroup::getTeacherId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    public Optional<CourseGroup> assignProfessorToGroup(Long courseId, Long groupId, Long professorId) {
        if (courseId == null || groupId == null || professorId == null) {
            return Optional.empty();
        }

        return courseGroupRepository.findById(groupId)
                .filter(group -> Objects.equals(group.getCourseId(), courseId))
                .map(group -> {
                    group.setTeacherId(professorId);
                    return courseGroupRepository.save(group);
                });
    }

    public Optional<CourseGroup> removeProfessorFromGroup(Long courseId, Long groupId, Long professorId) {
        if (courseId == null || groupId == null || professorId == null) {
            return Optional.empty();
        }

        return courseGroupRepository.findById(groupId)
                .filter(group -> Objects.equals(group.getCourseId(), courseId))
                .filter(group -> Objects.equals(group.getTeacherId(), professorId))
                .map(group -> {
                    group.setTeacherId(null);
                    return courseGroupRepository.save(group);
                });
    }

    private Course toCourse(CourseImportRow row, List<String> errors) {
        List<String> columns = row.columns();
        int line = row.rowNumber();

        Integer courseCode = parseInteger(valueAt(columns, 0), "código", line, true, errors);
        String name = valueAt(columns, 1);
        if (name == null || name.isBlank()) {
            errors.add("Fila " + line + ": el nombre del curso es obligatorio");
            return null;
        }

        Integer credits = parseInteger(valueAt(columns, 2), "créditos", line, true, errors);
        Integer semester = parseInteger(valueAt(columns, 3), "semestre", line, true, errors);
        Integer theoryHours = parseInteger(valueAt(columns, 4), "horas teoría", line, false, errors);
        Integer practiceHours = parseInteger(valueAt(columns, 5), "horas práctica", line, false, errors);
        Integer labHours = parseInteger(valueAt(columns, 6), "horas laboratorio", line, false, errors);

        Integer continuous1 = parseInteger(valueAt(columns, 7), "peso continuo 1", line, true, errors);
        Integer continuous2 = parseInteger(valueAt(columns, 8), "peso continuo 2", line, true, errors);
        Integer continuous3 = parseInteger(valueAt(columns, 9), "peso continuo 3", line, true, errors);
        Integer exam1 = parseInteger(valueAt(columns, 10), "peso examen 1", line, true, errors);
        Integer exam2 = parseInteger(valueAt(columns, 11), "peso examen 2", line, true, errors);
        Integer exam3 = parseInteger(valueAt(columns, 12), "peso examen 3", line, true, errors);

        if (courseCode == null || credits == null || semester == null
                || continuous1 == null || continuous2 == null || continuous3 == null
                || exam1 == null || exam2 == null || exam3 == null) {
            return null;
        }

        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setName(name.trim());
        course.setCredits(credits);
        course.setSemesterNumber(semester);
        course.setTheoryHours(valueOrZero(theoryHours));
        course.setPracticeHours(valueOrZero(practiceHours));
        course.setLabHours(valueOrZero(labHours));
        course.setContinuousGradeWeights(List.of(continuous1, continuous2, continuous3));
        course.setExamGradeWeights(List.of(exam1, exam2, exam3));
        return course;
    }

    private List<CourseImportRow> parseCsv(InputStream inputStream) throws IOException {
        List<CourseImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> columns = Arrays.stream(line.split(","))
                        .map(String::trim)
                        .toList();
                rows.add(new CourseImportRow(lineNumber, columns));
            }
        }
        return rows;
    }

    private List<CourseImportRow> parseExcel(InputStream inputStream) throws IOException {
        List<CourseImportRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }

            for (Row row : sheet) {
                if (row == null) {
                    continue;
                }
                int lastCell = Math.max(row.getLastCellNum(), 0);
                List<String> columns = new ArrayList<>();
                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    columns.add(readCell(cell));
                }
                rows.add(new CourseImportRow(row.getRowNum() + 1, columns));
            }
        }
        return rows;
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.rint(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return Boolean.toString(cell.getBooleanCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getRichStringCellValue().getString().trim();
        }
        return "";
    }

    private boolean isHeaderRow(List<String> columns) {
        String first = valueAt(columns, 0);
        if (first == null) {
            return false;
        }
        String normalized = first.toLowerCase(Locale.ROOT);
        return normalized.contains("course") || normalized.contains("código") || normalized.contains("codigo") || normalized.contains("code");
    }

    private String valueAt(List<String> columns, int index) {
        if (columns == null || index < 0 || index >= columns.size()) {
            return null;
        }
        String value = columns.get(index);
        return value != null ? value.trim() : null;
    }

    private Integer parseInteger(String raw, String fieldName, int rowNumber, boolean required, List<String> errors) {
        if (raw == null || raw.isBlank()) {
            if (required) {
                errors.add("Fila " + rowNumber + ": falta el campo " + fieldName);
            }
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            errors.add("Fila " + rowNumber + ": valor inválido en " + fieldName + " ('" + raw + "')");
            return null;
        }
    }

    private int valueOrZero(Integer number) {
        return number != null ? number : 0;
    }

    private record CourseImportRow(int rowNumber, List<String> columns) {
    }

    private CourseDetails buildCourseDetails(CourseGroup group) {
        if (group == null || group.getCourse() == null) {
            return new CourseDetails(group, List.of(), null, null);
        }

        Long courseId = group.getCourse().getCourseId();
        com.application.sisacadepcc.domain.model.Syllabus syllabus = null;
        if (courseId != null) {
            syllabus = syllabusService.getByCourseId(courseId).orElse(null);
        }

        return new CourseDetails(group, List.of(), null, syllabus);
    }

    private Optional<CourseGroup> resolveRepresentativeGroup(Long courseId, Course course) {
        if (courseId == null) {
            return Optional.empty();
        }
        for (CourseGroup group : courseGroupRepository.findByCourseId(courseId)) {
            if (group == null) {
                continue;
            }
            if (group.getCourse() == null) {
                group.setCourse(course);
            }
            return Optional.of(group);
        }
        return Optional.empty();
    }

    private CourseGroup createPlaceholderGroup(Course course) {
        CourseGroup placeholder = new CourseGroup();
        placeholder.setCourse(course);
        placeholder.setCourseId(course.getCourseId());
        placeholder.setLetter("");
        placeholder.setType(CourseType.THEORY);
        return placeholder;
    }

    private List<CourseSchedule> buildScheduleAssignments(List<CourseScheduleSlotRequest> slots, Long courseId) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }

        List<CourseSchedule> assignments = new ArrayList<>();
        int sequence = 0;
        for (CourseScheduleSlotRequest slot : slots) {
            if (slot == null) {
                continue;
            }
            assignments.add(toCourseSchedule(slot, courseId, sequence++));
        }
        return assignments;
    }

    private CourseSchedule toCourseSchedule(CourseScheduleSlotRequest slot, Long courseId, int sequence) {
        String day = normalizeDay(slot.dayOfWeek());
        if (day == null || day.isBlank()) {
            throw new IllegalArgumentException("Cada horario debe especificar un día");
        }

        LocalTime start = parseTime(slot.startTime(), "hora de inicio");
        LocalTime end = parseTime(slot.endTime(), "hora de fin");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("El horario debe tener una duración positiva");
        }

        if (slot.classroomId() != null) {
            classroomRepository.findById(slot.classroomId())
                    .orElseThrow(() -> new IllegalArgumentException("El aula especificada no existe"));
        }

        CourseSchedule assignment = new CourseSchedule();
        assignment.setCourseId(courseId);
        assignment.setSequenceOrder(sequence);
        assignment.setClassroomId(slot.classroomId());

        Schedule schedule = new Schedule();
        schedule.setDayOfWeek(day);
        schedule.setStartTime(start);
        schedule.setEndTime(end);
        schedule.setScheduleType(ScheduleType.COURSE);
        assignment.setSchedule(schedule);
        return assignment;
    }

    private Course resolveCourse(CourseGroup group) {
        if (group == null) {
            return null;
        }
        Course course = group.getCourse();
        if (course != null) {
            return course;
        }
        Long courseId = group.getCourseId();
        if (courseId == null) {
            return null;
        }
        return repository.findById(courseId).orElse(null);
    }

    private Long resolveCourseId(Course course, CourseSchedule courseSchedule) {
        if (course != null && course.getCourseId() != null) {
            return course.getCourseId();
        }
        return courseSchedule != null ? courseSchedule.getCourseId() : null;
    }

    private Long resolveCourseCode(Course course) {
        if (course == null || course.getCourseCode() == null) {
            return null;
        }
        return course.getCourseCode().longValue();
    }

    private String buildCourseDisplayName(Course course, CourseGroup group) {
        String baseName = course != null && course.getName() != null ? course.getName() : "Curso";
        String letter = group != null ? group.getLetter() : null;
        if (letter != null && !letter.isBlank()) {
            return baseName + " (" + letter + ")";
        }
        return baseName;
    }

    private String resolveClassroomName(Long classroomId) {
        if (classroomId == null) {
            return "Aula sin asignar";
        }
        return classroomRepository.findById(classroomId)
                .map(Classroom::getDisplayName)
                .orElse("Aula " + classroomId);
    }

    private String formatTime(LocalTime time) {
        return time != null ? TIME_FORMATTER.format(time) : null;
    }

    private String normalizeLetter(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDay(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private LocalTime parseTime(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Debe especificarse la " + fieldLabel);
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMATTER);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Formato inválido para " + fieldLabel + ": " + value);
        }
    }

    private CourseType resolveCourseType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return CourseType.THEORY;
        }
        try {
            return CourseType.fromValue(rawType.trim());
        } catch (IllegalArgumentException ex) {
            return CourseType.THEORY;
        }
    }

    private void validateCourseWeights(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo");
        }

        List<Integer> continuousWeights = course.getContinuousGradeWeights();
        if (continuousWeights == null || continuousWeights.size() != 3) {
            throw new IllegalArgumentException("Las evaluaciones continuas deben tener exactamente 3 porcentajes");
        }
        int continuousSum = continuousWeights.stream().mapToInt(Integer::intValue).sum();

        List<Integer> examWeights = course.getExamGradeWeights();
        if (examWeights == null || examWeights.size() != 3) {
            throw new IllegalArgumentException("Los exámenes deben tener exactamente 3 porcentajes");
        }
        int examSum = examWeights.stream().mapToInt(Integer::intValue).sum();

        int totalSum = continuousSum + examSum;
        if (totalSum != 100) {
            throw new IllegalArgumentException("Los porcentajes de las evaluaciones continuas y exámenes deben sumar 100% en total. Actualmente suman " + totalSum + "%.");
        }
    }

}
