package com.application.sisacadepcc.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelScheduleService {

    private final Map<String, List<OccupiedTimeSlot>> scheduleData = new HashMap<>();

    // Mapeo de columnas a días de la semana
    private static final Map<Integer, String> COLUMN_TO_DAY;
    static {
        Map<Integer, String> tempMap = new HashMap<>();
        tempMap.put(2, "LUNES");    // Columna C
        tempMap.put(3, "MARTES");   // Columna D  
        tempMap.put(4, "MIERCOLES"); // Columna E
        tempMap.put(5, "JUEVES");   // Columna F
        tempMap.put(6, "VIERNES");   // Columna G
        COLUMN_TO_DAY = Collections.unmodifiableMap(tempMap);
    }

    // Horarios definidos en el Excel
    private static final Map<Integer, String[]> TIME_SLOTS;
    static {
        Map<Integer, String[]> tempMap = new HashMap<>();
        tempMap.put(7, new String[]{"7:00", "7:50"});    // Fila 8
        tempMap.put(8, new String[]{"7:50", "8:40"});    // Fila 9
        tempMap.put(9, new String[]{"8:50", "09:40"});   // Fila 10
        tempMap.put(10, new String[]{"09:40", "10:30"}); // Fila 11
        tempMap.put(11, new String[]{"10:40", "11:30"}); // Fila 12
        tempMap.put(12, new String[]{"11:30", "12:20"}); // Fila 13
        tempMap.put(13, new String[]{"12:20", "13:10"}); // Fila 14
        tempMap.put(14, new String[]{"13:10", "14:00"}); // Fila 15
        tempMap.put(15, new String[]{"14:00", "14:50"}); // Fila 16
        tempMap.put(16, new String[]{"14:50", "15:40"}); // Fila 17
        tempMap.put(17, new String[]{"15:50", "16:40"}); // Fila 18
        tempMap.put(18, new String[]{"16:40", "17:30"}); // Fila 19
        tempMap.put(19, new String[]{"17:40", "18:30"}); // Fila 20
        tempMap.put(20, new String[]{"18:30", "19:20"}); // Fila 21
        tempMap.put(21, new String[]{"19:20", "20:10"}); // Fila 22
        TIME_SLOTS = Collections.unmodifiableMap(tempMap);
    }

    public ExcelScheduleService() {
        loadScheduleData();
    }

    public boolean isTimeSlotOccupied(String classroomName, String dayOfWeek, String startTime, String endTime) {
        String key = normalizeClassroomName(classroomName);
        List<OccupiedTimeSlot> occupiedSlots = scheduleData.get(key);

        if (occupiedSlots == null) {
            return false;
        }

        return occupiedSlots.stream().anyMatch(slot ->
                slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek) &&
                        isTimeOverlap(slot.getStartTime(), slot.getEndTime(), startTime, endTime)
        );
    }

    public List<String> getAvailableClassrooms() {
        return Arrays.asList(
                "AULA 101", "AULA 201", "AULA 202", "AULA 203", "AULA 301", "LAB 01", "LAB 02", "LAB 04"
        );
    }

    public Map<String, List<OccupiedTimeSlot>> getClassroomSchedule(String classroomName) {
        String key = normalizeClassroomName(classroomName);
        Map<String, List<OccupiedTimeSlot>> schedule = new HashMap<>();

        // Días de la semana
        String[] days = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"};

        for (String day : days) {
            List<OccupiedTimeSlot> daySlots = new ArrayList<>();
            List<OccupiedTimeSlot> allSlots = scheduleData.get(key);

            if (allSlots != null) {
                allSlots.stream()
                        .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(day))
                        .forEach(daySlots::add);
            }

            schedule.put(day, daySlots);
        }

        return schedule;
    }

    private void loadScheduleData() {
        try {
            ClassPathResource resource = new ClassPathResource("2025 B HORARIOS - ALUMNOS EPCC.xlsx");
            if (!resource.exists()) {
                System.err.println("Excel file not found in classpath. Creating empty schedule data.");
                return;
            }

            InputStream inputStream = resource.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);

            System.out.println("Processing Excel file with " + workbook.getNumberOfSheets() + " sheets");

            // Procesar cada hoja del Excel
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName().toUpperCase();
                System.out.println("Processing sheet: " + sheetName);

                // Determinar el tipo de aula/lab basado en el nombre de la hoja
                if (sheetName.contains("1ER") || sheetName.contains("2DO") ||
                        sheetName.contains("3ER") || sheetName.contains("4TO") ||
                        sheetName.contains("5TO")) {
                    processClassroomSheet(sheet, sheetName);
                } else if (sheetName.contains("LAB")) {
                    processLabSheet(sheet, sheetName);
                }
            }

            workbook.close();
            inputStream.close();

            System.out.println("Loaded schedule data for classrooms: " + scheduleData.keySet());

        } catch (Exception e) {
            System.err.println("Error loading Excel schedule data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processClassroomSheet(Sheet sheet, String sheetName) {
    String currentClassroom = null;

        for (Row row : sheet) {
            // Buscar filas que definen el aula
            Cell aulaCell = row.getCell(1); // Columna B
            if (aulaCell != null) {
                String cellValue = getCellValue(aulaCell).toUpperCase();

                // Detectar cambio de aula
                if (cellValue.contains("AULA") || cellValue.contains("LAB")) {
                    currentClassroom = extractClassroomName(cellValue);
                    System.out.println("Found classroom: " + currentClassroom + " in sheet " + sheetName);
                }

            }

            // Procesar horarios si tenemos un aula definida
            if (currentClassroom != null && TIME_SLOTS.containsKey(row.getRowNum())) {
                processScheduleRow(row, currentClassroom, sheetName);
            }
        }
    }

    private void processLabSheet(Sheet sheet, String sheetName) {
        String labName = "LAB " + sheetName.replace("LAB", "").trim();
        System.out.println("Processing lab: " + labName);

        for (Row row : sheet) {
            if (TIME_SLOTS.containsKey(row.getRowNum())) {
                processScheduleRow(row, labName, sheetName);
            }
        }
    }

    private void processScheduleRow(Row row, String classroomName, String sheetName) {
        String[] times = TIME_SLOTS.get(row.getRowNum());
        if (times == null) return;

        String startTime = times[0];
        String endTime = times[1];

        // Procesar cada día de la semana (columnas C-G)
        for (Map.Entry<Integer, String> entry : COLUMN_TO_DAY.entrySet()) {
            int columnIndex = entry.getKey();
            String dayOfWeek = entry.getValue();

            Cell cell = row.getCell(columnIndex);
            if (cell != null) {
                String courseInfo = getCellValue(cell).trim();
                if (!courseInfo.isEmpty() && !courseInfo.equals("-")) {
                    // Extraer nombre del curso (limpiar el texto)
                    String courseName = courseInfo;

                    OccupiedTimeSlot slot = new OccupiedTimeSlot(
                            classroomName, dayOfWeek, startTime, endTime, courseName
                    );

                    String key = normalizeClassroomName(classroomName);
                    scheduleData.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);

                    System.out.println("Added slot: " + classroomName + " " + dayOfWeek + " " +
                            startTime + "-" + endTime + " - " + courseName);
                }
            }
        }
    }

    private String extractClassroomName(String cellValue) {
        // Extraer nombre del aula del texto
        if (cellValue.contains("AULA")) {
            String[] parts = cellValue.split("AULA");
            if (parts.length > 1) {
                return "AULA " + parts[1].trim().replace("/", "").replace(" ", "");
            }
        } else if (cellValue.contains("LAB")) {
            String[] parts = cellValue.split("LAB");
            if (parts.length > 1) {
                return "LAB " + parts[1].trim();
            }
        }
        return cellValue.trim();
    }
    
    private String normalizeClassroomName(String classroomName) {
        // Normalizar nombres de aulas/labs
        String normalized = classroomName.toUpperCase()
                .replace("LAB 1", "LAB 01")
                .replace("LAB 2", "LAB 02")
                .replace("LAB 3", "LAB 04") // Según tu estructura
                .replace("LAB 4", "LAB 04")
                .trim();

        return normalized;
    }

    public List<OccupiedTimeSlot> findByCourseName(String courseName) {
        if (courseName == null || courseName.isBlank()) {
            return List.of();
        }

        String normalizedCourseName = normalizeCourseName(courseName);

        return scheduleData.values().stream()
                .flatMap(List::stream)
                .filter(slot -> {
                    String slotName = normalizeCourseName(slot.getCourseName());
                    return slotName.contains(normalizedCourseName);
                })
                .collect(Collectors.toList());
    }

    public List<OccupiedTimeSlot> findByCourse(String courseName, String groupLetter,
                                               com.application.sisacadepcc.domain.model.valueobject.CourseType courseType) {
        List<OccupiedTimeSlot> base = findByCourseName(courseName);
        if (base.isEmpty()) return base;

        return base.stream()
                .filter(slot -> {
                    boolean groupOk = true;
                    if (groupLetter != null && !groupLetter.isBlank()) {
                        groupOk = groupLetter.equalsIgnoreCase(Optional.ofNullable(slot.getGroupLetter()).orElse(""));
                    }

                    boolean typeOk = true;
                    if (courseType != null) {
                        if (courseType == com.application.sisacadepcc.domain.model.valueobject.CourseType.LAB) {
                            typeOk = slot.isLab();
                        } else {
                            // Treat non-lab (teoría/práctica) as THEORY in domain
                            typeOk = !slot.isLab();
                        }
                    }

                    return groupOk && typeOk;
                })
                .collect(Collectors.toList());
    }

    private String normalizeCourseName(String value) {
        if (value == null) {
            return "";
        }
        return value
                .toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        // Normalizar formatos de tiempo
        String normStart1 = normalizeTime(start1);
        String normEnd1 = normalizeTime(end1);
        String normStart2 = normalizeTime(start2);
        String normEnd2 = normalizeTime(end2);

        return normStart1.compareTo(normEnd2) < 0 && normEnd1.compareTo(normStart2) > 0;
    }

    private String normalizeTime(String time) {
        // Asegurar formato HH:MM
        if (time.length() == 4) { // H:MM
            return "0" + time;
        }
        return time.length() == 5 ? time : time + ":00";
    }

    // Método para debug - ver todos los horarios cargados
    public void printAllSchedules() {
        for (Map.Entry<String, List<OccupiedTimeSlot>> entry : scheduleData.entrySet()) {
            System.out.println("=== " + entry.getKey() + " ===");
            for (OccupiedTimeSlot slot : entry.getValue()) {
                System.out.println(slot.getDayOfWeek() + " " + slot.getStartTime() +
                        "-" + slot.getEndTime() + " - " + slot.getCourseName());
            }
        }
    }

    public static class OccupiedTimeSlot {
        private String classroomName;
        private String dayOfWeek;
        private String startTime;
        private String endTime;
        private String courseName;
        private String groupLetter; // A, B, C ... if detected from text
        private boolean lab; // true if LAB/LABORATORIO detected

        public OccupiedTimeSlot(String classroomName, String dayOfWeek, String startTime, String endTime, String courseName) {
            this.classroomName = classroomName;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.courseName = courseName;
            parseHintsFromCourseText(courseName);
        }

        // Getters
        public String getClassroomName() { return classroomName; }
        public String getDayOfWeek() { return dayOfWeek; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getCourseName() { return courseName; }
        public String getGroupLetter() { return groupLetter; }
        public boolean isLab() { return lab; }

        @Override
        public String toString() {
            return String.format("%s %s %s-%s (%s)", classroomName, dayOfWeek, startTime, endTime, courseName);
        }

        private void parseHintsFromCourseText(String text) {
            if (text == null) {
                this.groupLetter = null;
                this.lab = false;
                return;
            }

            String upper = text.toUpperCase(Locale.ROOT);
            // detect lab markers
            this.lab = upper.contains("LABORATORIO") || upper.matches(".*\\bLAB\\b.*");

            // group detection: (A) or GRUPO A or TEORIA A / PRACTICA A / LAB A
            String detected = null;

            java.util.regex.Pattern pParen = java.util.regex.Pattern.compile(".*\\(([A-F])\\).*", java.util.regex.Pattern.UNICODE_CASE);
            java.util.regex.Matcher m1 = pParen.matcher(upper);
            if (m1.matches()) {
                detected = m1.group(1);
            }

            if (detected == null) {
                java.util.regex.Pattern pGrupo = java.util.regex.Pattern.compile(".*\\bGRUPO\\s+([A-F])\\b.*");
                java.util.regex.Matcher m2 = pGrupo.matcher(upper);
                if (m2.matches()) {
                    detected = m2.group(1);
                }
            }

            if (detected == null) {
                java.util.regex.Pattern pSuffix = java.util.regex.Pattern.compile(".*\\b(TEORIA|PRACTICA|LAB(?:ORATORIO)?)\\s*([A-F])\\b.*");
                java.util.regex.Matcher m3 = pSuffix.matcher(upper);
                if (m3.matches()) {
                    detected = m3.group(2);
                }
            }

            this.groupLetter = detected;
        }
    }
}
