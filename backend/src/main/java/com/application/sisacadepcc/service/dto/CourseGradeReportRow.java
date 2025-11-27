package com.application.sisacadepcc.service.dto;

import java.util.Collections;
import java.util.List;

/**
 * Row payload used to generate the professor grade report in Excel.
 */
public record CourseGradeReportRow(
        String studentName,
        String studentCui,
        String groupLetter,
        List<Integer> continuousGrades,
        List<Integer> examGrades,
        Double finalGrade) {

    public CourseGradeReportRow {
        continuousGrades = continuousGrades != null ? List.copyOf(continuousGrades) : Collections.emptyList();
        examGrades = examGrades != null ? List.copyOf(examGrades) : Collections.emptyList();
    }
}
