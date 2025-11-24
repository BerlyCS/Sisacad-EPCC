package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.dto.StudentAttendanceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.stream.Collectors;

public interface StudentAttendanceJpaRepository extends JpaRepository<StudentAttendanceEntity, Long> {
    List<StudentAttendanceEntity> findByAttendance_AttendanceId(Long attendanceId);
    List<StudentAttendanceEntity> findByStudentId(String studentId);

    List<StudentAttendanceEntity> findByAttendance_CourseGroupId(Long courseGroupId);
    List<StudentAttendanceEntity> findByAttendance_CourseId(Long courseId);
    List<StudentAttendanceEntity> findByStudentIdAndAttendance_Date(String studentId, java.time.LocalDate date);

    List<StudentAttendanceEntity> findByStudentIdAndCourseId(String studentId, Long courseId);

    default List<StudentAttendanceDTO> findDTOByStudentIdAndCourseId(String studentId, Long courseId) {
        return findByStudentIdAndCourseId(studentId, courseId)
                .stream()
                .map(entity -> {
                    var attendance = entity.getAttendance();
                    return new StudentAttendanceDTO(
                            entity.getId(),
                            entity.getDate(),
                            attendance != null ? attendance.getClassType() : null,
                            entity.getStatus(),
                            attendance != null ? attendance.getTodo() : null
                    );
                })
                .collect(Collectors.toList());
    }
}
