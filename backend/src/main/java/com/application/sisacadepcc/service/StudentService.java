package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public Optional<Student> getStudentByCui(String cui) {
        if (cui == null || cui.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCui(cui.trim());
    }

    public List<Student> getAllStudentsSorted(String sortBy, String direction) {
        List<Student> students = repository.findAll();

        Comparator<Student> comparator;
        if ("dni".equals(sortBy)) {
            comparator = Comparator.comparing(Student::getUserId, Comparator.nullsLast(Long::compareTo));
        } else if ("cui".equals(sortBy)) {
            comparator = Comparator.comparing(Student::getCui, Comparator.nullsLast(String::compareTo));
        } else if ("name".equals(sortBy)) {
            comparator = Comparator.comparing(s -> {
                String nombres = s.getFirstNames() != null ? s.getFirstNames() : "";
                String apellidoP = s.getPaternalSurname() != null ? s.getPaternalSurname() : "";
                String apellidoM = s.getMaternalSurname() != null ? s.getMaternalSurname() : "";
                return (nombres + " " + apellidoP + " " + apellidoM).trim();
            }, Comparator.nullsLast(String::compareTo));
        } else if ("apellidos".equals(sortBy)) {
            comparator = Comparator.comparing(s -> {
                String apellidoP = s.getPaternalSurname() != null ? s.getPaternalSurname() : "";
                String apellidoM = s.getMaternalSurname() != null ? s.getMaternalSurname() : "";
                return (apellidoP + " " + apellidoM).trim();
            }, Comparator.nullsLast(String::compareTo));
        } else {
            comparator = Comparator.comparing(Student::getUserId, Comparator.nullsLast(Long::compareTo));
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return students.stream().sorted(comparator).toList();
    }
}
