package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Course;
import com.application.sisacadepcc.domain.model.CourseGroup;
import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.model.valueobject.CourseType;
import com.application.sisacadepcc.domain.repository.CourseGroupRepository;
import com.application.sisacadepcc.domain.repository.CourseRepository;
import com.application.sisacadepcc.domain.repository.GradeRepository;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.service.dto.UserImportResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class EnrollmentImportService {

    private static final int MAX_GRADE = 20;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final GradeRepository gradeRepository;
    private final StudentCourseService studentCourseService;

    public EnrollmentImportService(StudentRepository studentRepository,
                                   CourseRepository courseRepository,
                                   CourseGroupRepository courseGroupRepository,
                                   GradeRepository gradeRepository,
                                   StudentCourseService studentCourseService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseGroupRepository = courseGroupRepository;
        this.gradeRepository = gradeRepository;
        this.studentCourseService = studentCourseService;
    }

    public UserImportResult<String> importEnrollmentsWithGrades(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un archivo CSV o Excel con matriculas");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        List<EnrollmentImportRow> rawRows;
        try {
            InputStream inputStream = file.getInputStream();
            if (filename.endsWith(".csv")) {
                rawRows = parseCsv(inputStream);
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rawRows = parseExcel(inputStream);
            } else {
                throw new IllegalArgumentException("Formato no soportado. Use CSV o Excel (.xlsx)");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo", ex);
        }

        List<String> successes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (EnrollmentImportRow row : rawRows) {
            if (row == null || row.columns().isEmpty()) {
                continue;
            }

            if (row.rowNumber() == 1 && isHeaderRow(row.columns())) {
                continue;
            }

            processed++;
            try {
                processRow(row, successes, errors);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": " + ex.getMessage());
            } catch (Exception ex) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": error inesperado al procesar la fila");
            }
        }

        return new UserImportResult<>(successes, errors, processed, skipped);
    }

    private void processRow(EnrollmentImportRow row, List<String> successes, List<String> errors) {
        List<String> columns = row.columns();
        int line = row.rowNumber();

        String cuiRaw = valueAt(columns, 0);
        String courseCodeRaw = valueAt(columns, 1);
        String groupLetterRaw = valueAt(columns, 2);
        String cont1Raw = valueAt(columns, 3);
        String cont2Raw = valueAt(columns, 4);
        String exam1Raw = valueAt(columns, 5);
        String exam2Raw = valueAt(columns, 6);

        String cui = sanitizeDigits(cuiRaw);
        if (cui.isEmpty()) {
            throw new IllegalArgumentException("El CUI es obligatorio");
        }

        Integer courseCode = parseInteger(courseCodeRaw, "código de curso");
        if (courseCode == null) {
            throw new IllegalArgumentException("El código de curso es obligatorio");
        }

        String groupLetter = groupLetterRaw != null ? groupLetterRaw.trim().toUpperCase(Locale.ROOT) : "";
        if (groupLetter.isEmpty()) {
            throw new IllegalArgumentException("La letra del grupo es obligatoria");
        }

        Integer cont1 = parseGrade(cont1Raw, "cont1");
        Integer cont2 = parseGrade(cont2Raw, "cont2");
        Integer exam1 = parseGrade(exam1Raw, "exam1");
        Integer exam2 = parseGrade(exam2Raw, "exam2");

        Student student = studentRepository.findByCui(cui)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el estudiante con CUI " + cui));

        Course course = courseRepository.findByCourseCode(courseCode.longValue())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el curso con código " + courseCode));

        List<Long> targetGroupIds = resolveGroupIds(course, groupLetter);
        if (targetGroupIds.isEmpty()) {
            throw new IllegalArgumentException("No hay grupos (teoría/práctica) con la letra " + groupLetter);
        }

        // Matricular usando la lógica existente
        studentCourseService.enrollStudentInCourseGroups(student.getUserId(), targetGroupIds);

        // Registrar notas iniciales
        upsertGrade(student.getUserId(), course.getCourseId(), cont1, cont2, exam1, exam2);

        successes.add("Fila " + line + ": CUI " + cui + " matriculado en curso " + courseCode + " grupo " + groupLetter);
    }

    private List<Long> resolveGroupIds(Course course, String groupLetter) {
        List<CourseGroup> groups = courseGroupRepository.findByCourseId(course.getCourseId());
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        return groups.stream()
                .filter(group -> group.getLetter() != null && group.getLetter().trim().equalsIgnoreCase(groupLetter))
                .filter(group -> group.getType() != CourseType.LAB)
                .map(CourseGroup::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void upsertGrade(Long studentId, Long courseId, Integer cont1, Integer cont2, Integer exam1, Integer exam2) {
        List<Integer> continuous = sanitizeGrades(List.of(cont1, cont2));
        List<Integer> exams = sanitizeGrades(List.of(exam1, exam2));

        com.application.sisacadepcc.domain.model.Grade existing = gradeRepository.findByCourseAndStudent(courseId, studentId)
                .orElse(null);

        com.application.sisacadepcc.domain.model.Grade grade = new com.application.sisacadepcc.domain.model.Grade(
                existing != null ? existing.getGradeID() : null,
                studentId,
                courseId,
                0L,
                continuous,
                exams
        );

        if (existing != null) {
            grade.setVersion(existing.getVersion());
        }

        boolean saved = gradeRepository.save(grade);
        if (!saved) {
            throw new IllegalStateException("No se pudo registrar la nota inicial");
        }
    }

    private List<Integer> sanitizeGrades(List<Integer> raw) {
        List<Integer> sanitized = new ArrayList<>();
        if (raw == null) {
            return sanitized;
        }
        for (Integer value : raw) {
            if (value == null) {
                continue;
            }
            if (value < 0 || value > MAX_GRADE) {
                throw new IllegalArgumentException("Las notas deben estar entre 0 y " + MAX_GRADE);
            }
            sanitized.add(value);
            if (sanitized.size() == 3) {
                break;
            }
        }
        return sanitized;
    }

    private String sanitizeDigits(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    private Integer parseInteger(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor inválido en " + fieldName + " ('" + raw + "')");
        }
    }

    private Integer parseGrade(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 0 || value > MAX_GRADE) {
                throw new IllegalArgumentException("La nota " + fieldName + " debe estar entre 0 y " + MAX_GRADE);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor inválido en " + fieldName + " ('" + raw + "')");
        }
    }

    private List<EnrollmentImportRow> parseCsv(InputStream inputStream) throws IOException {
        List<EnrollmentImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> columns = Arrays.stream(line.split(","))
                        .map(String::trim)
                        .toList();
                rows.add(new EnrollmentImportRow(lineNumber, columns));
            }
        }
        return rows;
    }

    private List<EnrollmentImportRow> parseExcel(InputStream inputStream) throws IOException {
        List<EnrollmentImportRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }

            for (Row row : sheet) {
                if (row == null) {
                    continue;
                }
                int lastCell = Math.max(row.getLastCellNum(), 0);
                List<String> columns = new ArrayList<>();
                for (int i = 0; i < lastCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    columns.add(readCell(cell));
                }
                rows.add(new EnrollmentImportRow(row.getRowNum() + 1, columns));
            }
        }
        return rows;
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.rint(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return Boolean.toString(cell.getBooleanCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getRichStringCellValue().getString().trim();
        }
        return "";
    }

    private boolean isHeaderRow(List<String> columns) {
        String first = valueAt(columns, 0);
        if (first == null) {
            return false;
        }
        String normalized = first.toLowerCase(Locale.ROOT);
        return normalized.contains("cui") || normalized.contains("curso") || normalized.contains("codigo") || normalized.contains("code");
    }

    private String valueAt(List<String> columns, int index) {
        if (columns == null || index < 0 || index >= columns.size()) {
            return null;
        }
        String value = columns.get(index);
        return value != null ? value.trim() : null;
    }

    private record EnrollmentImportRow(int rowNumber, List<String> columns) {
    }
}
