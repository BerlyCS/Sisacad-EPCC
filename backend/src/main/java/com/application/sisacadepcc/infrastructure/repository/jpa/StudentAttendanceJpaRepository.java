package com.application.sisacadepcc.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentAttendanceJpaRepository extends JpaRepository<StudentAttendanceEntity, Long> {
    List<StudentAttendanceEntity> findByAttendance_AttendanceId(Long attendanceId);
    List<StudentAttendanceEntity> findByStudentId(String studentId);
    
    List<StudentAttendanceEntity> findByAttendance_GroupId(Long groupId);
    List<StudentAttendanceEntity> findByAttendance_CourseId(Long courseId);
    List<StudentAttendanceEntity> findByStudentIdAndAttendance_Date(String studentId, java.time.LocalDate date);

        @Query("""
                        SELECT sa FROM StudentAttendanceEntity sa
                        JOIN sa.attendance a
                        LEFT JOIN StudentEntity st ON st.documentoIdentidad = sa.studentId
                        WHERE a.courseId = :courseId
                            AND (sa.studentId = :studentId OR (st.cui IS NOT NULL AND st.cui = :studentId))
                        """)
        List<StudentAttendanceEntity> findByStudentIdAndCourseId(@Param("studentId") String studentId, @Param("courseId") Long courseId);

        @Query("""
                        SELECT new com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO(sa.id, a.date, a.classType, sa.status, a.todo)
                        FROM StudentAttendanceEntity sa
                        JOIN sa.attendance a
                        LEFT JOIN StudentEntity st ON st.documentoIdentidad = sa.studentId
                        WHERE a.courseId = :courseId
                            AND (sa.studentId = :studentId OR (st.cui IS NOT NULL AND st.cui = :studentId))
                        ORDER BY a.date DESC, a.timestamp DESC
                        """)
        List<com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO> findDTOByStudentIdAndCourseId(@Param("studentId") String studentId, @Param("courseId") Long courseId);
}
