package com.application.sisacadepcc.infrastructure.repository.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class StudentEntity {

    @Id
    @Column(name = "documento_identidad")
    private String documentoIdentidad;

    private String cui;

    @Column(name = "apellido_paterno")
    private String apellidoPaterno;

    @Column(name = "apellido_materno")
    private String apellidoMaterno;

    private String nombres;

    @Column(name = "correo_institucional")
    private String correoInstitucional;

    @Column(name = "anio")
    private Integer anio; // Cambiado de año a anio

    public StudentEntity() {}

    public StudentEntity(String documentoIdentidad, String cui, String apellidoPaterno,
                         String apellidoMaterno, String nombres, String correoInstitucional, Integer anio) {
        this.documentoIdentidad = documentoIdentidad;
        this.cui = cui;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombres = nombres;
        this.correoInstitucional = correoInstitucional;
        this.anio = anio;
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

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
}
