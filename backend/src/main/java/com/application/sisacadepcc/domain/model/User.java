package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.UserType;

public abstract class User {

    private Long userId;
    private String paternalSurname;
    private String maternalSurname;
    private String firstNames;
    private String institutionalEmail;
    private UserType userType;

    public User() {}

    public User(String paternalSurname, String maternalSurname,
                String firstNames, String institutionalEmail, UserType userType) {
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.firstNames = firstNames;
        this.institutionalEmail = institutionalEmail;
        this.userType = userType;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public void setPaternalSurname(String paternalSurname) {
        this.paternalSurname = paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public void setMaternalSurname(String maternalSurname) {
        this.maternalSurname = maternalSurname;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}