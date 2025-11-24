package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.UserType;
import jakarta.persistence.*;

@Entity
@Table(name = "secretaries")
@PrimaryKeyJoinColumn(name = "user_id")
public class SecretaryEntity extends UserEntity {

    public SecretaryEntity() {
        super();
        setUserType(UserType.SECRETARY);
    }

    public SecretaryEntity(String paternalSurname, String maternalSurname,
                           String firstNames, String institutionalEmail) {
        super(paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.SECRETARY);
    }
}
