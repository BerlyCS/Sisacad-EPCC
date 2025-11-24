package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.UserType;

public class Student extends User {

    private String cui;
    private Integer enrollmentYear;

    public Student() {
        super();
        setUserType(UserType.STUDENT);
    }

    public Student(String paternalSurname, String maternalSurname,
                   String firstNames, String institutionalEmail, String cui, Integer enrollmentYear) {
        super(paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.STUDENT);
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
