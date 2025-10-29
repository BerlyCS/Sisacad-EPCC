package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();
    Optional<Reservation> findById(Long id);
    List<Reservation> findByClassroomName(String classroomName);
    List<Reservation> findByReservedBy(String reservedBy);
    Reservation save(Reservation reservation);
    void deleteById(Long id);
    List<Reservation> findByStatus(String status);
}
