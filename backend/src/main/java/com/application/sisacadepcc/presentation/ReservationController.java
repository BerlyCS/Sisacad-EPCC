package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.presentation.dto.ClassroomScheduleResponse;
import com.application.sisacadepcc.presentation.dto.CreateReservationRequest;
import com.application.sisacadepcc.presentation.dto.ReservationAvailabilityResponse;
import com.application.sisacadepcc.presentation.dto.ReservationResponse;
import com.application.sisacadepcc.presentation.dto.UpdateReservationStatusRequest;
import com.application.sisacadepcc.presentation.dto.WeeklyAvailabilityResponse;
import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.ReservationService;
import com.application.sisacadepcc.service.UserDirectoryService;
import com.application.sisacadepcc.service.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReservationController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final ReservationService reservationService;
    private final UserDirectoryService userDirectoryService;
    private final AuthorizationService authorizationService;

    public ReservationController(ReservationService reservationService,
                                 UserDirectoryService userDirectoryService,
                                 AuthorizationService authorizationService) {
        this.reservationService = reservationService;
        this.userDirectoryService = userDirectoryService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations(Authentication authentication) {
        ensureAdministratorOrSecretary(authentication);
        List<ReservationResponse> responses = reservationService.getAllReservations().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        List<ReservationResponse> responses = reservationService.getReservationsForCurrentUser(authentication).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/classroom/{classroomName}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByClassroom(@PathVariable String classroomName,
                                                                                Authentication authentication) {
        ensureReservationActor(authentication);
        try {
            List<ReservationResponse> responses = reservationService.getReservationsByClassroomName(classroomName).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody CreateReservationRequest request,
                                                                 Authentication authentication) {
        ensureReservationActor(authentication);
        if (request == null || request.getSchedule() == null) {
            return ResponseEntity.badRequest().build();
        }
        var schedule = request.getSchedule();
        try {
            Reservation reservation = reservationService.createReservation(
                    request.getClassroomName(),
                    request.getPurpose(),
                    schedule.getDayOfWeek(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    authentication
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reservation));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateStatus(@PathVariable Long id,
                                                            @RequestBody UpdateReservationStatusRequest request,
                                                            Authentication authentication) {
        ensureAdministrator(authentication);
        if (request == null || request.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        Reservation updated = reservationService.updateReservationStatus(id, request.getStatus().toUpperCase(), authentication);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, Authentication authentication) {
        reservationService.deleteReservation(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/classrooms")
    public ResponseEntity<List<String>> getClassrooms(Authentication authentication) {
        ensureReservationActor(authentication);
        return ResponseEntity.ok(reservationService.getReservableClassrooms());
    }

    @GetMapping("/schedule/{classroomName}")
    public ResponseEntity<ClassroomScheduleResponse> getClassroomSchedule(@PathVariable String classroomName,
                                                                           Authentication authentication) {
        ensureReservationActor(authentication);
        try {
            ClassroomScheduleResponse response = new ClassroomScheduleResponse();
            response.setClassroomName(classroomName);
            reservationService.getCourseScheduleEntries(classroomName).forEach(entry -> {
                ClassroomScheduleResponse.ScheduleEntry dto = new ClassroomScheduleResponse.ScheduleEntry();
                dto.setStartTime(formatTime(entry.getStartTime()));
                dto.setEndTime(formatTime(entry.getEndTime()));
                dto.setCourseName(entry.getCourseName());
                dto.setType("FIXED");
                response.addEntry(normalizeDay(entry.getDayOfWeek()), dto);
            });
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/weekly-availability/{classroomName}")
    public ResponseEntity<WeeklyAvailabilityResponse> getWeeklyAvailability(@PathVariable String classroomName,
                                                                            Authentication authentication) {
        ensureReservationActor(authentication);
        try {
            WeeklyAvailabilityResponse response = new WeeklyAvailabilityResponse();
            response.setClassroomName(classroomName);
            response.setDays(new java.util.ArrayList<>(reservationService.getSupportedDays()));
            response.setTimeSlots(new java.util.ArrayList<>(reservationService.getDefaultTimeSlots()));
            response.setAvailability(reservationService.buildAvailabilityMatrix(classroomName));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/availability/{classroomName}")
    public ResponseEntity<ReservationAvailabilityResponse> checkAvailability(@PathVariable String classroomName,
                                                                             @RequestParam String dayOfWeek,
                                                                             @RequestParam String startTime,
                                                                             @RequestParam String endTime,
                                                                             Authentication authentication) {
                                            ensureReservationActor(authentication);
        try {
            LocalTime start = LocalTime.parse(startTime, TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endTime, TIME_FORMATTER);
            boolean available = reservationService.isTimeSlotAvailable(classroomName, dayOfWeek, start, end);
            return ResponseEntity.ok(new ReservationAvailabilityResponse(available));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setPurpose(reservation.getPurpose());
        response.setStatus(reservation.getStatus());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setReservationDate(reservation.getReservationDate());
        response.setClassroomName(reservationService.getClassroomDisplayName(reservation.getClassroomId()));
        response.setReservedBy(userDirectoryService.resolveDisplayNameOrFallback(reservation.getUserId()));

        if (reservation.getSchedule() != null) {
            ReservationResponse.ScheduleDto scheduleDto = new ReservationResponse.ScheduleDto();
            scheduleDto.setDayOfWeek(reservation.getSchedule().getDayOfWeek());
            scheduleDto.setStartTime(formatTime(reservation.getSchedule().getStartTime()));
            scheduleDto.setEndTime(formatTime(reservation.getSchedule().getEndTime()));
            response.setSchedule(scheduleDto);
        }
        return response;
    }

    private String formatTime(LocalTime time) {
        return time != null ? TIME_FORMATTER.format(time) : null;
    }

    private void ensureAdministratorOrSecretary(Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            throw new SecurityException("No autorizado");
        }
    }

    private void ensureAdministrator(Authentication authentication) {
        if (!authorizationService.isAdministrator(authentication)) {
            throw new SecurityException("No autorizado");
        }
    }

    private void ensureReservationActor(Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY, UserRole.PROFESSOR)) {
            throw new SecurityException("No autorizado");
        }
    }

    private String normalizeDay(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Void> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
