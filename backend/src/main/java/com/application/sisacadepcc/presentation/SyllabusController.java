package com.application.sisacadepcc.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.service.SyllabusService;

@RestController
@RequestMapping("/api/syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;

    public SyllabusController(SyllabusService syllabusService) {
        this.syllabusService = syllabusService;
    }

    @GetMapping
    public ResponseEntity<List<Syllabus>> getAllSyllabus() {
        List<Syllabus> syllabus = syllabusService.getAllSyllabus();
        return ResponseEntity.ok(syllabus);
    }

}