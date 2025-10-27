package com.application.sisacadepcc.domain.model;

import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.Topic;
import com.application.sisacadepcc.infrastructure.repository.jpa.CourseEntity;

import java.util.List;
import java.util.Objects;

public class Syllabus {

    private Long id;
    private Long courseId;
    private Content content;
    private List<Topic> topics;

    public Syllabus(Long id, Long courseId, Content content, List<Topic> topics) {
        this.id = id;
        this.courseId = courseId;
        this.content = content;
        this.topics = topics;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Content getContent() { return content; }
    public List<Topic> getTopics() { return topics; }

    public void setId(Long id) { this.id = id; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setContent(Content content) { this.content = content; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Syllabus syllabus)) return false;
        return Objects.equals(id, syllabus.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}