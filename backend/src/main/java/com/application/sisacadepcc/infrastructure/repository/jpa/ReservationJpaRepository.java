package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByClassroomName(String classroomName);
    List<ReservationEntity> findByReservedBy(String reservedBy);
    List<ReservationEntity> findByStatus(String status);
}
