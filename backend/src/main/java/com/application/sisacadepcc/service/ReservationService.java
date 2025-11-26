package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.valueobject.ScheduleType;
import com.application.sisacadepcc.domain.repository.ReservationRepository;
import com.application.sisacadepcc.domain.repository.ScheduleRepository;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final List<String> DEFAULT_DAYS = List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES");
    private static final List<String> DEFAULT_TIME_SLOTS = List.of(
            "7:00-7:50", "7:50-8:40", "8:50-09:40", "09:40-10:30",
            "10:40-11:30", "11:30-12:20", "12:20-13:10", "13:10-14:00",
            "14:00-14:50", "14:50-15:40", "15:50-16:40", "16:40-17:30",
            "17:40-18:30", "18:30-19:20", "19:20-20:10"
    );
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthorizationService authorizationService;
    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ScheduleRepository scheduleRepository,
                              AuthorizationService authorizationService,
                              ClassroomRepository classroomRepository,
                              CourseRepository courseRepository) {
        this.reservationRepository = reservationRepository;
        this.scheduleRepository = scheduleRepository;
        this.authorizationService = authorizationService;
        this.classroomRepository = classroomRepository;
        this.courseRepository = courseRepository;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<String> getReservableClassrooms() {
        return classroomRepository.findAll().stream()
                .map(Classroom::getDisplayName)
                .sorted()
                .toList();
    }

    public List<Reservation> getReservationsForCurrentUser(Authentication authentication) {
        Long userId = getUserId(authentication);
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getReservationsByClassroomName(String classroomName) {
        Classroom classroom = requireClassroom(classroomName);
        return reservationRepository.findByClassroomId(classroom.getClassroomID());
    }

    public Reservation createReservation(String classroomName,
                                         String purpose,
                                         LocalDate reservationDate,
                                         String startTime,
                                         String endTime,
                                         Authentication authentication) {
        if (reservationDate == null) {
            throw new IllegalArgumentException("La fecha de la reserva es obligatoria");
        }

        Classroom classroom = requireClassroom(classroomName);
        java.time.LocalTime start = parseTime(startTime);
        java.time.LocalTime end = parseTime(endTime);

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        Schedule schedule = createReservationSchedule(reservationDate, start, end);
        Reservation reservation = new Reservation(classroom.getClassroomID(), null, purpose, schedule, reservationDate);
        return persistReservation(reservation, authentication);
    }

    public boolean isTimeSlotAvailable(String classroomName,
                                       LocalDate reservationDate,
                                       LocalTime startTime,
                                       LocalTime endTime) {
        Classroom classroom = requireClassroom(classroomName);
        return isTimeSlotAvailable(classroom.getClassroomID(), reservationDate, startTime, endTime);
    }

    public List<CourseScheduleEntry> getCourseScheduleEntries(String classroomName) {
        Classroom classroom = requireClassroom(classroomName);
        return loadCourseScheduleEntries(classroom.getClassroomID());
    }

    public List<List<Boolean>> buildAvailabilityMatrix(String classroomName) {
        Classroom classroom = requireClassroom(classroomName);
        List<CourseScheduleEntry> courseEntries = loadCourseScheduleEntries(classroom.getClassroomID());
        List<Reservation> reservations = reservationRepository.findByClassroomId(classroom.getClassroomID());
        return buildAvailabilityMatrix(courseEntries, reservations);
    }

    public List<String> getSupportedDays() {
        return List.copyOf(DEFAULT_DAYS);
    }

    public List<String> getDefaultTimeSlots() {
        return List.copyOf(DEFAULT_TIME_SLOTS);
    }

    private Reservation persistReservation(Reservation reservation, Authentication authentication) {
        // Verificar que el usuario tiene permisos para reservar
        if (!canMakeReservation(authentication)) {
            throw new SecurityException("No tiene permisos para realizar reservas");
        }

        // Verificar que no haya reservas existentes en el mismo horario
        if (hasExistingReservation(reservation.getClassroomId(), reservation)) {
            throw new IllegalArgumentException("Ya existe una reserva en este horario");
        }

        // Verificar que el aula esté disponible (no ocupada por cursos)
        if (!isClassroomAvailable(reservation.getClassroomId(), reservation)) {
            throw new IllegalArgumentException("El aula no está disponible en este horario");
        }

        if (reservation.getReservationDate() == null) {
            throw new IllegalArgumentException("Las reservas deben incluir una fecha específica");
        }

        if (reservation.getSchedule() == null) {
            throw new IllegalArgumentException("Las reservas deben incluir un rango horario");
        }

        Long userId = getUserId(authentication);
        reservation.setUserId(userId);

        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Long id, Authentication authentication) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            Long userId = getUserId(authentication);

            // Solo el usuario que creó la reserva o un administrador puede eliminarla
            if (!reservation.getUserId().equals(userId) &&
                    !authorizationService.isAdministrator(authentication)) {
                throw new SecurityException("No tiene permisos para eliminar esta reserva");
            }

            reservationRepository.deleteById(id);
        }
    }

    private boolean isTimeSlotAvailable(Long classroomId, LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        if (classroomId == null || reservationDate == null) {
            return false;
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Horario inválido para la reserva");
        }
        List<Reservation> reservations = getReservationsByClassroomId(classroomId);
        return reservations.stream().noneMatch(existing -> {
            if (existing.getReservationDate() == null || existing.getSchedule() == null) {
                return false;
            }
            if (!existing.getReservationDate().equals(reservationDate)) {
                return false;
            }
            LocalTime rs = existing.getSchedule().getStartTime();
            LocalTime re = existing.getSchedule().getEndTime();
            return rs.isBefore(endTime) && re.isAfter(startTime);
        });
    }

    private List<Reservation> getReservationsByClassroomId(Long classroomId) {
        if (classroomId == null) {
            return List.of();
        }
        return reservationRepository.findByClassroomId(classroomId);
    }

    private boolean canMakeReservation(Authentication authentication) {
        return authorizationService.isAdministrator(authentication) ||
                authorizationService.isProfessor(authentication) ||
                authorizationService.isSecretary(authentication);
    }

    private Long getUserId(Authentication authentication) {
        if (authorizationService.isStudent(authentication)) {
            return authorizationService.getAuthenticatedStudent(authentication)
                    .map(Student::getUserId)
                    .orElseThrow(() -> new SecurityException("Usuario no encontrado"));
        } else if (authorizationService.isProfessor(authentication)) {
            return authorizationService.getAuthenticatedProfessor(authentication)
                    .map(Professor::getUserId)
                    .orElseThrow(() -> new SecurityException("Usuario no encontrado"));
        } else if (authorizationService.isSecretary(authentication)) {
            return authorizationService.getAuthenticatedSecretary(authentication)
                    .map(Secretary::getUserId)
                    .orElseThrow(() -> new SecurityException("Usuario no encontrado"));
        } else if (authorizationService.isAdministrator(authentication)) {
            return authorizationService.getAuthenticatedAdministrator(authentication)
                    .map(Administrator::getUserId)
                    .orElseThrow(() -> new SecurityException("Usuario no encontrado"));
        }
        throw new SecurityException("Tipo de usuario no soportado");
    }

    private boolean hasExistingReservation(Long classroomId, Reservation requested) {
        List<Reservation> reservations = getReservationsByClassroomId(classroomId);
        return reservations.stream().anyMatch(existing -> reservationOverlaps(existing, requested));
    }

    private boolean isClassroomAvailable(Long classroomId, Reservation requested) {
        List<Schedule> existingSchedules = scheduleRepository.findByClassroomId(classroomId);
        // Only consider course-type schedules when checking availability against courses
        return existingSchedules.stream()
                .filter(s -> s.getScheduleType() == com.application.sisacadepcc.domain.model.valueobject.ScheduleType.COURSE)
                .noneMatch(existing -> overlaps(existing, requested));
    }
    private boolean overlaps(Schedule existing, Reservation requested) {
        // Determine requested day string (use reservationDate if present to compute day)
        if (requested.getReservationDate() == null || requested.getSchedule() == null) {
            return false;
        }
        String requestedDay = dayOfWeekInSpanish(requested.getReservationDate());

        String existingDay = existing.getDayOfWeek();
        if (existingDay == null || requestedDay == null || !existingDay.equalsIgnoreCase(requestedDay)) {
            return false;
        }

        // Compare times
        LocalTime existingStart = existing.getStartTime();
        LocalTime existingEnd = existing.getEndTime();
        LocalTime requestedStart = requested.getSchedule().getStartTime();
        LocalTime requestedEnd = requested.getSchedule().getEndTime();
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    private boolean reservationOverlaps(Reservation existing, Reservation requested) {
        // Determine day strings
        if (existing.getReservationDate() == null || requested.getReservationDate() == null) {
            return false;
        }
        if (!existing.getReservationDate().equals(requested.getReservationDate())) {
            return false;
        }
        if (existing.getSchedule() == null || requested.getSchedule() == null) {
            return false;
        }
        LocalTime existingStart = existing.getSchedule().getStartTime();
        LocalTime existingEnd = existing.getSchedule().getEndTime();
        LocalTime requestedStart = requested.getSchedule().getStartTime();
        LocalTime requestedEnd = requested.getSchedule().getEndTime();
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    // Método helper para crear Schedule para reservas con fecha específica
    public Schedule createReservationSchedule(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha de la reserva es obligatoria");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("El horario de la reserva es obligatorio");
        }
        Schedule schedule = new Schedule(dayOfWeekInSpanish(date), startTime, endTime);
        schedule.setScheduleType(ScheduleType.RESERVATION);
        return schedule;
    }

    public static class CourseScheduleEntry {
        private final String dayOfWeek;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String courseName;

        public CourseScheduleEntry(String dayOfWeek, LocalTime startTime, LocalTime endTime, String courseName) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.courseName = courseName;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public String getCourseName() {
            return courseName;
        }
    }

    private Classroom requireClassroom(String classroomName) {
        Classroom classroom = resolveClassroomByName(classroomName);
        if (classroom == null) {
            throw new IllegalArgumentException("Aula no encontrada: " + classroomName);
        }
        return classroom;
    }

    private Classroom resolveClassroomByName(String classroomName) {
        if (classroomName == null || classroomName.isBlank()) {
            return null;
        }
        String normalized = normalizeClassroomName(classroomName);
        return classroomRepository.findAll().stream()
            .filter(classroom -> normalized.equals(classroom.getNormalizedDisplayName()))
                .findFirst()
                .orElse(null);
    }

    private String normalizeClassroomName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    public String getClassroomDisplayName(Long classroomId) {
        if (classroomId == null) {
            return "Aula sin asignar";
        }
        return classroomRepository.findById(classroomId)
                .map(Classroom::getDisplayName)
                .orElse("Aula #" + classroomId);
    }

    private List<CourseScheduleEntry> loadCourseScheduleEntries(Long classroomId) {
        if (classroomId == null) {
            return List.of();
        }

        List<CourseScheduleEntry> entries = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            if (course.getGroups() == null) {
                continue;
            }
            for (CourseGroup group : course.getGroups()) {
                if (group.getCourseSchedules() == null) {
                    continue;
                }
                group.getCourseSchedules().stream()
                        .filter(schedule -> schedule.getClassroomId() != null && schedule.getClassroomId().equals(classroomId))
                        .map(schedule -> schedule.getSchedule())
                        .filter(java.util.Objects::nonNull)
                        .forEach(slot -> entries.add(new CourseScheduleEntry(
                                slot.getDayOfWeek(),
                                slot.getStartTime(),
                                slot.getEndTime(),
                                formatCourseLabel(course, group)
                        )));
            }
        }

        entries.sort(Comparator
                .comparing((CourseScheduleEntry entry) -> dayIndex(entry.getDayOfWeek()))
                .thenComparing(CourseScheduleEntry::getStartTime));
        return entries;
    }

    private String formatCourseLabel(Course course, CourseGroup group) {
        String name = course != null && course.getName() != null ? course.getName() : "Curso";
        if (group != null && group.getLetter() != null && !group.getLetter().isBlank()) {
            return name + " (" + group.getLetter() + ")";
        }
        return name;
    }

    private int dayIndex(String day) {
        if (day == null) {
            return Integer.MAX_VALUE;
        }
        String normalized = normalizeDay(day);
        int idx = DEFAULT_DAYS.indexOf(normalized);
        return idx >= 0 ? idx : Integer.MAX_VALUE;
    }

        private List<List<Boolean>> buildAvailabilityMatrix(List<CourseScheduleEntry> courseEntries, List<Reservation> reservations) {
        Map<String, List<CourseScheduleEntry>> courseByDay = courseEntries.stream()
                .collect(Collectors.groupingBy(entry -> normalizeDay(entry.getDayOfWeek()), LinkedHashMap::new, Collectors.toList()));
        Map<String, List<Reservation>> reservationsByDay = reservations.stream()
            .filter(reservation -> reservation.getSchedule() != null)
                .collect(Collectors.groupingBy(reservation -> normalizeDay(reservation.getSchedule().getDayOfWeek()), LinkedHashMap::new, Collectors.toList()));

        List<List<Boolean>> matrix = new ArrayList<>();
        for (String day : DEFAULT_DAYS) {
            List<CourseScheduleEntry> dayCourses = courseByDay.getOrDefault(day, List.of());
            List<Reservation> dayReservations = reservationsByDay.getOrDefault(day, List.of());
            List<Boolean> row = new ArrayList<>();

            for (String slot : DEFAULT_TIME_SLOTS) {
                String[] bounds = slot.split("-");
                if (bounds.length != 2) {
                    row.add(Boolean.TRUE);
                    continue;
                }
                LocalTime slotStart = parseTime(bounds[0]);
                LocalTime slotEnd = parseTime(bounds[1]);

                boolean occupiedByCourse = dayCourses.stream()
                        .anyMatch(entry -> overlaps(slotStart, slotEnd, entry.getStartTime(), entry.getEndTime()));

                boolean occupiedByReservation = dayReservations.stream()
                        .map(Reservation::getSchedule)
                        .filter(java.util.Objects::nonNull)
                        .anyMatch(schedule -> overlaps(slotStart, slotEnd, schedule.getStartTime(), schedule.getEndTime()));

                row.add(!(occupiedByCourse || occupiedByReservation));
            }
            matrix.add(row);
        }
        return matrix;
    }

    private String normalizeDay(String day) {
        if (day == null) {
            return "";
        }
        return day.trim().toUpperCase(Locale.ROOT);
    }

    private LocalTime parseTime(String value) {
        return LocalTime.parse(value.trim(), TIME_FORMATTER);
    }

    private boolean overlaps(LocalTime slotStart, LocalTime slotEnd, LocalTime eventStart, LocalTime eventEnd) {
        if (slotStart == null || slotEnd == null || eventStart == null || eventEnd == null) {
            return false;
        }
        return eventStart.isBefore(slotEnd) && eventEnd.isAfter(slotStart);
    }

    private String dayOfWeekInSpanish(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}