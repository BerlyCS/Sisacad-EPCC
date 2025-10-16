package com.application.sisacadepcc.presentation;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.service.ClassroomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService service;

    public ClassroomController(ClassroomService service) {
        this.service = service;
    }

    @GetMapping
    public List<Classroom> getAllClassrooms() {
        return service.getAllClassrooms();
    }
}
