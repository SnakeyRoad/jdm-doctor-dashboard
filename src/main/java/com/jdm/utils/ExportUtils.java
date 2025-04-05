package com.jdm.utils;

import com.jdm.models.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for exporting data to CSV and Excel
 */
public class ExportUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    /**
     * Export CMAS data to CSV
     * 
     * @param cmasEntries List of CMAS entries
     * @param filePath Path to save the CSV file
     * @throws IOException if file writing fails
     */
    public static void exportCMASToCSV(List<CMAS> cmasEntries, Path filePath) throws IOException {
        String[] headers = {"Date", "Category", "Value"};
        
        try (FileWriter fileWriter = new FileWriter(filePath.toFile());
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, 
                 CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            
            for (CMAS cmas : cmasEntries) {
                csvPrinter.printRecord(
                    cmas.getDate().format(DATE_FORMATTER),
                    cmas.getCategory(),
                    cmas.getValue()
                );
            }
            
            csvPrinter.flush();
        }
    }
    
    /**
     * Export lab results and measurements to CSV
     * 
     * @param labResults List of lab results
     * @param measurementsMap Map of lab result ID to list of measurements
     * @param filePath Path to save the CSV file
     * @throws IOException if file writing fails
     */
    public static void exportLabResultsToCSV(List<LabResult> labResults, 
                                           Map<String, List<Measurement>> measurementsMap,
                                           Path filePath) throws IOException {
        String[] headers = {"Result Name", "Group", "Date", "Value", "Unit"};
        
        try (FileWriter fileWriter = new FileWriter(filePath.toFile());
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, 
                 CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            
            for (LabResult labResult : labResults) {
                String resultName = labResult.getDisplayName();
                String unit = labResult.getUnit();
                
                List<Measurement> measurements = measurementsMap.get(labResult.getLabResultId());
                if (measurements != null) {
                    for (Measurement measurement : measurements) {
                        csvPrinter.printRecord(
                            resultName,
                            labResult.getLabResultGroupId(), // This would ideally be the group name
                            measurement.getDateTime().format(DATE_TIME_FORMATTER),
                            measurement.getValue(),
                            unit
                        );
                    }
                }
            }
            
            csvPrinter.flush();
        }
    }
    
    /**
     * Export patient report to Excel (includes CMAS and lab results)
     * 
     * @param patient Patient (can be null when exporting all patients)
     * @param cmasEntries List of CMAS entries
     * @param cmasByCategory Map of category to list of CMAS entries
     * @param labResults List of lab results
     * @param labResultsByGroup Map of group ID to list of lab results
     * @param measurementsMap Map of lab result ID to list of measurements
     * @param groupNames Map of group ID to group name
     * @param filePath Path to save the Excel file
     * @throws IOException if file writing fails
     */
    public static void exportPatientReportToExcel(Patient patient,
                                               List<CMAS> cmasEntries,
                                               Map<String, List<CMAS>> cmasByCategory,
                                               List<LabResult> labResults,
                                               Map<String, List<LabResult>> labResultsByGroup,
                                               Map<String, List<Measurement>> measurementsMap,
                                               Map<String, String> groupNames,
                                               Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // Create patient info sheet only if patient is not null
            if (patient != null) {
                Sheet patientSheet = workbook.createSheet("Patient Info");
                createPatientInfoSheet(patientSheet, patient, headerStyle);
            }
            
            // Create CMAS sheet
            if (cmasEntries != null && !cmasEntries.isEmpty()) {
                Sheet cmasSheet = workbook.createSheet("CMAS Data");
                createCMASSheet(cmasSheet, cmasEntries, headerStyle, dateStyle, numberStyle);
            }
            
            // Create a sheet for each lab result group
            if (labResultsByGroup != null) {
                for (Map.Entry<String, List<LabResult>> entry : labResultsByGroup.entrySet()) {
                    String groupId = entry.getKey();
                    List<LabResult> groupResults = entry.getValue();
                    
                    String groupName = groupNames.getOrDefault(groupId, "Group " + groupId);
                    Sheet groupSheet = workbook.createSheet(sanitizeSheetName(groupName));
                    
                    createLabResultGroupSheet(groupSheet, groupName, groupResults, measurementsMap, 
                                             headerStyle, dateStyle, numberStyle);
                }
            }
            
            // Auto-size columns for all sheets
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                    sheet.autoSizeColumn(j);
                }
            }
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Create a header cell style
     * 
     * @param workbook Workbook to create the style in
     * @return CellStyle for headers
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Create a date cell style
     * 
     * @param workbook Workbook to create the style in
     * @return CellStyle for dates
     */
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd-mm-yyyy"));
        return style;
    }
    
    /**
     * Create a number cell style
     * 
     * @param workbook Workbook to create the style in
     * @return CellStyle for numbers
     */
    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        return style;
    }
    
    /**
     * Create the patient info sheet
     * 
     * @param sheet Sheet to populate
     * @param patient Patient
     * @param headerStyle Style for headers
     */
    private static void createPatientInfoSheet(Sheet sheet, Patient patient, CellStyle headerStyle) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell patientIdHeader = headerRow.createCell(0);
        patientIdHeader.setCellValue("Patient ID");
        patientIdHeader.setCellStyle(headerStyle);
        
        Cell nameHeader = headerRow.createCell(1);
        nameHeader.setCellValue("Name");
        nameHeader.setCellStyle(headerStyle);
        
        // Create data row
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(patient.getPatientId());
        dataRow.createCell(1).setCellValue(patient.getName());
    }
    
    /**
     * Create the CMAS data sheet
     * 
     * @param sheet Sheet to populate
     * @param cmasEntries List of CMAS entries
     * @param headerStyle Style for headers
     * @param dateStyle Style for dates
     * @param numberStyle Style for numbers
     */
    private static void createCMASSheet(Sheet sheet, List<CMAS> cmasEntries, 
                                       CellStyle headerStyle, CellStyle dateStyle, CellStyle numberStyle) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Category", "Value"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        int rowNum = 1;
        for (CMAS cmas : cmasEntries) {
            Row row = sheet.createRow(rowNum++);
            
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(cmas.getDate().toString());
            dateCell.setCellStyle(dateStyle);
            
            row.createCell(1).setCellValue(cmas.getCategory());
            
            Cell valueCell = row.createCell(2);
            valueCell.setCellValue(cmas.getValue());
            valueCell.setCellStyle(numberStyle);
        }
    }
    
    /**
     * Create a lab result group sheet
     * 
     * @param sheet Sheet to populate
     * @param groupName Group name
     * @param labResults List of lab results in this group
     * @param measurementsMap Map of lab result ID to list of measurements
     * @param headerStyle Style for headers
     * @param dateStyle Style for dates
     * @param numberStyle Style for numbers
     */
    private static void createLabResultGroupSheet(Sheet sheet, String groupName, List<LabResult> labResults,
                                                Map<String, List<Measurement>> measurementsMap,
                                                CellStyle headerStyle, CellStyle dateStyle, CellStyle numberStyle) {
        // Create title row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Group: " + groupName);
        titleCell.setCellStyle(headerStyle);
        
        // Create header row
        Row headerRow = sheet.createRow(1);
        String[] headers = {"Result Name", "Date", "Value", "Unit"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        int rowNum = 2;
        for (LabResult labResult : labResults) {
            String resultName = labResult.getDisplayName();
            String unit = labResult.getUnit();
            
            List<Measurement> measurements = measurementsMap.get(labResult.getLabResultId());
            if (measurements != null && !measurements.isEmpty()) {
                for (Measurement measurement : measurements) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(resultName);
                    
                    Cell dateCell = row.createCell(1);
                    dateCell.setCellValue(measurement.getDateTime().toString());
                    dateCell.setCellStyle(dateStyle);
                    
                    Cell valueCell = row.createCell(2);
                    Double numericValue = measurement.getNumericValue();
                    if (numericValue != null) {
                        valueCell.setCellValue(numericValue);
                        valueCell.setCellStyle(numberStyle);
                    } else {
                        valueCell.setCellValue(measurement.getValue());
                    }
                    
                    row.createCell(3).setCellValue(unit);
                }
            }
        }
    }
    
    /**
     * Sanitize a sheet name to make it valid for Excel
     * 
     * @param name Original name
     * @return Sanitized name
     */
    private static String sanitizeSheetName(String name) {
        // Excel sheet names cannot exceed 31 characters
        String sanitized = name.length() > 31 ? name.substring(0, 31) : name;
        
        // Excel sheet names cannot contain these characters: \ / ? * [ ]
        sanitized = sanitized.replaceAll("[\\\\/:?*\\[\\]]", "_");
        
        return sanitized;
    }
}
