package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class StudentService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_CUI_LENGTH = 6;

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

    @Transactional
    public Student createStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Los datos del estudiante son obligatorios.");
        }

        String firstNames = sanitizeText(student.getFirstNames());
        String paternalSurname = sanitizeText(student.getPaternalSurname());
        String maternalSurname = sanitizeText(student.getMaternalSurname());
        String cui = sanitizeDigits(student.getCui());
        String email = sanitizeEmail(student.getInstitutionalEmail());

        if (firstNames.isEmpty()) {
            throw new IllegalArgumentException("Ingresa los nombres del estudiante.");
        }
        if (paternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido paterno.");
        }
        if (maternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido materno.");
        }
        if (cui.length() < MIN_CUI_LENGTH) {
            throw new IllegalArgumentException("El CUI debe tener al menos " + MIN_CUI_LENGTH + " dígitos.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresa un correo institucional válido.");
        }

        if (repository.existsByCorreoInstitucional(email)) {
            throw new IllegalArgumentException("El correo institucional ya está registrado.");
        }

        if (repository.findByCui(cui).isPresent()) {
            throw new IllegalArgumentException("El CUI ya está registrado.");
        }

        student.setFirstNames(firstNames);
        student.setPaternalSurname(paternalSurname);
        student.setMaternalSurname(maternalSurname);
        student.setCui(cui);
        student.setInstitutionalEmail(email);

        return repository.save(student);
    }

    private String sanitizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String sanitizeEmail(String value) {
        return sanitizeText(value).toLowerCase();
    }

    private String sanitizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
