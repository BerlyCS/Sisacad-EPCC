package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Professor;
import java.util.List;

public interface ProfessorRepository {
    List<Professor> findAll();
}
