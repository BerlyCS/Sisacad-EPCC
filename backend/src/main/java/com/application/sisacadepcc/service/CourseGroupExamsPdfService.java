package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.CourseGroupExamsPdf;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.model.valueobject.ExamStatisticType;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import com.application.sisacadepcc.service.exception.ExamSummaryAccessDeniedException;
import com.application.sisacadepcc.service.exception.ExamSummaryNotFoundException;
import com.application.sisacadepcc.service.exception.ExamSummaryValidationException;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class CourseGroupExamsPdfService {

    private static final int MIN_EXAM_NUMBER = 1;
    private static final int MAX_EXAM_NUMBER = 3;

    private final CourseGroupRepository courseGroupRepository;
    private final AuthorizationService authorizationService;
    private final ExamSummaryStorageService storageService;

    public CourseGroupExamsPdfService(CourseGroupRepository courseGroupRepository,
                                      AuthorizationService authorizationService,
                                      ExamSummaryStorageService storageService) {
        this.courseGroupRepository = courseGroupRepository;
        this.authorizationService = authorizationService;
        this.storageService = storageService;
    }

    public List<CourseGroupExamsPdf> listSummaries(Long groupId, Authentication authentication) {
        CourseGroup group = loadGroup(groupId);
        ensureCanView(group, authentication);
        return group.getExamPdfs().stream()
                .sorted(Comparator.comparingInt(CourseGroupExamsPdf::getExamNumber)
                        .thenComparing(summary -> summary.getSummaryType().name()))
                .toList();
    }

    public CourseGroupExamsPdf uploadSummary(Long groupId,
                                             int examNumber,
                                             ExamStatisticType summaryType,
                                             MultipartFile file,
                                             Authentication authentication) {
        CourseGroup group = loadGroup(groupId);
        ensureTheoryGroup(group);
        ensureCanModify(group, authentication);
        validateExamNumber(examNumber);

        CourseGroupExamsPdf existing = group.getExamPdfs().stream()
                .filter(summary -> summary.getExamNumber() == examNumber && summary.getSummaryType() == summaryType)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            storageService.deleteFile(existing.getContent());
        }

        Content storedContent = storageService.storeFile(file, groupId, examNumber, summaryType);
        CourseGroupExamsPdf updated = new CourseGroupExamsPdf(
                existing != null ? existing.getId() : null,
                groupId,
                examNumber,
                summaryType,
                storedContent
        );
        group.upsertExamPdf(updated);
        CourseGroup persisted = courseGroupRepository.save(group);

        return persisted.getExamPdfs().stream()
                .filter(summary -> summary.getExamNumber() == examNumber && summary.getSummaryType() == summaryType)
                .findFirst()
                .orElse(updated);
    }

    public void deleteSummary(Long groupId,
                              Long summaryId,
                              Authentication authentication) {
        CourseGroup group = loadGroup(groupId);
        ensureTheoryGroup(group);
        ensureCanModify(group, authentication);

        CourseGroupExamsPdf target = group.getExamPdfs().stream()
                .filter(summary -> Objects.equals(summary.getId(), summaryId))
                .findFirst()
                .orElseThrow(() -> new ExamSummaryNotFoundException("Summary not found for this group"));

        storageService.deleteFile(target.getContent());
        group.removeExamPdf(summaryId);
        courseGroupRepository.save(group);
    }

    public ExamPdfFile downloadSummary(Long groupId,
                                       Long summaryId,
                                       Authentication authentication) {
        CourseGroup group = loadGroup(groupId);
        ensureCanView(group, authentication);

        CourseGroupExamsPdf summary = group.getExamPdfs().stream()
                .filter(item -> Objects.equals(item.getId(), summaryId))
                .findFirst()
                .orElseThrow(() -> new ExamSummaryNotFoundException("Summary not found for this group"));

        Resource resource = storageService.loadAsResource(summary.getContent());
        return new ExamPdfFile(resource, summary);
    }

    private CourseGroup loadGroup(Long groupId) {
        return courseGroupRepository.findById(groupId)
                .orElseThrow(() -> new ExamSummaryNotFoundException("Course group not found"));
    }

    private void ensureTheoryGroup(CourseGroup group) {
        if (group.getType() != CourseType.THEORY) {
            throw new ExamSummaryValidationException("Only theory groups can upload exam summaries");
        }
    }

    private void validateExamNumber(int examNumber) {
        if (examNumber < MIN_EXAM_NUMBER || examNumber > MAX_EXAM_NUMBER) {
            throw new ExamSummaryValidationException("Exam number must be between " + MIN_EXAM_NUMBER + " and " + MAX_EXAM_NUMBER);
        }
    }

    private void ensureCanModify(CourseGroup group, Authentication authentication) {
        if (authorizationService.hasRole(authentication, UserRole.ADMIN)) {
            return;
        }
        if (!authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            throw new ExamSummaryAccessDeniedException("Only professors can manage exam summaries");
        }

        Professor professor = authorizationService.getAuthenticatedProfessor(authentication)
                .orElseThrow(() -> new ExamSummaryAccessDeniedException("Professor identification is required"));

        if (group.getTeacherId() == null || !Objects.equals(group.getTeacherId(), professor.getUserId())) {
            throw new ExamSummaryAccessDeniedException("You are not assigned to this theory group");
        }
    }

    private void ensureCanView(CourseGroup group, Authentication authentication) {
        if (authorizationService.hasRole(authentication, UserRole.ADMIN)) {
            return;
        }
        if (authorizationService.hasRole(authentication, UserRole.PROFESSOR)) {
            Professor professor = authorizationService.getAuthenticatedProfessor(authentication)
                    .orElse(null);
            if (professor != null && Objects.equals(group.getTeacherId(), professor.getUserId())) {
                return;
            }
        }
        throw new ExamSummaryAccessDeniedException("You are not allowed to view these summaries");
    }

    public record ExamPdfFile(Resource resource, CourseGroupExamsPdf summary) {}
}
