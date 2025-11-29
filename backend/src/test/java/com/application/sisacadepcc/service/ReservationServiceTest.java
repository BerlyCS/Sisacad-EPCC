package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Reservation;
import com.application.sisacadepcc.domain.model.Schedule;
import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.domain.model.valueobject.ScheduleType;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.ReservationRepository;
import com.application.sisacadepcc.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private Authentication authentication;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(
                reservationRepository,
                scheduleRepository,
                authorizationService,
                classroomRepository,
                courseRepository
        );
    }

    @Test
    void createReservation_persistsWhenSlotAvailable() {
        Classroom classroom = buildClassroom(1L, "Aula", 101);
        when(classroomRepository.findAll()).thenReturn(List.of(classroom));
        when(reservationRepository.findByClassroomId(classroom.getClassroomID())).thenReturn(List.of());
        Schedule existingCourseSlot = new Schedule("LUNES", LocalTime.of(8, 0), LocalTime.of(10, 0));
        existingCourseSlot.setScheduleType(ScheduleType.COURSE);
        when(scheduleRepository.findByClassroomId(classroom.getClassroomID())).thenReturn(List.of(existingCourseSlot));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        configureProfessorAuthentication(42L);

        LocalDate reservationDate = LocalDate.of(2025, 1, 13); // Monday
        Reservation result = service.createReservation(
                "Aula 101",
                "Sesión de repaso",
                reservationDate,
                "10:00",
                "11:00",
                authentication
        );

        assertNotNull(result);
        assertEquals(classroom.getClassroomID(), result.getClassroomId());
        assertEquals(42L, result.getUserId());
        assertEquals(reservationDate, result.getReservationDate());
        assertEquals(LocalTime.of(10, 0), result.getSchedule().getStartTime());
        assertEquals(LocalTime.of(11, 0), result.getSchedule().getEndTime());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_rejectsOverlappingReservation() {
        Classroom classroom = buildClassroom(2L, "Aula", 202);
        when(classroomRepository.findAll()).thenReturn(List.of(classroom));

        LocalDate date = LocalDate.of(2025, 1, 14);
        Schedule bookedSlot = new Schedule("MARTES", LocalTime.of(9, 0), LocalTime.of(11, 0));
        bookedSlot.setScheduleType(ScheduleType.RESERVATION);
        Reservation existingReservation = new Reservation(classroom.getClassroomID(), 9L, "Uso previo", bookedSlot, date);
        existingReservation.setReservationDate(date);
        existingReservation.setSchedule(bookedSlot);
        when(reservationRepository.findByClassroomId(classroom.getClassroomID())).thenReturn(List.of(existingReservation));

        configureProfessorAuthentication(77L);

        assertThrows(IllegalArgumentException.class, () -> service.createReservation(
                "Aula 202",
                "Charla",
                date,
                "10:00",
                "11:00",
                authentication
        ));

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    private void configureProfessorAuthentication(Long userId) {
        lenient().when(authorizationService.isStudent(authentication)).thenReturn(false);
        lenient().when(authorizationService.isProfessor(authentication)).thenReturn(true);
        lenient().when(authorizationService.isSecretary(authentication)).thenReturn(false);
        lenient().when(authorizationService.isAdministrator(authentication)).thenReturn(false);

        Professor professor = new Professor();
        professor.setUserId(userId);
        lenient().when(authorizationService.getAuthenticatedProfessor(authentication)).thenReturn(Optional.of(professor));
    }

    private Classroom buildClassroom(Long id, String building, int number) {
        Place place = new Place(building, 1, number, 40, "GENERAL");
        Classroom classroom = new Classroom(id, place);
        classroom.setClassroomID(id);
        return classroom;
    }
}
