package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.UserType;
import jakarta.persistence.*;

@Entity
@Table(name = "professors")
@PrimaryKeyJoinColumn(name = "user_id")
public class ProfessorEntity extends UserEntity {

    public ProfessorEntity() {
        super();
        setUserType(UserType.PROFESSOR);
    }

    public ProfessorEntity(String paternalSurname, String maternalSurname,
                           String firstNames, String institutionalEmail) {
        super(paternalSurname, maternalSurname, firstNames, institutionalEmail, UserType.PROFESSOR);
    }
}
