package com.application.sisacadepcc.infrastructure.repository.jpa;

import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.Topic;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "syllabus")
public class SyllabusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private CourseEntity course;

    @Embedded
    private Content content;

    @ElementCollection
    @CollectionTable(name = "syllabus_topics", joinColumns = @JoinColumn(name = "syllabus_id"))
    private List<Topic> topics;

    public SyllabusEntity() {}

    public SyllabusEntity(CourseEntity course, Content content, List<Topic> topics) {
        this.course = course;
        this.content = content;
        this.topics = topics;
    }

    public Long getId() { return id; }
    public CourseEntity getCourse() { return course; }
    public Content getContent() { return content; }
    public List<Topic> getTopics() { return topics; }

    public void setId(Long id) { this.id = id; }
    public void setCourse(CourseEntity course) { this.course = course; }
    public void setContent(Content content) { this.content = content; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }
}
