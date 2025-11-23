package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.UserType;

public class Administrator extends User {

    public Administrator() {
        super();
        setUserType(UserType.ADMINISTRATOR);
    }

    public Administrator(String documentId, String paternalSurname, String maternalSurname,
                         String firstNames, String institutionalEmail) {
        super(documentId, paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.ADMINISTRATOR);
    }
}