package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Administrator;
import com.application.sisacadepcc.domain.model.Professor;
import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.User;
import com.application.sisacadepcc.domain.repository.AdministratorRepository;
import com.application.sisacadepcc.domain.repository.ProfessorRepository;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDirectoryService {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final SecretaryRepository secretaryRepository;
    private final AdministratorRepository administratorRepository;

    public UserDirectoryService(StudentRepository studentRepository,
                                ProfessorRepository professorRepository,
                                SecretaryRepository secretaryRepository,
                                AdministratorRepository administratorRepository) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.secretaryRepository = secretaryRepository;
        this.administratorRepository = administratorRepository;
    }

    public Optional<String> resolveDisplayName(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        return studentRepository.findById(userId).flatMap(this::formatFullName)
                .or(() -> professorRepository.findById(userId).flatMap(this::formatFullName))
                .or(() -> secretaryRepository.findById(userId).flatMap(this::formatFullName))
                .or(() -> administratorRepository.findById(userId).flatMap(this::formatFullName));
    }

    public String resolveDisplayNameOrFallback(Long userId) {
        return resolveDisplayName(userId)
                .filter(name -> !name.isBlank())
                .orElseGet(() -> userId != null ? "Usuario #" + userId : "Usuario desconocido");
    }

    private Optional<String> formatFullName(User user) {
        if (user == null) {
            return Optional.empty();
        }

        StringBuilder builder = new StringBuilder();
        if (user.getFirstNames() != null) {
            builder.append(user.getFirstNames().trim());
        }
        if (user.getPaternalSurname() != null) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(user.getPaternalSurname().trim());
        }
        if (user.getMaternalSurname() != null) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(user.getMaternalSurname().trim());
        }

        String fullName = builder.toString().trim();
        if (!fullName.isBlank()) {
            return Optional.of(fullName);
        }

        return Optional.ofNullable(user.getInstitutionalEmail()).filter(s -> !s.isBlank());
    }
}
