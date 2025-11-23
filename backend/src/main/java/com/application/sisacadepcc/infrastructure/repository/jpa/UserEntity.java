package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.UserType;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "document_id", unique = true, nullable = false)
    private String documentId;

    @Column(name = "paternal_surname", nullable = false)
    private String paternalSurname;

    @Column(name = "maternal_surname", nullable = false)
    private String maternalSurname;

    @Column(name = "first_names", nullable = false)
    private String firstNames;

    @Column(name = "institutional_email", unique = true, nullable = false)
    private String institutionalEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    public UserEntity() {}

    public UserEntity(String documentId, String paternalSurname, String maternalSurname,
                      String firstNames, String institutionalEmail, UserType userType) {
        this.documentId = documentId;
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
    
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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