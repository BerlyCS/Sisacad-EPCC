package com.application.sisacadepcc.presentation.dto;

import com.application.sisacadepcc.domain.model.CourseSchedule;
import com.application.sisacadepcc.domain.model.Schedule;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public record CourseScheduleSlotResponse(
        Long scheduleId,
        String dayOfWeek,
        String startTime,
        String endTime,
        Long classroomId,
        Integer sequenceOrder
) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static CourseScheduleSlotResponse from(CourseSchedule courseSchedule) {
        if (courseSchedule == null) {
            return null;
        }
        Schedule schedule = courseSchedule.getSchedule();
        if (schedule == null) {
            return null;
        }
        return new CourseScheduleSlotResponse(
                courseSchedule.getId(),
                normalizeDay(schedule.getDayOfWeek()),
                formatTime(schedule.getStartTime()),
                formatTime(schedule.getEndTime()),
                courseSchedule.getClassroomId(),
                courseSchedule.getSequenceOrder()
        );
    }

    private static String formatTime(LocalTime value) {
        return value != null ? TIME_FORMATTER.format(value) : null;
    }

    private static String normalizeDay(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static java.util.List<CourseScheduleSlotResponse> fromList(java.util.List<CourseSchedule> courseSchedules) {
        if (courseSchedules == null || courseSchedules.isEmpty()) {
            return java.util.List.of();
        }
        return courseSchedules.stream()
                .map(CourseScheduleSlotResponse::from)
                .filter(Objects::nonNull)
                .sorted((left, right) -> {
                    if (left.sequenceOrder() != null && right.sequenceOrder() != null) {
                        return Integer.compare(left.sequenceOrder(), right.sequenceOrder());
                    }
                    if (left.sequenceOrder() != null) {
                        return -1;
                    }
                    if (right.sequenceOrder() != null) {
                        return 1;
                    }
                    if (left.dayOfWeek() != null && right.dayOfWeek() != null) {
                        int dayCompare = left.dayOfWeek().compareTo(right.dayOfWeek());
                        if (dayCompare != 0) {
                            return dayCompare;
                        }
                    }
                    if (left.startTime() != null && right.startTime() != null) {
                        return left.startTime().compareTo(right.startTime());
                    }
                    return 0;
                })
                .toList();
    }
}
