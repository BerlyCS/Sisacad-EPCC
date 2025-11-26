package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.ExamStatisticType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course_group_exam_summaries",
        uniqueConstraints = @UniqueConstraint(name = "uq_group_exam_type", columnNames = {"group_id", "exam_number", "summary_type"}))
public class CourseGroupExamsPdfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private CourseGroupEntity courseGroup;

    @Column(name = "exam_number", nullable = false)
    private int examNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false, length = 16)
    private ExamStatisticType summaryType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "file_name", nullable = false, length = 255)),
            @AttributeOverride(name = "type", column = @Column(name = "file_type", nullable = false, length = 64)),
            @AttributeOverride(name = "url", column = @Column(name = "file_url", nullable = false, length = 255)),
            @AttributeOverride(name = "sizeBytes", column = @Column(name = "file_size_bytes"))
    })
    private Content content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseGroupEntity getCourseGroup() {
        return courseGroup;
    }

    public void setCourseGroup(CourseGroupEntity courseGroup) {
        this.courseGroup = courseGroup;
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
}
