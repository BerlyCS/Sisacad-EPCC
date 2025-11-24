package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Classroom;
import java.util.List;
import java.util.Optional;

public interface ClassroomRepository {
    List<Classroom> findAll();
    Optional<Classroom> findById(Long id);
    Classroom save(Classroom classroom);
}
