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
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
                         ClassroomRepository classroomRepository) {
        this.repository = repository;
        this.courseGroupRepository = courseGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.classroomRepository = classroomRepository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course createCourse(Course course) {
        return repository.save(course);
    }

    public List<Course> getCoursesForProfessor(Long professorId) {
        if (professorId == null) {
            return List.of();
        }

        return courseGroupRepository.findByTeacherId(professorId).stream()
                .map(CourseGroup::getCourse)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
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

    public Optional<CourseDetails> getCourseDetails(Long groupId) {
        if (groupId == null) {
            return Optional.empty();
        }

        return courseGroupRepository.findById(groupId)
                .map(this::buildCourseDetails);
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

    private CourseDetails buildCourseDetails(CourseGroup group) {
        return new CourseDetails(group, List.of(), null, null);
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

}
