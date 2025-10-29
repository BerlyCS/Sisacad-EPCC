package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.config.security.RequiresAdministratorAccess;
import com.application.sisacadepcc.domain.model.Grade;
import com.application.sisacadepcc.service.CourseService;
import com.application.sisacadepcc.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/grades")
public class GradeController {
    private final GradeService service;

    public GradeController(GradeService service) {
        this.service = service;
    }
    @GetMapping
    public List<Grade> getAllGrades() {
        return service.getAllGrades();
    }
}
