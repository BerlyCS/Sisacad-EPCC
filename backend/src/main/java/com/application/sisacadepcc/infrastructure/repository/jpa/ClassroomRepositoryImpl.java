package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<Classroom> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Classroom save(Classroom classroom) {
        ClassroomEntity entity = mapToEntity(classroom);
        ClassroomEntity savedEntity = jpaRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    private Classroom mapToDomain(ClassroomEntity entity) {
        Classroom classroom = new Classroom(
                entity.getClassroomId(),
                entity.getPlace()
        );
        return classroom;
    }

    private ClassroomEntity mapToEntity(Classroom classroom) {
        return new ClassroomEntity(classroom.getClassroomID(), classroom.getPlace());
    }
}
