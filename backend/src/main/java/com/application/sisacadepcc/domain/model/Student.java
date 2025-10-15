package com.application.sisacadepcc.domain.model;

public class Student {
    private String documentoIdentidad;
    private String cui;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private String correoInstitucional;

    public Student() {}

    public Student(String documentoIdentidad, String cui, String apellidoPaterno,
                   String apellidoMaterno, String nombres, String correoInstitucional) {
        this.documentoIdentidad = documentoIdentidad;
        this.cui = cui;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombres = nombres;
        this.correoInstitucional = correoInstitucional;
    }

    // Getters y setters
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }
}
