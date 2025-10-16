package com.application.sisacadepcc.domain.repository;

import com.application.sisacadepcc.domain.model.Course;
import java.util.List;

public interface CourseRepository {
    List<Course> findAll();
}
