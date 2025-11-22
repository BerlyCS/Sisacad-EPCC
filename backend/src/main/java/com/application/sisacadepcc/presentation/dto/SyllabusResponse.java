package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.Topic;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record SyllabusResponse(
        Long syllabusId,
        Long courseId,
        ContentSummary content,
        List<TopicSummary> topics
) {
    public static SyllabusResponse from(Syllabus syllabus, Clock clock) {
        Objects.requireNonNull(syllabus, "Syllabus cannot be null");
        Objects.requireNonNull(clock, "Clock cannot be null");

        Content content = syllabus.getContent();
        ContentSummary contentSummary = content != null
                ? new ContentSummary(content.getName(), content.getType(), content.getUrl(), content.getSizeBytes())
                : null;

        List<TopicSummary> topicSummaries = syllabus.getTopics() == null
                ? List.of()
                : syllabus.getTopics().stream()
                    .sorted(Comparator.comparing(topic ->
                            topic.getSessionDate() != null ? topic.getSessionDate() : LocalDate.MAX))
                    .map(topic -> TopicSummary.from(topic, clock))
                    .toList();

        return new SyllabusResponse(
                syllabus.getId(),
                syllabus.getCourseId(),
                contentSummary,
                topicSummaries
        );
    }

    public record ContentSummary(
            String name,
            String type,
            String url,
            Long sizeBytes
    ) {}

    public record TopicSummary(
            String name,
            Double weight,
            LocalDate sessionDate,
            TopicScheduleStatus status
    ) {
        private static TopicSummary from(Topic topic, Clock clock) {
            Double weight = topic.getWeight() != null ? topic.getWeight().doubleValue() : null;
            return new TopicSummary(
                    topic.getName(),
                    weight,
                    topic.getSessionDate(),
                    resolveStatus(topic.getSessionDate(), clock)
            );
        }

        private static TopicScheduleStatus resolveStatus(LocalDate sessionDate, Clock clock) {
            if (sessionDate == null) {
                return TopicScheduleStatus.UNSCHEDULED;
            }
            LocalDate today = LocalDate.now(clock);
            if (sessionDate.isEqual(today)) {
                return TopicScheduleStatus.TODAY;
            }
            return sessionDate.isBefore(today)
                    ? TopicScheduleStatus.COMPLETED
                    : TopicScheduleStatus.UPCOMING;
        }
    }

    public enum TopicScheduleStatus {
        UPCOMING,
        TODAY,
        COMPLETED,
        UNSCHEDULED
    }
}
