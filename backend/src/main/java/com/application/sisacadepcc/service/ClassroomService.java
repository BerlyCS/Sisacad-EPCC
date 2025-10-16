package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    private final ClassroomRepository repository;

    public ClassroomService(ClassroomRepository repository) {
        this.repository = repository;
    }

    public List<Classroom> getAllClassrooms() {
        return repository.findAll();
    }
}