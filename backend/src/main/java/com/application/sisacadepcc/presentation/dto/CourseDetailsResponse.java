package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.Syllabus;
import com.application.sisacadepcc.domain.model.valueobject.Content;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.model.valueobject.Topic;
import com.application.sisacadepcc.service.dto.CourseDetails;
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
        LabCourseSummary labCourse,
        Long syllabusId,
        SyllabusSummary syllabus,
        List<StudentSummary> enrolledStudents,
        Integer enrolledCount,
        List<Long> teacherIds
) {

    public static CourseDetailsResponse from(CourseDetails details) {
        Course course = details.course();
        String groupLetter = mapGroupLetter(course.getGroupLetter());
        CourseType type = course.getCourseType();

        LabCourseSummary labSummary = details.associatedLabCourse() != null
                ? LabCourseSummary.from(details.associatedLabCourse())
                : null;

        SyllabusSummary syllabusSummary = details.syllabus() != null
                ? SyllabusSummary.from(details.syllabus())
                : null;

        List<StudentSummary> students = details.enrolledStudents()
                .stream()
                .map(StudentSummary::from)
                .toList();

        List<Long> teacherIds = course.getTeacherIDs() != null ? course.getTeacherIDs() : List.of();

        return new CourseDetailsResponse(
                course.getCourseId(),
                course.getCourseCode(),
                course.getName(),
                course.getCreditNumber(),
                groupLetter,
                course.getAnio(),
                type != null ? type.name() : null,
                mapCourseTypeLabel(type),
                course.getLabPrerequisiteCourseId(),
                labSummary,
                course.getSyllabusID(),
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
                    course.getCourseCode(),
                    course.getName(),
                    CourseDetailsResponse.mapGroupLetter(course.getGroupLetter()),
                    CourseDetailsResponse.mapCourseTypeLabel(course.getCourseType())
            );
        }
    }

    public record SyllabusSummary(
            Long syllabusId,
            ContentSummary content,
            List<TopicSummary> topics
    ) {
        private static SyllabusSummary from(Syllabus syllabus) {
            Content content = syllabus.getContent();
            ContentSummary contentSummary = content != null
                    ? new ContentSummary(content.getName(), content.getType(), content.getUrl(), content.getSizeBytes())
                    : null;

            List<TopicSummary> topicSummaries = syllabus.getTopics() != null
                    ? syllabus.getTopics().stream()
                            .map(TopicSummary::from)
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
            Double weight
    ) {
        private static TopicSummary from(Topic topic) {
            Double weight = topic.getWeight() != null ? topic.getWeight().doubleValue() : null;
            return new TopicSummary(topic.getName(), weight);
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
                    student.getDocumentoIdentidad(),
                    student.getCui(),
                    student.getNombres(),
                    student.getApellidoPaterno(),
                    student.getApellidoMaterno(),
                    student.getCorreoInstitucional(),
                    student.getAnio()
            );
        }
    }
}
