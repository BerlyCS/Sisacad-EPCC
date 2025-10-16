package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "administrators")
public class AdministratorEntity {

    @Id
    @Column(name = "dni")
    private String dni;

    @Column(name = "paternal_surname")
    private String paternalSurname;

    @Column(name = "maternal_surname")
    private String maternalSurname;

    @Column(name = "name")
    private String name;

    @Column(name = "institutional_email")
    private String institutionalEmail;

    @Column(name = "password")
    private String password;

    public AdministratorEntity() {}

    public AdministratorEntity(String dni, String paternalSurname, String maternalSurname,
                               String name, String institutionalEmail, String password) {
        this.dni = dni;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.name = name;
        this.institutionalEmail = institutionalEmail;
        this.password = password;
    }

    // Getters and Setters
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
