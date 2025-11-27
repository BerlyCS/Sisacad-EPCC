package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.valueobject.AttendanceStatus;
import com.application.sisacadepcc.domain.model.valueobject.ClassType;
import com.application.sisacadepcc.domain.model.valueobject.GeoLocation;
import com.application.sisacadepcc.domain.repository.AttendanceRepository;
import com.application.sisacadepcc.domain.repository.StudentAttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository repository;

    @Mock
    private StudentAttendanceRepository studentAttendanceRepository;

    @Mock
    private SyllabusService syllabusService;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(repository, studentAttendanceRepository, syllabusService);
    }

    @Test
    void markAttendance_requiresSyllabus() {
        Long courseId = 555L;
        when(syllabusService.getByCourseId(courseId)).thenReturn(Optional.empty());

        LocalDate date = LocalDate.now();
        LocalDateTime timestamp = LocalDateTime.now();

        assertThrows(IllegalArgumentException.class, () -> attendanceService.markAttendance(
                1L,
                courseId,
                1L,
                AttendanceStatus.PRESENT,
                (GeoLocation) null,
                timestamp,
                date,
                ClassType.THEORY,
                "Tema X",
                LocalTime.of(9, 0)
        ));
    }
}
