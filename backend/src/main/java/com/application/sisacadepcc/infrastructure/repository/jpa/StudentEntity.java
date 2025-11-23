package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.UserType;
import jakarta.persistence.*;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
public class StudentEntity extends UserEntity {

    @Column(name = "cui", unique = true)
    private String cui;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    public StudentEntity() {
        super();
        setUserType(UserType.STUDENT);
    }

    public StudentEntity(String documentId, String paternalSurname, String maternalSurname,
                         String firstNames, String institutionalEmail, String cui, Integer enrollmentYear) {
        super(documentId, paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.STUDENT);
        this.cui = cui;
        this.enrollmentYear = enrollmentYear;
    }

    // Getters and setters
    public String getCui() {
        return cui;
    }

    public void setCui(String cui) {
        this.cui = cui;
    }

    public Integer getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(Integer enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }
}
