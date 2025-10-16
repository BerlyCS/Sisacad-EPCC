package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Classroom;
import java.util.List;

public interface ClassroomRepository {
    List<Classroom> findAll();
}
