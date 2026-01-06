package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Classroom;
import com.application.sisacadepcc.domain.model.valueobject.Place;
import com.application.sisacadepcc.domain.repository.ClassroomRepository;
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

@Service
public class ClassroomService {

    private final ClassroomRepository repository;

    public ClassroomService(ClassroomRepository repository) {
        this.repository = repository;
    }

    public List<Classroom> getAllClassrooms() {
        return repository.findAll();
    }

    public Classroom createClassroom(Classroom classroom) {
        return repository.save(classroom);
    }

    public UserImportResult<Classroom> importClassrooms(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un archivo CSV o Excel con aulas");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        List<ClassroomImportRow> rawRows;
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

        List<Classroom> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (ClassroomImportRow row : rawRows) {
            if (row == null || row.columns().isEmpty()) {
                continue;
            }

            if (row.rowNumber() == 1 && isHeaderRow(row.columns())) {
                continue;
            }

            processed++;
            Classroom classroom = toClassroom(row, errors);
            if (classroom == null) {
                skipped++;
                continue;
            }

            try {
                created.add(repository.save(classroom));
            } catch (IllegalArgumentException ex) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": " + ex.getMessage());
            }
        }

        return new UserImportResult<>(created, errors, processed, skipped);
    }

    private Classroom toClassroom(ClassroomImportRow row, List<String> errors) {
        List<String> columns = row.columns();
        int line = row.rowNumber();

        // classroom_id (col 0) se ignora para creación automática
        String building = valueAt(columns, 1);
        Integer capacity = parseInteger(valueAt(columns, 2), "capacidad", line, true, errors);
        String classroomType = valueAt(columns, 3);
        Integer floor = parseInteger(valueAt(columns, 4), "piso", line, false, errors);
        Integer number = parseInteger(valueAt(columns, 5), "numero", line, true, errors);

        String normalizedBuilding = building != null ? building.trim() : "";
        if (normalizedBuilding.isBlank()) {
            errors.add("Fila " + line + ": el edificio es obligatorio");
            return null;
        }
        if (capacity != null && capacity <= 0) {
            errors.add("Fila " + line + ": la capacidad debe ser mayor a cero");
            return null;
        }
        if (number == null) {
            errors.add("Fila " + line + ": el numero del aula es obligatorio");
            return null;
        }

        Place place = new Place(normalizedBuilding, floor, number, capacity, classroomType);
        return new Classroom(place);
    }

    private List<ClassroomImportRow> parseCsv(InputStream inputStream) throws IOException {
        List<ClassroomImportRow> rows = new ArrayList<>();
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
                rows.add(new ClassroomImportRow(lineNumber, columns));
            }
        }
        return rows;
    }

    private List<ClassroomImportRow> parseExcel(InputStream inputStream) throws IOException {
        List<ClassroomImportRow> rows = new ArrayList<>();
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
                rows.add(new ClassroomImportRow(row.getRowNum() + 1, columns));
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
        return normalized.contains("classroom") || normalized.contains("aula") || normalized.contains("id");
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

    private record ClassroomImportRow(int rowNumber, List<String> columns) {
    }
}