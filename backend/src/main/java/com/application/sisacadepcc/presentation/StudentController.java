package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.presentation.dto.StudentScheduleEntry;
import com.application.sisacadepcc.presentation.dto.StudentCourseResponse;
import com.application.sisacadepcc.presentation.dto.StudentCourseGroupResponse;
import com.application.sisacadepcc.service.StudentService;
import com.application.sisacadepcc.service.StudentCourseService;
import com.application.sisacadepcc.service.AuthorizationService;
import com.application.sisacadepcc.service.UserRole;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentController.class);

    private final StudentService service;
    private final StudentCourseService studentCourseService;
    private final StudentRepository studentRepository;
    private final AuthorizationService authorizationService;

    public StudentController(StudentService service,
                             StudentCourseService studentCourseService,
                             StudentRepository studentRepository,
                             AuthorizationService authorizationService) {
        this.service = service;
        this.studentCourseService = studentCourseService;
        this.studentRepository = studentRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/document/{studentId}/courses")
    @RequiresAdministratorAccess
    public ResponseEntity<List<StudentCourseResponse>> getCoursesByStudentDocument(@PathVariable Long studentId) {
        List<Course> courses = studentCourseService.getCoursesByStudent(studentId);
        return ResponseEntity.ok(mapToStudentCourseResponses(courses));
    }

    // NUEVO ENDPOINT para que estudiantes vean sus propios cursos - SIN anotación de administrador
    @GetMapping("/my-courses")
    public ResponseEntity<List<StudentCourseResponse>> getMyCourses(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }

            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.badRequest().build();
            }

            // Buscar el estudiante por email
            Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
            if (student.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Long studentId = student.get().getUserId();
            List<Course> courses = studentCourseService.getCoursesByStudent(studentId);
            return ResponseEntity.ok(mapToStudentCourseResponses(courses));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<List<StudentScheduleEntry>> getMySchedule(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }

            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
            if (student.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

                Long studentId = student.get().getUserId();
                List<StudentScheduleEntry> schedule = studentCourseService.getScheduleForStudent(studentId);
                String scheduleDump = schedule.stream()
                    .map(entry -> String.format("{courseId=%d, courseCode=%d, name=%s, type=%s, day=%s, start=%s, end=%s, room=%s}",
                        entry.getCourseId(),
                        entry.getCourseCode(),
                        entry.getCourseName(),
                        entry.getCourseType(),
                        entry.getDayOfWeek(),
                        entry.getStartTime(),
                        entry.getEndTime(),
                        entry.getClassroomName()))
                    .collect(Collectors.joining(", "));
                LOGGER.info("Student schedule response for {} ({} entries): [{}]",
                    studentId,
                    schedule.size(),
                    scheduleDump);
            return ResponseEntity.ok(schedule);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/my-profile")
    public ResponseEntity<Student> getMyProfile(@AuthenticationPrincipal OAuth2User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().build();
            }

            String email = principal.getAttribute("email");
            if (email == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Student> student = studentRepository.findByCorreoInstitucional(email);
            if (student.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(student.get());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @RequiresAdministratorAccess
    public ResponseEntity<?> createStudent(@RequestBody Student student) {
        try {
            Student created = service.createStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            LOGGER.error("Error creando estudiante", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo crear el estudiante"));
        }
    }

    @PutMapping("/{documentoIdentidad}")
    @RequiresAdministratorAccess
    public ResponseEntity<Student> updateStudent(@PathVariable String documentoIdentidad, @RequestBody Student student) {
        // Lógica para actualizar estudiante
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{documentoIdentidad}")
    @RequiresAdministratorAccess
    public ResponseEntity<Void> deleteStudent(@PathVariable String documentoIdentidad) {
        // Lógica para eliminar estudiante
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<Student>> getAllStudentsSorted(@RequestParam(defaultValue = "dni") String sortBy,
                                                              @RequestParam(defaultValue = "asc") String direction,
                                                              Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(service.getAllStudentsSorted(sortBy, direction));
    }

    @GetMapping("/profile/{cui}")
    public ResponseEntity<Student> getStudentProfile(@PathVariable String cui,
                                                                    Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY,
                UserRole.PROFESSOR, UserRole.STUDENT)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Student> studentOptional = service.getStudentByCui(cui);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOptional.get();

        if (authorizationService.hasRole(authentication, UserRole.STUDENT)) {
            Optional<String> requesterCui = authorizationService.getAuthenticatedStudentCui(authentication);
            if (requesterCui.isEmpty() || !requesterCui.get().equalsIgnoreCase(cui)) {
                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok(student);
    }

    @GetMapping("/{cui}/courses")
    public ResponseEntity<List<StudentCourseResponse>> getStudentCourses(@PathVariable String cui,
                                                          Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY,
                UserRole.PROFESSOR, UserRole.STUDENT)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Student> studentOptional = service.getStudentByCui(cui);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOptional.get();

        if (authorizationService.hasRole(authentication, UserRole.STUDENT)) {
            Optional<String> requesterCui = authorizationService.getAuthenticatedStudentCui(authentication);
            if (requesterCui.isEmpty() || !requesterCui.get().equalsIgnoreCase(cui)) {
                return ResponseEntity.status(403).build();
            }
        }

        List<Course> courses = studentCourseService.getCoursesByStudent(student.getUserId());
        return ResponseEntity.ok(mapToStudentCourseResponses(courses));
    }

    @GetMapping("/{cui}/schedule")
    public ResponseEntity<List<StudentScheduleEntry>> getStudentSchedule(@PathVariable String cui,
                                                                         Authentication authentication) {
        if (!authorizationService.hasAnyRole(authentication, UserRole.ADMIN, UserRole.SECRETARY,
                UserRole.PROFESSOR, UserRole.STUDENT)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Student> studentOptional = service.getStudentByCui(cui);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOptional.get();

        if (authorizationService.hasRole(authentication, UserRole.STUDENT)) {
            Optional<String> requesterCui = authorizationService.getAuthenticatedStudentCui(authentication);
            if (requesterCui.isEmpty() || !requesterCui.get().equalsIgnoreCase(cui)) {
                return ResponseEntity.status(403).build();
            }
        }

        List<StudentScheduleEntry> schedule = studentCourseService.getScheduleForStudent(student.getUserId());
        return ResponseEntity.ok(schedule);
    }

    private List<StudentCourseResponse> mapToStudentCourseResponses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return List.of();
        }

        return courses.stream()
                .map(this::mapCourseToResponse)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private StudentCourseResponse mapCourseToResponse(Course course) {
        if (course == null) {
            return null;
        }

        Long courseCode = course.getCourseCode() != null
                ? course.getCourseCode().longValue()
                : null;

        List<CourseGroup> groupCandidates = course.getGroups() != null ? course.getGroups() : List.<CourseGroup>of();
        List<StudentCourseGroupResponse> groups = groupCandidates.stream()
                .filter(Objects::nonNull)
                .map(this::mapCourseGroupToResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new StudentCourseResponse(
                course.getCourseId(),
                courseCode,
                course.getName(),
                course.getCredits(),
                groups
        );
    }

    private StudentCourseGroupResponse mapCourseGroupToResponse(CourseGroup group) {
        if (group == null) {
            return null;
        }

        CourseType courseType = group.getType() != null
                ? group.getType()
                : CourseType.THEORY;

        return new StudentCourseGroupResponse(
                group.getId(),
                group.getLetter(),
                courseType,
                resolveCourseTypeLabel(courseType)
        );
    }

    private String resolveCourseTypeLabel(CourseType type) {
        if (type == null) {
            return "Teoría";
        }
        return switch (type) {
            case LAB -> "Laboratorio";
            case THEORY -> "Teoría";
            case PRACTICE -> "Práctica";
        };
    }
}