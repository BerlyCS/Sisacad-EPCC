package com.application.sisacadepcc.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfScheduleParser {

    public static class ScheduleEntry {
        private String day;
        private String startTime;
        private String endTime;
        private String course;
        private String classroom;
        private String professor;

        // Constructor, getters, setters
        public ScheduleEntry(String day, String startTime, String endTime, String course, String classroom, String professor) {
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.course = course;
            this.classroom = classroom;
            this.professor = professor;
        }

        @Override
        public String toString() {
            return String.format("Día: %s, Hora: %s-%s, Curso: %s, Salón: %s, Profesor: %s",
                    day, startTime, endTime, course, classroom, professor);
        }

        // Getters
        public String getDay() { return day; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getCourse() { return course; }
        public String getClassroom() { return classroom; }
        public String getProfessor() { return professor; }
    }

    public List<ScheduleEntry> parseSchedule(InputStream pdfInputStream) throws IOException {
        List<ScheduleEntry> entries = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfInputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Asumir que el PDF tiene líneas como: "Lunes 08:00-10:00 MAT101 Aula 101 Prof. Juan Pérez"
            // Usar regex para extraer
            Pattern pattern = Pattern.compile("(\\w+)\\s+(\\d{2}:\\d{2})-(\\d{2}:\\d{2})\\s+(\\w+)\\s+(\\w+)\\s+(.+)");
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                String day = matcher.group(1);
                String startTime = matcher.group(2);
                String endTime = matcher.group(3);
                String course = matcher.group(4);
                String classroom = matcher.group(5);
                String professor = matcher.group(6).trim();

                entries.add(new ScheduleEntry(day, startTime, endTime, course, classroom, professor));
            }
        }

        return entries;
    }
}