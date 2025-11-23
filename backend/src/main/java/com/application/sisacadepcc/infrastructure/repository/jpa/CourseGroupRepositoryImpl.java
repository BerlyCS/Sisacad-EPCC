package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.valueobject.CourseSchedule;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseGroupRepositoryImpl implements CourseGroupRepository {

    private final CourseGroupJpaRepository jpaRepository;

    public CourseGroupRepositoryImpl(CourseGroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CourseGroup> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<CourseGroup> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public List<CourseGroup> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public CourseGroup save(CourseGroup courseGroup) {
        CourseGroupEntity entity = mapToEntity(courseGroup);
        CourseGroupEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public void deleteById(Long groupId) {
        if (groupId != null) {
            jpaRepository.deleteById(groupId);
        }
    }

    private CourseGroup mapToDomain(CourseGroupEntity entity) {
        CourseGroup group = new CourseGroup();
        group.setId(entity.getId());
        group.setLetter(entity.getLetter());
        group.setType(entity.getType());
        group.setMaxCapacity(entity.getMaxCapacity());
        group.setAvailableCapacity(entity.getAvailableCapacity());
        group.setScheduleSlots(mapScheduleSlotsToDomain(entity.getScheduleSlots()));
        // Enrollments are lazy loaded, so set to empty list to avoid overhead
        group.setEnrollments(new ArrayList<>());
        // Note: course reference may need to be set separately
        return group;
    }

    private List<CourseSchedule> mapScheduleSlotsToDomain(List<CourseScheduleEmbeddable> embeddables) {
        if (embeddables == null || embeddables.isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseSchedule> slots = new ArrayList<>();
        for (CourseScheduleEmbeddable embeddable : embeddables) {
            CourseSchedule slot = new CourseSchedule(
                    embeddable.getClassroomName(),
                    embeddable.getDayOfWeek(),
                    embeddable.getStartTime(),
                    embeddable.getEndTime()
            );
            slots.add(slot);
        }
        return slots;
    }

    private CourseGroupEntity mapToEntity(CourseGroup courseGroup) {
        CourseGroupEntity entity = new CourseGroupEntity();
        entity.setId(courseGroup.getId());
        entity.setLetter(courseGroup.getLetter());
        entity.setType(courseGroup.getType());
        entity.setMaxCapacity(courseGroup.getMaxCapacity());
        entity.setAvailableCapacity(courseGroup.getAvailableCapacity());
        entity.setScheduleSlots(mapScheduleSlotsToEmbeddable(courseGroup.getScheduleSlots()));
        // Note: course reference needs to be set
        return entity;
    }

    private List<CourseScheduleEmbeddable> mapScheduleSlotsToEmbeddable(List<CourseSchedule> slots) {
        List<CourseScheduleEmbeddable> embeddables = new ArrayList<>();
        if (slots == null) {
            return embeddables;
        }
        for (CourseSchedule slot : slots) {
            CourseScheduleEmbeddable embeddable = new CourseScheduleEmbeddable();
            embeddable.setClassroomName(slot.getClassroomName());
            embeddable.setDayOfWeek(slot.getDayOfWeek());
            embeddable.setStartTime(slot.getStartTime());
            embeddable.setEndTime(slot.getEndTime());
            embeddables.add(embeddable);
        }
        return embeddables;
    }
}