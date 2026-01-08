package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.infrastructure.repository.jpa.CourseGroupEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.EnrollmentEntity;
import com.application.sisacadepcc.infrastructure.repository.jpa.StudentEntity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CourseStudentResponse(
        Long studentId,
        String cui,
        String firstNames,
        String paternalSurname,
        String maternalSurname,
        String institutionalEmail,
        String groupLetter
) {

    public static CourseStudentResponse from(EnrollmentEntity enrollment) {
        if (enrollment == null) {
            return null;
        }

        StudentEntity student = enrollment.getStudent();
        CourseGroupEntity group = enrollment.getCourseGroup();
        if (student == null) {
            return null;
        }

        String letter = group != null ? group.getLetter() : null;
        return new CourseStudentResponse(
                student.getUserId(),
                student.getCui(),
                student.getFirstNames(),
                student.getPaternalSurname(),
                student.getMaternalSurname(),
                student.getInstitutionalEmail(),
                letter != null ? letter : ""
        );
    }

    public String fullName() {
        return Stream.of(paternalSurname, maternalSurname, firstNames)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" ")).trim();
    }
}