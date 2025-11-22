package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.presentation.dto.SyllabusResponse;
import com.application.sisacadepcc.presentation.dto.SyllabusTopicRequest;
import com.application.sisacadepcc.presentation.dto.SyllabusTopicsUpdateRequest;
import com.application.sisacadepcc.presentation.dto.SyllabusUploadRequest;
import com.application.sisacadepcc.service.SyllabusService;
import com.application.sisacadepcc.service.dto.SyllabusTopicInput;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

@RestController
@Validated
@RequestMapping("/api/syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;
    private final Clock clock;

    public SyllabusController(SyllabusService syllabusService, Clock clock) {
        this.syllabusService = syllabusService;
        this.clock = clock;
    }

    @GetMapping
    public ResponseEntity<List<Syllabus>> getAllSyllabus() {
        return ResponseEntity.ok(syllabusService.getAllSyllabus());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<SyllabusResponse> getSyllabusByCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        syllabusService.assertReadAccess(authentication);
        return syllabusService.getByCourseId(courseId)
                .map(syllabus -> ResponseEntity.ok(SyllabusResponse.from(syllabus, clock)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SyllabusResponse> uploadSyllabus(
            @RequestPart("metadata") @Valid SyllabusUploadRequest request,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        List<SyllabusTopicInput> topicInputs = mapTopicRequests(request.topics());
        Syllabus syllabus = syllabusService.uploadSyllabus(request.courseId(), file, topicInputs, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SyllabusResponse.from(syllabus, clock));
    }

    @DeleteMapping("/{syllabusId}")
    public ResponseEntity<Void> deleteSyllabus(@PathVariable Long syllabusId, Authentication authentication) {
        syllabusService.deleteSyllabus(syllabusId, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/course/{courseId}/topics")
    public ResponseEntity<SyllabusResponse> updateTopics(
            @PathVariable Long courseId,
            @RequestBody @Valid SyllabusTopicsUpdateRequest request,
            Authentication authentication) {
        if (!Objects.equals(courseId, request.courseId())) {
            return ResponseEntity.badRequest().build();
        }
        List<SyllabusTopicInput> topicInputs = mapTopicRequests(request.topics());
        Syllabus syllabus = syllabusService.updateTopics(courseId, topicInputs, authentication);
        return ResponseEntity.ok(SyllabusResponse.from(syllabus, clock));
    }

    @GetMapping("/{syllabusId}/download")
    public ResponseEntity<Resource> downloadSyllabus(
            @PathVariable Long syllabusId,
            Authentication authentication) {
        SyllabusService.SyllabusFile file = syllabusService.downloadFile(syllabusId, authentication);
        HttpHeaders headers = new HttpHeaders();
        String filename = file.content() != null && file.content().getName() != null
                ? file.content().getName()
                : "syllabus.pdf";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF);
        if (file.content() != null && file.content().getSizeBytes() != null) {
            builder.contentLength(file.content().getSizeBytes());
        }
        return builder.body(file.resource());
    }

    private List<SyllabusTopicInput> mapTopicRequests(List<SyllabusTopicRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
            .filter(Objects::nonNull)
                .map(request -> new SyllabusTopicInput(
                    request.name(),
                    BigDecimal.valueOf(request.weight()),
                    request.sessionDate()))
                .toList();
    }
}