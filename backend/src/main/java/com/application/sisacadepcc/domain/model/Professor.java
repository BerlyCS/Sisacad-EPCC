package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.UserType;

public class Professor extends User {

    public Professor() {
        super();
        setUserType(UserType.PROFESSOR);
    }

    public Professor(String documentId, String paternalSurname, String maternalSurname,
                     String firstNames, String institutionalEmail) {
        super(documentId, paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.PROFESSOR);
    }
}
