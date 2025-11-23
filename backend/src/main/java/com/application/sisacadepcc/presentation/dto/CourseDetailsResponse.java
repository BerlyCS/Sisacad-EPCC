package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.model.valueobject.Topic;
import com.application.sisacadepcc.service.dto.CourseDetails;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public record CourseDetailsResponse(
        Long courseId,
        Long courseCode,
        String name,
        Integer creditNumber,
        String groupLetter,
        Integer anio,
        String courseType,
        String courseTypeLabel,
        Long labPrerequisiteCourseId,
        Integer labCapacity,
        LabCourseSummary labCourse,
        Long syllabusId,
        SyllabusSummary syllabus,
        List<StudentSummary> enrolledStudents,
        Integer enrolledCount,
        List<Long> teacherIds
) {

        public static CourseDetailsResponse from(CourseDetails details) {
                return from(details, Clock.systemDefaultZone());
        }

        public static CourseDetailsResponse from(CourseDetails details, Clock clock) {
        CourseGroup courseGroup = details.courseGroup();
        Course course = courseGroup.getCourse();
        String groupLetter = courseGroup.getLetter() != null ? courseGroup.getLetter() : "";
        CourseType type = courseGroup.getType();

        LabCourseSummary labSummary = details.associatedLabCourse() != null
                ? LabCourseSummary.from(details.associatedLabCourse())
                : null;

        SyllabusSummary syllabusSummary = details.syllabus() != null
                ? SyllabusSummary.from(details.syllabus(), clock)
                : null;

        List<StudentSummary> students = details.enrolledStudents()
                .stream()
                .map(StudentSummary::from)
                .toList();

        List<Long> teacherIds = courseGroup.getTeacherId() != null ? List.of(courseGroup.getTeacherId()) : List.of();

        return new CourseDetailsResponse(
                course.getCourseId(),
                Long.valueOf(course.getCourseCode()),
                course.getName(),
                course.getCredits(),
                groupLetter,
                null, // anio removed
                type != null ? type.name() : null,
                mapCourseTypeLabel(type),
                null, // labPrerequisiteCourseId removed
                CourseType.LAB.equals(type) ? null : null, // labCapacity removed
                labSummary,
                course.getSyllabusId(),
                syllabusSummary,
                students,
                students.size(),
                teacherIds
        );
    }

    private static String mapGroupLetter(char groupLetter) {
        return groupLetter == '\u0000' ? "" : String.valueOf(groupLetter);
    }

    private static String mapCourseTypeLabel(CourseType courseType) {
        if (courseType == null) {
            return "Teoría";
        }
        return switch (courseType) {
            case THEORY -> "Teoría";
            case LAB -> "Laboratorio";
            case PRACTICE -> "Práctica";
        };
    }

    public record LabCourseSummary(
            Long courseId,
            Long courseCode,
            String name,
            String groupLetter,
            String courseTypeLabel
    ) {
        private static LabCourseSummary from(Course course) {
            return new LabCourseSummary(
                    course.getCourseId(),
                    Long.valueOf(course.getCourseCode()),
                    course.getName(),
                    "", // groupLetter removed
                    "Teoría" // default courseTypeLabel
            );
        }
    }

    public record SyllabusSummary(
            Long syllabusId,
            ContentSummary content,
            List<TopicSummary> topics
    ) {
        private static SyllabusSummary from(Syllabus syllabus, Clock clock) {
            Content content = syllabus.getContent();
            ContentSummary contentSummary = content != null
                    ? new ContentSummary(content.getName(), content.getType(), content.getUrl(), content.getSizeBytes())
                    : null;

            List<TopicSummary> topicSummaries = syllabus.getTopics() != null
                    ? syllabus.getTopics().stream()
                            .map(topic -> TopicSummary.from(topic, clock))
                            .toList()
                    : List.of();

            return new SyllabusSummary(
                    syllabus.getId(),
                    contentSummary,
                    topicSummaries
            );
        }
    }

    public record ContentSummary(
            String name,
            String type,
            String url,
            Long sizeBytes
    ) {
    }

    public record TopicSummary(
            String name,
            Double weight,
            LocalDate sessionDate,
            SyllabusResponse.TopicScheduleStatus status
    ) {
                private static TopicSummary from(Topic topic, Clock clock) {
            Double weight = topic.getWeight() != null ? topic.getWeight().doubleValue() : null;
                        LocalDate sessionDate = topic.getSessionDate();
                        SyllabusResponse.TopicScheduleStatus status = resolveStatus(sessionDate, clock);
                        return new TopicSummary(topic.getName(), weight, sessionDate, status);
        }

                private static SyllabusResponse.TopicScheduleStatus resolveStatus(LocalDate sessionDate, Clock clock) {
                        if (sessionDate == null) {
                                return SyllabusResponse.TopicScheduleStatus.UNSCHEDULED;
                        }
                        LocalDate today = LocalDate.now(clock);
                        if (sessionDate.isEqual(today)) {
                                return SyllabusResponse.TopicScheduleStatus.TODAY;
                        }
                        if (sessionDate.isBefore(today)) {
                                return SyllabusResponse.TopicScheduleStatus.COMPLETED;
                        }
                        return SyllabusResponse.TopicScheduleStatus.UPCOMING;
                }
    }

    public record StudentSummary(
            String documentoIdentidad,
            String cui,
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            String correoInstitucional,
            Integer anio
    ) {
        private static StudentSummary from(Student student) {
            return new StudentSummary(
                    student.getDocumentId(),
                    student.getCui(),
                    student.getFirstNames(),
                    student.getPaternalSurname(),
                    student.getMaternalSurname(),
                    student.getInstitutionalEmail(),
                    student.getEnrollmentYear()
            );
        }
    }
}
