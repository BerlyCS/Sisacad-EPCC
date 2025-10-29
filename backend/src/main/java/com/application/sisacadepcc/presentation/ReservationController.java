package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import com.application.sisacadepcc.service.ReservationService;
import com.application.sisacadepcc.service.ExcelScheduleService;
import com.application.sisacadepcc.presentation.dto.CreateReservationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;
    private final ExcelScheduleService excelScheduleService;

    public ReservationController(ReservationService reservationService,
                                 ExcelScheduleService excelScheduleService) {
        this.reservationService = reservationService;
        this.excelScheduleService = excelScheduleService;
    }

    @GetMapping
    public List<Reservation> getAllReservations(Authentication authentication) {
        return reservationService.getAllReservations();
    }

    @GetMapping("/classroom/{classroomName}")
    public List<Reservation> getReservationsByClassroom(@PathVariable String classroomName) {
        return reservationService.getReservationsByClassroom(classroomName);
    }

    @GetMapping("/my-reservations")
    public List<Reservation> getMyReservations(Authentication authentication) {
        // Obtener el email del usuario autenticado
        String userEmail = getCurrentUserEmail(authentication);
        return reservationService.getReservationsByUser(userEmail);
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest request, Authentication authentication) {
        try {
            // Crear el OccupiedSchedule desde el request
            OccupiedSchedule schedule = new OccupiedSchedule(
                    request.getSchedule().getDayOfWeek(),
                    request.getSchedule().getStartTime(),
                    request.getSchedule().getEndTime()
            );

            // Crear la reserva CON STATUS
            Reservation reservation = new Reservation();
            reservation.setClassroomName(request.getClassroomName());
            reservation.setPurpose(request.getPurpose());
            reservation.setSchedule(schedule);
            reservation.setStatus("PENDING"); // ← CORRECCIÓN: Establecer status

            Reservation created = reservationService.createReservation(reservation, authentication);
            return ResponseEntity.ok(created);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String status = request.get("status");
            Reservation updated = reservationService.updateReservationStatus(id, status, authentication);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id, Authentication authentication) {
        try {
            reservationService.deleteReservation(id, authentication);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/availability/{classroomName}")
    public Map<String, Boolean> checkAvailability(
            @PathVariable String classroomName,
            @RequestParam String dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        boolean available = reservationService.isTimeSlotAvailable(classroomName, dayOfWeek, startTime, endTime);
        return Map.of("available", available);
    }

    // En ReservationController.java - agregar este endpoint
    @GetMapping("/weekly-availability/{classroomName}")
    public Map<String, Object> getWeeklyAvailability(@PathVariable String classroomName) {
        boolean[][] availability = reservationService.getWeeklyAvailability(classroomName);

        String[] days = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"};
        String[] timeSlots = {
                "07:00-07:50", "07:50-08:40", "08:50-09:40", "09:40-10:30",
                "10:40-11:30", "11:30-12:20", "12:20-13:10", "13:10-14:00",
                "14:00-14:50", "14:50-15:40", "15:50-16:40", "16:40-17:30",
                "17:40-18:30", "18:30-19:20", "19:20-20:10"
        };

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("classroomName", classroomName);
        response.put("days", days);
        response.put("timeSlots", timeSlots);
        response.put("availability", availability);

        return response;
    }

    @GetMapping("/classrooms")
    public List<String> getAvailableClassrooms() {
        return excelScheduleService.getAvailableClassrooms();
    }

    @GetMapping("/schedule/{classroomName}")
    public Map<String, Object> getClassroomSchedule(@PathVariable String classroomName) {
        Map<String, List<ExcelScheduleService.OccupiedTimeSlot>> schedule =
                excelScheduleService.getClassroomSchedule(classroomName);

        return Map.of(
                "classroomName", classroomName,
                "schedule", schedule
        );
    }

    private String getCurrentUserEmail(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User =
                    (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
            return oauth2User.getAttribute("email");
        }
        return null;
    }
}
