package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.valueobject.ScheduleType;
import com.application.sisacadepcc.domain.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    private Reservation toDomain(ReservationEntity entity) {
        Reservation reservation = new Reservation();
        reservation.setId(entity.getId());
        reservation.setUserId(entity.getUserId());
        reservation.setClassroomId(entity.getClassroomId());
        reservation.setPurpose(entity.getPurpose());

        // Crear Schedule domain desde el schedule referenciado
        ScheduleEntity scheduleEntity = entity.getSchedule();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleEntity.getId());
        schedule.setDayOfWeek(scheduleEntity.getDayOfWeek());
        schedule.setStartTime(scheduleEntity.getStartTime());
        schedule.setEndTime(scheduleEntity.getEndTime());
        schedule.setScheduleType(scheduleEntity.getScheduleType());
        reservation.setSchedule(schedule);
        reservation.setReservationDate(entity.getReservationDate());

        reservation.setCreatedAt(entity.getCreatedAt());

        return reservation;
    }

    private ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservation.getId());
        entity.setUserId(reservation.getUserId());
        entity.setPurpose(reservation.getPurpose());

        // Crear ScheduleEntity desde el Schedule domain
        Schedule schedule = reservation.getSchedule();
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setDayOfWeek(schedule.getDayOfWeek());
        scheduleEntity.setStartTime(schedule.getStartTime());
        scheduleEntity.setEndTime(schedule.getEndTime());
        // mark this schedule as a reservation-type schedule
        scheduleEntity.setScheduleType(ScheduleType.RESERVATION);
        // classroom and courseGroup remain null for reservations
        entity.setSchedule(scheduleEntity);

        // Persist reservation-specific date on reservation entity
        entity.setReservationDate(reservation.getReservationDate());

        // CORRECCIÓN: Asegurar que createdAt nunca sea null
        if (reservation.getCreatedAt() == null) {
            entity.setCreatedAt(java.time.LocalDateTime.now());
        } else {
            entity.setCreatedAt(reservation.getCreatedAt());
        }

        entity.setClassroomId(reservation.getClassroomId());

        return entity;
    }

    @Override
    public List<Reservation> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reservation> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByClassroomId(Long classroomId) {
        if (classroomId == null) {
            return List.of();
        }
        return jpaRepository.findByClassroomId(classroomId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = toEntity(reservation);
        ReservationEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
