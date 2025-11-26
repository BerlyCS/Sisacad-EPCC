package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByTimestampDesc(Long userId);

    List<NotificationEntity> findByUserIdAndReadFalse(Long userId);
}
