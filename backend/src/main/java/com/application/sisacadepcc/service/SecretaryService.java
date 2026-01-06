package com.application.sisacadepcc.service;

import com.application.sisacadepcc.domain.model.Secretary;
import com.application.sisacadepcc.domain.repository.SecretaryRepository;
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
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SecretaryService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final SecretaryRepository repository;

    public SecretaryService(SecretaryRepository repository) {
        this.repository = repository;
    }

    public List<Secretary> getAllSecretaries() {
        return repository.findAll();
    }

    @Transactional
    public Secretary createSecretary(Secretary secretary) {
        if (secretary == null) {
            throw new IllegalArgumentException("Los datos de la secretaria son obligatorios.");
        }

        String firstNames = sanitizeText(secretary.getFirstNames());
        String paternalSurname = sanitizeText(secretary.getPaternalSurname());
        String maternalSurname = sanitizeText(secretary.getMaternalSurname());
        String email = sanitizeEmail(secretary.getInstitutionalEmail());

        if (firstNames.isEmpty()) {
            throw new IllegalArgumentException("Ingresa los nombres de la secretaria.");
        }
        if (paternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido paterno.");
        }
        if (maternalSurname.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el apellido materno.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresa un correo institucional válido.");
        }

        if (repository.existsByInstitutionalEmail(email)) {
            throw new IllegalArgumentException("El correo institucional ya está registrado.");
        }

        Secretary toSave = new Secretary();
        toSave.setFirstNames(firstNames);
        toSave.setPaternalSurname(paternalSurname);
        toSave.setMaternalSurname(maternalSurname);
        toSave.setInstitutionalEmail(email);

        return repository.save(toSave);
    }

    public UserImportResult<Secretary> importSecretaries(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un archivo CSV o Excel con secretarias");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        List<SecretaryImportRow> rawRows;
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

        List<Secretary> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (SecretaryImportRow row : rawRows) {
            if (row == null || row.columns().isEmpty()) {
                continue;
            }

            if (row.rowNumber() == 1 && isHeaderRow(row.columns())) {
                continue;
            }

            processed++;
            Secretary secretary = toSecretary(row, errors);
            if (secretary == null) {
                skipped++;
                continue;
            }

            String email = secretary.getInstitutionalEmail();
            if (email != null && repository.existsByInstitutionalEmail(email)) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": el correo " + email + " ya existe, se omitió.");
                continue;
            }

            try {
                created.add(repository.save(secretary));
            } catch (IllegalArgumentException ex) {
                skipped++;
                errors.add("Fila " + row.rowNumber() + ": " + ex.getMessage());
            }
        }

        return new UserImportResult<>(created, errors, processed, skipped);
    }

    private Secretary toSecretary(SecretaryImportRow row, List<String> errors) {
        List<String> columns = row.columns();
        int line = row.rowNumber();

        String paternalSurname = sanitizeText(valueAt(columns, 0));
        String maternalSurname = sanitizeText(valueAt(columns, 1));
        String firstNames = sanitizeText(valueAt(columns, 2));
        String email = sanitizeEmail(valueAt(columns, 3));

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
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Fila " + line + ": el correo institucional es inválido");
            return null;
        }

        Secretary secretary = new Secretary();
        secretary.setPaternalSurname(paternalSurname);
        secretary.setMaternalSurname(maternalSurname);
        secretary.setFirstNames(firstNames);
        secretary.setInstitutionalEmail(email);
        return secretary;
    }

    private List<SecretaryImportRow> parseCsv(InputStream inputStream) throws IOException {
        List<SecretaryImportRow> rows = new ArrayList<>();
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
                rows.add(new SecretaryImportRow(lineNumber, columns));
            }
        }
        return rows;
    }

    private List<SecretaryImportRow> parseExcel(InputStream inputStream) throws IOException {
        List<SecretaryImportRow> rows = new ArrayList<>();
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
                rows.add(new SecretaryImportRow(row.getRowNum() + 1, columns));
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
        return normalized.contains("apellido") || normalized.contains("secretaria") || normalized.contains("secretario");
    }

    private String valueAt(List<String> columns, int index) {
        if (columns == null || index < 0 || index >= columns.size()) {
            return null;
        }
        String value = columns.get(index);
        return value != null ? value.trim() : null;
    }

    private record SecretaryImportRow(int rowNumber, List<String> columns) {
    }

    private String sanitizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String sanitizeEmail(String value) {
        return sanitizeText(value).toLowerCase();
    }
}
