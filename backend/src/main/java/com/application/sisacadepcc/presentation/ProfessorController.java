package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.service.ProfessorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Professor> getAllProfessors() {
        return service.getAllProfessors();
    }
}
