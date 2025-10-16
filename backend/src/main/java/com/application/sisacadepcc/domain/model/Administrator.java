package com.application.sisacadepcc.domain.model;

public class Administrator {

    private String dni;
    private String paternalSurname;
    private String maternalSurname;
    private String name;
    private String institutionalEmail;

    public Administrator() {}

    public Administrator(String dni, String paternalSurname, String maternalSurname,
                         String name, String institutionalEmail) {
        this.dni = dni;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.name = name;
        this.institutionalEmail = institutionalEmail;
    }

    // Getters y Setters
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
}