package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.UserType;
import jakarta.persistence.*;

@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "user_id")
public class AdministratorEntity extends UserEntity {

    public AdministratorEntity() {
        super();
        setUserType(UserType.ADMINISTRATOR);
    }

    public AdministratorEntity(String documentId, String paternalSurname, String maternalSurname,
                               String firstNames, String institutionalEmail) {
        super(documentId, paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.ADMINISTRATOR);
    }
}
