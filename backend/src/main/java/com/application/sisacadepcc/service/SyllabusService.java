package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.Topic;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.SyllabusRepository;
import com.application.sisacadepcc.service.dto.SyllabusTopicInput;
import com.application.sisacadepcc.service.exception.InvalidSyllabusException;
import com.application.sisacadepcc.service.exception.SyllabusAccessDeniedException;
import com.application.sisacadepcc.service.exception.SyllabusNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final CourseRepository courseRepository;
    private final AuthorizationService authorizationService;
    private final SyllabusStorageService storageService;
    private final Clock clock;

    public SyllabusService(SyllabusRepository syllabusRepository,
                           CourseRepository courseRepository,
                           AuthorizationService authorizationService,
                           SyllabusStorageService storageService,
                           Clock clock) {
        this.syllabusRepository = syllabusRepository;
        this.courseRepository = courseRepository;
        this.authorizationService = authorizationService;
        this.storageService = storageService;
        this.clock = clock;
    }

    public List<Syllabus> getAllSyllabus() {
        return syllabusRepository.findAll();
    }

    public Optional<Syllabus> getByCourseId(Long courseId) {
        return syllabusRepository.findByCourseId(courseId);
    }

    public List<Topic> getUpcomingTopics(Long courseId) {
        LocalDate today = LocalDate.now(clock);
        return syllabusRepository.findByCourseId(courseId)
                .map(Syllabus::getTopics)
                .orElse(List.of())
                .stream()
                .filter(topic -> topic.getSessionDate() != null && !topic.getSessionDate().isBefore(today))
                .sorted(Comparator.comparing(Topic::getSessionDate))
                .toList();
    }

    public Syllabus uploadSyllabus(Long courseId,
                                   MultipartFile syllabusFile,
                                   List<SyllabusTopicInput> topics,
                                   Authentication authentication) {
        Course course = findCourse(courseId);
        ensureCanModify(authentication, course);

        List<Topic> topicEntities = mapTopics(topics);

        Syllabus existing = syllabusRepository.findByCourseId(courseId).orElse(null);
        if (topicEntities.isEmpty() && existing != null && existing.getTopics() != null) {
            topicEntities = new ArrayList<>(existing.getTopics());
        }
        if (existing != null && existing.getContent() != null) {
            storageService.deleteFile(existing.getContent());
        }

        Content content = storageService.storeFile(syllabusFile, courseId);
        Syllabus syllabusToPersist = buildSyllabus(existing != null ? existing.getId() : null, courseId, content, topicEntities);

        return syllabusRepository.save(syllabusToPersist);
    }

    public Syllabus updateTopics(Long courseId,
                                 List<SyllabusTopicInput> topics,
                                 Authentication authentication) {
        Course course = findCourse(courseId);
        ensureCanModify(authentication, course);

        Syllabus existing = syllabusRepository.findByCourseId(courseId)
                .orElseThrow(() -> new SyllabusNotFoundException("No syllabus exists for this course"));

        List<Topic> topicEntities = mapTopics(topics);
        Syllabus syllabusToPersist = buildSyllabus(existing.getId(), courseId, existing.getContent(), topicEntities);

        return syllabusRepository.save(syllabusToPersist);
    }

    public void deleteSyllabus(Long syllabusId, Authentication authentication) {
        Syllabus existing = syllabusRepository.findById(syllabusId)
                .orElseThrow(() -> new SyllabusNotFoundException("Syllabus not found"));
        Course course = findCourse(existing.getCourseId());
        ensureCanModify(authentication, course);

        storageService.deleteFile(existing.getContent());
        syllabusRepository.deleteById(syllabusId);
    }

    public SyllabusFile downloadFile(Long syllabusId, Authentication authentication) {
        ensureCanView(authentication);
        Syllabus syllabus = syllabusRepository.findById(syllabusId)
                .orElseThrow(() -> new SyllabusNotFoundException("Syllabus not found"));
        Resource resource = storageService.loadAsResource(syllabus.getContent());
        return new SyllabusFile(resource, syllabus.getContent());
    }

    public void assertReadAccess(Authentication authentication) {
        ensureCanView(authentication);
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new SyllabusNotFoundException("Course not found"));
    }

    private void ensureCanModify(Authentication authentication, Course course) {
        if (authorizationService.hasRole(authentication, UserRole.ADMIN)) {
            return;
        }
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            throw new SyllabusAccessDeniedException("Only professors or administrators can manage syllabi");
        }

        Long professorId = authorizationService.getAuthenticatedProfessor(authentication)
                .map(Professor::getUserId)
                .orElse(null);

        if (professorId == null) {
            throw new SyllabusAccessDeniedException("Professor identification is required");
        }

        List<Long> teacherIds = course.getGroups().stream().map(CourseGroup::getTeacherId).filter(Objects::nonNull).distinct().toList();
        boolean isAssigned = teacherIds.stream().anyMatch(id -> Objects.equals(id, professorId));
        if (!isAssigned) {
            throw new SyllabusAccessDeniedException("You are not assigned to this course");
        }
    }

    private void ensureCanView(Authentication authentication) {
        boolean hasAccess = authorizationService.hasAnyRole(authentication,
                UserRole.ADMIN,
                UserRole.PROFESSOR,
                UserRole.STUDENT,
                UserRole.SECRETARY);
        if (!hasAccess) {
            throw new SyllabusAccessDeniedException("You are not allowed to view this syllabus");
        }
    }

    private List<Topic> mapTopics(List<SyllabusTopicInput> inputs) {
        if (inputs == null) {
            return List.of();
        }
        List<Topic> topics = new ArrayList<>();
        for (SyllabusTopicInput input : inputs) {
            if (input == null) {
                continue;
            }
            if (input.name() == null || input.name().isBlank()) {
                throw new InvalidSyllabusException("Topic name cannot be empty");
            }
            if (input.weight() == null || input.weight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidSyllabusException("Topic weight must be greater than zero");
            }
            if (input.sessionDate() == null) {
                throw new InvalidSyllabusException("Topic session date is required");
            }
            topics.add(new Topic(input.name(), input.weight(), input.sessionDate()));
        }
        topics.sort(Comparator.comparing(Topic::getSessionDate));
        return topics;
    }

    private Syllabus buildSyllabus(Long id, Long courseId, Content content, List<Topic> topics) {
        return new Syllabus(id, courseId, content, topics);
    }

    public record SyllabusFile(Resource resource, Content content) {}
}