package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.ExamStatisticType;

import java.util.Objects;

public class CourseGroupExamsPdf {

    private Long id;
    private Long courseGroupId;
    private int examNumber;
    private ExamStatisticType summaryType;
    private Content content;

    public CourseGroupExamsPdf() {
    }

    public CourseGroupExamsPdf(Long id,
                               Long courseGroupId,
                               int examNumber,
                               ExamStatisticType summaryType,
                               Content content) {
        this.id = id;
        this.courseGroupId = courseGroupId;
        this.examNumber = examNumber;
        this.summaryType = summaryType;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseGroupId() {
        return courseGroupId;
    }

    public void setCourseGroupId(Long courseGroupId) {
        this.courseGroupId = courseGroupId;
    }

    public int getExamNumber() {
        return examNumber;
    }

    public void setExamNumber(int examNumber) {
        this.examNumber = examNumber;
    }

    public ExamStatisticType getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(ExamStatisticType summaryType) {
        this.summaryType = summaryType;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseGroupExamsPdf that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
