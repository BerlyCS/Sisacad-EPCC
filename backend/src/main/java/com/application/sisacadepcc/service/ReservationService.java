package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import com.application.sisacadepcc.domain.repository.ReservationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ExcelScheduleService excelScheduleService;
    private final AuthorizationService authorizationService;

    public ReservationService(ReservationRepository reservationRepository,
                              ExcelScheduleService excelScheduleService,
                              AuthorizationService authorizationService) {
        this.reservationRepository = reservationRepository;
        this.excelScheduleService = excelScheduleService;
        this.authorizationService = authorizationService;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> getReservationsByClassroom(String classroomName) {
        return reservationRepository.findByClassroomName(classroomName);
    }

    public List<Reservation> getReservationsByUser(String email) {
        return reservationRepository.findByReservedBy(email);
    }

    public Reservation createReservation(Reservation reservation, Authentication authentication) {
        // Verificar que el usuario tiene permisos para reservar
        if (!canMakeReservation(authentication)) {
            throw new SecurityException("No tiene permisos para realizar reservas");
        }

        OccupiedSchedule schedule = reservation.getSchedule();

        // Verificar que el horario no esté ocupado en el Excel (horarios recurrentes)
        if (excelScheduleService.isTimeSlotOccupied(
                reservation.getClassroomName(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),  // CAMBIADO: usar getStartTime() directamente
                schedule.getEndTime())) { // CAMBIADO: usar getEndTime() directamente
            throw new IllegalArgumentException("El horario seleccionado ya está ocupado en el horario regular");
        }

        // Verificar que no haya reservas existentes en el mismo horario
        if (hasExistingReservation(reservation.getClassroomName(), schedule)) {
            throw new IllegalArgumentException("Ya existe una reserva en este horario");
        }

        // Establecer el usuario que hace la reserva
        String userEmail = getUserEmail(authentication);
        reservation.setReservedBy(userEmail);

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
            String userEmail = getUserEmail(authentication);

            // Solo el usuario que creó la reserva o un administrador puede eliminarla
            if (!reservation.getReservedBy().equals(userEmail) &&
                    !authorizationService.isAdministrator(authentication)) {
                throw new SecurityException("No tiene permisos para eliminar esta reserva");
            }

            reservationRepository.deleteById(id);
        }
    }

    public boolean isTimeSlotAvailable(String classroomName, String dayOfWeek, String startTime, String endTime) {
        // Verificar en el Excel (horarios recurrentes)
        if (excelScheduleService.isTimeSlotOccupied(classroomName, dayOfWeek, startTime, endTime)) {
            return false;
        }

        // Verificar en las reservas existentes
        List<Reservation> classroomReservations = reservationRepository.findByClassroomName(classroomName);
        return classroomReservations.stream().noneMatch(reservation -> {
            OccupiedSchedule reservationSchedule = reservation.getSchedule();
            return reservationSchedule.occupiesTimeSlot(dayOfWeek, startTime, endTime) &&
                    !"REJECTED".equals(reservation.getStatus());
        });
    }

    // Método para obtener disponibilidad semanal
    public boolean[][] getWeeklyAvailability(String classroomName) {
        String[] days = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"};
        String[] timeSlots = {
                "07:00-07:50", "07:50-08:40", "08:50-09:40", "09:40-10:30",
                "10:40-11:30", "11:30-12:20", "12:20-13:10", "13:10-14:00",
                "14:00-14:50", "14:50-15:40", "15:50-16:40", "16:40-17:30",
                "17:40-18:30", "18:30-19:20", "19:20-20:10"
        };

        boolean[][] availability = new boolean[days.length][timeSlots.length];

        for (int i = 0; i < days.length; i++) {
            for (int j = 0; j < timeSlots.length; j++) {
                String[] times = timeSlots[j].split("-");
                availability[i][j] = isTimeSlotAvailable(classroomName, days[i], times[0], times[1]);
            }
        }

        return availability;
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

    private boolean hasExistingReservation(String classroomName, OccupiedSchedule schedule) {
        List<Reservation> reservations = reservationRepository.findByClassroomName(classroomName);
        return reservations.stream().anyMatch(reservation -> {
            OccupiedSchedule reservationSchedule = reservation.getSchedule();
            return reservationSchedule.occupiesTimeSlot(
                    schedule.getDayOfWeek(),
                    schedule.getStartTime(),  // CAMBIADO: usar getStartTime() directamente
                    schedule.getEndTime()     // CAMBIADO: usar getEndTime() directamente
            ) && !"REJECTED".equals(reservation.getStatus());
        });
    }

    // Método helper para crear OccupiedSchedule desde strings
    public OccupiedSchedule createReservationSchedule(String dayOfWeek, String startTime, String endTime) {
        try {
            // Usar strings directamente sin conversión a Time
            return new OccupiedSchedule(dayOfWeek, startTime, endTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido: " + e.getMessage());
        }
    }

    // Método helper para crear OccupiedSchedule con fecha específica
    public OccupiedSchedule createSpecificDateSchedule(Date date, String startTime, String endTime) {
        try {
            // Usar strings directamente sin conversión a Time
            return new OccupiedSchedule(date, startTime, endTime, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido: " + e.getMessage());
        }
    }
}
