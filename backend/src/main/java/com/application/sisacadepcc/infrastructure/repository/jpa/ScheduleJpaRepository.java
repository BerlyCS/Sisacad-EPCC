package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScheduleJpaRepository extends JpaRepository<ScheduleEntity, Long> {

        @Query("select csa.schedule from CourseSchedule csa where csa.courseGroup.id = :courseGroupId")
            List<ScheduleEntity> findByCourseGroupId(@Param("courseGroupId") Long courseGroupId);

        @Query("select csa.schedule from CourseSchedule csa where csa.classroom.classroomId = :classroomId")
            List<ScheduleEntity> findByClassroomId(@Param("classroomId") Long classroomId);

    @Modifying
    @Transactional
    @Query("""
        delete from CourseSchedule csa
        where csa.courseGroup.id = :courseGroupId
        """)
     void deleteAssignmentsByCourseGroup(@Param("courseGroupId") Long courseGroupId);
}
