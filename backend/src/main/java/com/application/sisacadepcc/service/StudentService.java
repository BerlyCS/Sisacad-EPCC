package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }
}
