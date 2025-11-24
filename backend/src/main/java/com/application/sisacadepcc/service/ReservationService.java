package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.valueobject.ScheduleType;
import com.application.sisacadepcc.domain.repository.ReservationRepository;
import com.application.sisacadepcc.domain.repository.ScheduleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final AuthorizationService authorizationService;

    public ReservationService(ReservationRepository reservationRepository,
                              ScheduleRepository scheduleRepository,
                              AuthorizationService authorizationService) {
        this.reservationRepository = reservationRepository;
        this.scheduleRepository = scheduleRepository;
        this.authorizationService = authorizationService;
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

    public Reservation createReservation(Reservation reservation, Authentication authentication) {
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

        // TODO: rework this with userId
        Long userId = getUserId(authentication);
        reservation.setUserId(userId);

        return reservationRepository.save(reservation);
    }

    public Reservation updateReservationStatus(Long id, String status, Authentication authentication) {
        // Solo administradores pueden cambiar el estado
        if (!authorizationService.isAdministrator(authentication)) {
            throw new SecurityException("Solo los administradores pueden cambiar el estado de las reservas");
        }

        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            reservation.setStatus(status);
            return reservationRepository.save(reservation);
        }
        throw new IllegalArgumentException("Reserva no encontrada");
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

    public boolean isTimeSlotAvailable(Long classroomId, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        // Verificar en las reservas existentes
        List<Reservation> reservations = getReservationsByClassroomId(classroomId);
        return reservations.stream().noneMatch(reservation -> {
            if ("REJECTED".equals(reservation.getStatus())) return false;
            String reservationDay = reservation.getReservationDate() != null ? dayOfWeekInSpanish(reservation.getReservationDate()) : (reservation.getSchedule() != null ? reservation.getSchedule().getDayOfWeek() : null);
            if (reservationDay == null || !reservationDay.equalsIgnoreCase(dayOfWeek)) return false;
            LocalTime rs = reservation.getSchedule().getStartTime();
            LocalTime re = reservation.getSchedule().getEndTime();
            return rs.isBefore(endTime) && re.isAfter(startTime);
        });
    }

    private List<Reservation> getReservationsByClassroomId(Long classroomId) {
        // Since repository doesn't have findByClassroomId, we need to filter all or add method
        // For now, return all and filter
        return reservationRepository.findAll().stream()
                .filter(r -> r.getClassroomId().equals(classroomId))
                .toList();
    }

    private boolean canMakeReservation(Authentication authentication) {
        return authorizationService.isAdministrator(authentication) ||
                authorizationService.isProfessor(authentication) ||
                authorizationService.isSecretary(authentication);
    }

    private String getUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }
        throw new SecurityException("Usuario no autenticado");
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
        return reservations.stream().anyMatch(existing -> {
            if ("REJECTED".equals(existing.getStatus())) return false;
            return reservationOverlaps(existing, requested);
        });
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
        String requestedDay;
        if (requested.getReservationDate() != null) {
            requestedDay = dayOfWeekInSpanish(requested.getReservationDate());
        } else if (requested.getSchedule() != null) {
            requestedDay = requested.getSchedule().getDayOfWeek();
        } else {
            return false;
        }

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
        String existingDay = existing.getReservationDate() != null ? dayOfWeekInSpanish(existing.getReservationDate()) : (existing.getSchedule() != null ? existing.getSchedule().getDayOfWeek() : null);
        String requestedDay = requested.getReservationDate() != null ? dayOfWeekInSpanish(requested.getReservationDate()) : (requested.getSchedule() != null ? requested.getSchedule().getDayOfWeek() : null);
        if (existingDay == null || requestedDay == null || !existingDay.equalsIgnoreCase(requestedDay)) {
            return false;
        }
        LocalTime existingStart = existing.getSchedule().getStartTime();
        LocalTime existingEnd = existing.getSchedule().getEndTime();
        LocalTime requestedStart = requested.getSchedule().getStartTime();
        LocalTime requestedEnd = requested.getSchedule().getEndTime();
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    // Método helper para crear Schedule for reservation from strings
    public Schedule createReservationSchedule(String dayOfWeek, String startTime, String endTime) {
        try {
            Schedule s = new Schedule(dayOfWeek, LocalTime.parse(startTime), LocalTime.parse(endTime));
            s.setScheduleType(ScheduleType.RESERVATION);
            return s;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido: " + e.getMessage());
        }
    }

    // Método helper para crear Schedule for a specific date (reservationDate should be set on Reservation)
    public Schedule createSpecificDateSchedule(java.time.LocalDate date, String startTime, String endTime) {
        try {
            String day = dayOfWeekInSpanish(date);
            Schedule s = new Schedule(day, LocalTime.parse(startTime), LocalTime.parse(endTime));
            s.setScheduleType(ScheduleType.RESERVATION);
            return s;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido: " + e.getMessage());
        }
    }

    private String dayOfWeekInSpanish(java.time.LocalDate date) {
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