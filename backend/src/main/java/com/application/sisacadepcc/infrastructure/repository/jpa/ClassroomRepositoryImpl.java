package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.domain.model.valueobject.OccupiedSchedule;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ClassroomRepositoryImpl implements ClassroomRepository {

    private final ClassroomJpaRepository jpaRepository;

    public ClassroomRepositoryImpl(ClassroomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Classroom> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Classroom mapToDomain(ClassroomEntity entity) {
        return new Classroom(
                entity.getClassroomId(),
                entity.getPlace(),
                entity.getOccupiedSchedules()
        );
    }
}
