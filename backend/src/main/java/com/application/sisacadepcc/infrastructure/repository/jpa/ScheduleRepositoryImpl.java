package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.repository.ScheduleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final ScheduleJpaRepository jpaRepository;

    public ScheduleRepositoryImpl(ScheduleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ScheduleEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<ScheduleEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ScheduleEntity> findByCourseGroupId(Long courseGroupId) {
        if (courseGroupId == null) {
            return List.of();
        }
        return jpaRepository.findByCourseGroup_Id(courseGroupId);
    }

    @Override
    public List<ScheduleEntity> findByClassroomId(Long classroomId) {
        if (classroomId == null) {
            return List.of();
        }
        return jpaRepository.findByClassroom_ClassroomId(classroomId);
    }

    @Override
    public ScheduleEntity save(ScheduleEntity schedule) {
        return jpaRepository.save(schedule);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            jpaRepository.deleteById(id);
        }
    }

    @Override
    public void deleteByCourseGroupId(Long courseGroupId) {
        if (courseGroupId != null) {
            jpaRepository.deleteByCourseGroup_Id(courseGroupId);
        }
    }
}
