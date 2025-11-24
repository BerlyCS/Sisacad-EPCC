package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
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
        reservation.setReservedBy(entity.getReservedBy());
        reservation.setPurpose(entity.getPurpose());

        // Crear OccupiedSchedule desde el schedule referenciado
        ScheduleEntity scheduleEntity = entity.getSchedule();
        OccupiedSchedule schedule = new OccupiedSchedule(
                scheduleEntity.getDayOfWeek(),
                scheduleEntity.getStartTime(),
                scheduleEntity.getEndTime()
        );
        reservation.setSchedule(schedule);

        reservation.setCreatedAt(entity.getCreatedAt());
        reservation.setStatus(entity.getStatus());

        return reservation;
    }

    private ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservation.getId());
        entity.setReservedBy(reservation.getReservedBy());
        entity.setPurpose(reservation.getPurpose());

        // Crear ScheduleEntity desde el OccupiedSchedule
        OccupiedSchedule schedule = reservation.getSchedule();
        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setDayOfWeek(schedule.getDayOfWeek());
        scheduleEntity.setStartTime(schedule.getStartTime());
        scheduleEntity.setEndTime(schedule.getEndTime());
        // classroom and courseGroup remain null for reservations
        entity.setSchedule(scheduleEntity);

        // CORRECCIÓN: Asegurar que createdAt nunca sea null
        if (reservation.getCreatedAt() == null) {
            entity.setCreatedAt(java.time.LocalDateTime.now());
        } else {
            entity.setCreatedAt(reservation.getCreatedAt());
        }

        // CORRECCIÓN: Asegurar que status nunca sea null
        if (reservation.getStatus() == null) {
            entity.setStatus("PENDING"); // Valor por defecto
        } else {
            entity.setStatus(reservation.getStatus());
        }

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
    public List<Reservation> findByReservedBy(String reservedBy) {
        return jpaRepository.findByReservedBy(reservedBy).stream()
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

    @Override
    public List<Reservation> findByStatus(String status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
