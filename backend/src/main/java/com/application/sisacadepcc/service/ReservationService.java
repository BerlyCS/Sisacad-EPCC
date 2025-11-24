package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import com.application.sisacadepcc.domain.repository.ReservationRepository;
import com.application.sisacadepcc.domain.repository.ScheduleRepository;
import com.application.sisacadepcc.infrastructure.repository.jpa.ScheduleEntity;
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

    public List<Reservation> getReservationsByUser(String email) {
        return reservationRepository.findByReservedBy(email);
    }

    public Reservation createReservation(Reservation reservation, Authentication authentication) {
        // Verificar que el usuario tiene permisos para reservar
        if (!canMakeReservation(authentication)) {
            throw new SecurityException("No tiene permisos para realizar reservas");
        }

        OccupiedSchedule schedule = reservation.getSchedule();

        // Verificar que no haya reservas existentes en el mismo horario
        if (hasExistingReservation(reservation.getClassroomId(), schedule)) {
            throw new IllegalArgumentException("Ya existe una reserva en este horario");
        }

        // Verificar que el aula esté disponible (no ocupada por cursos)
        if (!isClassroomAvailable(reservation.getClassroomId(), schedule)) {
            throw new IllegalArgumentException("El aula no está disponible en este horario");
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

    public boolean isTimeSlotAvailable(Long classroomId, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        // Verificar en las reservas existentes
        List<Reservation> reservations = getReservationsByClassroomId(classroomId);
        return reservations.stream().noneMatch(reservation -> {
            OccupiedSchedule reservationSchedule = reservation.getSchedule();
            return reservationSchedule.occupiesTimeSlot(dayOfWeek, startTime.toString(), endTime.toString()) &&
                    !"REJECTED".equals(reservation.getStatus());
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

    private boolean hasExistingReservation(Long classroomId, OccupiedSchedule schedule) {
        List<Reservation> reservations = getReservationsByClassroomId(classroomId);
        return reservations.stream().anyMatch(reservation -> {
            OccupiedSchedule reservationSchedule = reservation.getSchedule();
            return reservationSchedule.occupiesTimeSlot(
                    schedule.getDayOfWeek(),
                    schedule.getStartTime().toString(),
                    schedule.getEndTime().toString()
            ) && !"REJECTED".equals(reservation.getStatus());
        });
    }

    private boolean isClassroomAvailable(Long classroomId, OccupiedSchedule schedule) {
        List<ScheduleEntity> existingSchedules = scheduleRepository.findByClassroomId(classroomId);
        return existingSchedules.stream().noneMatch(existing -> overlaps(existing, schedule));
    }

    private boolean overlaps(ScheduleEntity existing, OccupiedSchedule requested) {
        // Compare dayOfWeek or date
        String existingDay = existing.getDayOfWeek();
        String requestedDay = requested.getDayOfWeek();
        if (!existingDay.equalsIgnoreCase(requestedDay)) {
            return false;
        }
        // Compare times
        LocalTime existingStart = existing.getStartTime();
        LocalTime existingEnd = existing.getEndTime();
        LocalTime requestedStart = requested.getStartTime();
        LocalTime requestedEnd = requested.getEndTime();
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }

    // Método helper para crear OccupiedSchedule desde strings
    public OccupiedSchedule createReservationSchedule(String dayOfWeek, String startTime, String endTime) {
        try {
            return new OccupiedSchedule(dayOfWeek, LocalTime.parse(startTime), LocalTime.parse(endTime));
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido: " + e.getMessage());
        }
    }

    // Método helper para crear OccupiedSchedule con fecha específica
    public OccupiedSchedule createSpecificDateSchedule(java.time.LocalDate date, String startTime, String endTime) {
        try {
            return new OccupiedSchedule(date, LocalTime.parse(startTime), LocalTime.parse(endTime), null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido: " + e.getMessage());
        }
    }
}