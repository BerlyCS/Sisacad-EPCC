package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Student;
import com.application.sisacadepcc.domain.repository.StudentRepository;
import com.application.sisacadepcc.service.dto.UserImportResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class StudentService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_CUI_LENGTH = 6;

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public Optional<Student> getStudentByCui(String cui) {
        if (cui == null || cui.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCui(cui.trim());
    }

    public List<Student> getAllStudentsSorted(String sortBy, String direction) {
        List<Student> students = repository.findAll();

        Comparator<Student> comparator;
        if ("dni".equals(sortBy)) {
            comparator = Comparator.comparing(Student::getUserId, Comparator.nullsLast(Long::compareTo));
        } else if ("cui".equals(sortBy)) {
            comparator = Comparator.comparing(Student::getCui, Comparator.nullsLast(String::compareTo));
        } else if ("name".equals(sortBy)) {
            comparator = Comparator.comparing(s -> {
                String nombres = s.getFirstNames() != null ? s.getFirstNames() : "";
                String apellidoP = s.getPaternalSurname() != null ? s.getPaternalSurname() : "";
                String apellidoM = s.getMaternalSurname() != null ? s.getMaternalSurname() : "";
                return (nombres + " " + apellidoP + " " + apellidoM).trim();
            }, Comparator.nullsLast(String::compareTo));
        } else if ("apellidos".equals(sortBy)) {
            comparator = Comparator.comparing(s -> {
                String apellidoP = s.getPaternalSurname() != null ? s.getPaternalSurname() : "";
                String apellidoM = s.getMaternalSurname() != null ? s.getMaternalSurname() : "";
                return (apellidoP + " " + apellidoM).trim();
            }, Comparator.nullsLast(String::compareTo));
        } else {
            comparator = Comparator.comparing(Student::getUserId, Comparator.nullsLast(Long::compareTo));
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return students.stream().sorted(comparator).toList();
    }

    @Transactional
    public Student createStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Los datos del estudiante son obligatorios.");
        }

        String firstNames = sanitizeText(student.getFirstNames());
        String paternalSurname = sanitizeText(student.getPaternalSurname());
        String maternalSurname = sanitizeText(student.getMaternalSurname());
        String cui = sanitizeDigits(student.getCui());
        String email = sanitizeEmail(student.getInstitutionalEmail());

        if (firstNames.isEmpty()) {
            throw new IllegalArgumentException("Ingresa los nombres del estudiante.");
        }
        if (paternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido paterno.");
        }
        if (maternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido materno.");
        }
        if (cui.length() < MIN_CUI_LENGTH) {
            throw new IllegalArgumentException("El CUI debe tener al menos " + MIN_CUI_LENGTH + " dígitos.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresa un correo institucional válido.");
        }

        if (repository.existsByCorreoInstitucional(email)) {
            throw new IllegalArgumentException("El correo institucional ya está registrado.");
        }

        if (repository.findByCui(cui).isPresent()) {
            throw new IllegalArgumentException("El CUI ya está registrado.");
        }

        student.setFirstNames(firstNames);
        student.setPaternalSurname(paternalSurname);
        student.setMaternalSurname(maternalSurname);
        student.setCui(cui);
        student.setInstitutionalEmail(email);

        return repository.save(student);
    }

    public UserImportResult<Student> importStudents(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un archivo CSV o Excel con estudiantes");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        List<StudentImportRow> rawRows;
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

        List<Student> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (StudentImportRow row : rawRows) {
            if (row == null || row.columns().isEmpty()) {
                continue;
            }

            if (row.rowNumber() == 1 && isHeaderRow(row.columns())) {
                continue;
            }

            processed++;
            Student student = toStudent(row, errors);
            if (student == null) {
                skipped++;
                continue;
            }

            String email = student.getInstitutionalEmail();
            String cui = student.getCui();

            if (email != null && repository.existsByCorreoInstitucional(email)) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": el correo " + email + " ya existe, se omitió.");
                continue;
            }

            if (cui != null && repository.findByCui(cui).isPresent()) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": el CUI " + cui + " ya existe, se omitió.");
                continue;
            }

            try {
                created.add(repository.save(student));
            } catch (IllegalArgumentException ex) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": " + ex.getMessage());
            }
        }

        return new UserImportResult<>(created, errors, processed, skipped);
    }

    private Student toStudent(StudentImportRow row, List<String> errors) {
        List<String> columns = row.columns();
        int line = row.rowNumber();

        String cuiRaw = valueAt(columns, 0);
        String paternalSurname = sanitizeText(valueAt(columns, 1));
        String maternalSurname = sanitizeText(valueAt(columns, 2));
        String firstNames = sanitizeText(valueAt(columns, 3));
        String email = sanitizeEmail(valueAt(columns, 4));
        Integer enrollmentYear = parseInteger(valueAt(columns, 5), "año de ingreso", line, false, errors);

        String cui = sanitizeDigits(cuiRaw);

        if (firstNames.isEmpty()) {
            errors.add("Fila " + line + ": los nombres son obligatorios");
            return null;
        }
        if (paternalSurname.isEmpty()) {
            errors.add("Fila " + line + ": el apellido paterno es obligatorio");
            return null;
        }
        if (maternalSurname.isEmpty()) {
            errors.add("Fila " + line + ": el apellido materno es obligatorio");
            return null;
        }
        if (cui.length() < MIN_CUI_LENGTH) {
            errors.add("Fila " + line + ": el CUI debe tener al menos " + MIN_CUI_LENGTH + " dígitos");
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Fila " + line + ": el correo institucional es inválido");
            return null;
        }

        Student student = new Student();
        student.setCui(cui);
        student.setPaternalSurname(paternalSurname);
        student.setMaternalSurname(maternalSurname);
        student.setFirstNames(firstNames);
        student.setInstitutionalEmail(email);
        student.setEnrollmentYear(enrollmentYear);
        return student;
    }

    private List<StudentImportRow> parseCsv(InputStream inputStream) throws IOException {
        List<StudentImportRow> rows = new ArrayList<>();
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
                rows.add(new StudentImportRow(lineNumber, columns));
            }
        }
        return rows;
    }

    private List<StudentImportRow> parseExcel(InputStream inputStream) throws IOException {
        List<StudentImportRow> rows = new ArrayList<>();
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
                rows.add(new StudentImportRow(row.getRowNum() + 1, columns));
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
        return normalized.contains("cui") || normalized.contains("codigo") || normalized.contains("estudiante");
    }

    private String valueAt(List<String> columns, int index) {
        if (columns == null || index < 0 || index >= columns.size()) {
            return null;
        }
        String value = columns.get(index);
        return value != null ? value.trim() : null;
    }

    private Integer parseInteger(String raw, String fieldName, int rowNumber, boolean required, List<String> errors) {
        if (raw == null || raw.isBlank()) {
            if (required) {
                errors.add("Fila " + rowNumber + ": falta el campo " + fieldName);
            }
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            errors.add("Fila " + rowNumber + ": valor inválido en " + fieldName + " ('" + raw + "')");
            return null;
        }
    }

    private record StudentImportRow(int rowNumber, List<String> columns) {
    }

    private String sanitizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String sanitizeEmail(String value) {
        return sanitizeText(value).toLowerCase();
    }

    private String sanitizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
