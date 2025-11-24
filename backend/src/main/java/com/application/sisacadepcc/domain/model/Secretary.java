package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.UserType;

public class Secretary extends User {

    public Secretary() {
        super();
        setUserType(UserType.SECRETARY);
    }

    public Secretary(String paternalSurname, String maternalSurname,
                     String firstNames, String institutionalEmail) {
        super(paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.SECRETARY);
    }
}