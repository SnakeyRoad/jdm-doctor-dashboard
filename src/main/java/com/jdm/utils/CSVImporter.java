package com.jdm.utils;

import com.jdm.dao.*;
import com.jdm.models.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for importing CSV data into the database
 */
public class CSVImporter {
    private final DatabaseManager dbManager;
    private final PatientDAO patientDAO;
    private final CMASDAO cmasDAO;
    private final LabResultGroupDAO labResultGroupDAO;
    private final LabResultDAO labResultDAO;
    private final MeasurementDAO measurementDAO;

    /**
     * Constructor
     */
    public CSVImporter() {
        this.dbManager = DatabaseManager.getInstance();
        this.patientDAO = new PatientDAO();
        this.cmasDAO = new CMASDAO();
        this.labResultGroupDAO = new LabResultGroupDAO();
        this.labResultDAO = new LabResultDAO();
        this.measurementDAO = new MeasurementDAO();
    }

    /**
     * Import all CSV files into the database
     * 
     * @param patientFile Path to Patient.csv
     * @param cmasFile Path to CMAS.csv
     * @param labResultGroupFile Path to LabResultGroup.csv
     * @param labResultFile Path to LabResult.csv
     * @param labResultsENFile Path to LabResultsEN.csv
     * @param measurementFile Path to Measurement.csv
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    public void importAllData(String patientFile, String cmasFile, String labResultGroupFile,
                             String labResultFile, String labResultsENFile, String measurementFile) 
            throws IOException, SQLException {
        
        // Create a database connection that will be shared by all import methods
        Connection conn = null;
        try {
            // Initialize database if it doesn't exist
            if (!dbManager.databaseExists()) {
                dbManager.initializeDatabase();
            }
            
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Import all data in order to maintain foreign key relationships
            importPatients(patientFile, conn);
            importCMAS(cmasFile, conn);
            importLabResultGroups(labResultGroupFile, conn);
            importLabResults(labResultFile, labResultsENFile, conn);
            importMeasurements(measurementFile, conn);
            
            conn.commit(); // Commit transaction
            System.out.println("Data import completed successfully");
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Import patients from CSV
     * 
     * @param patientFile Path to Patient.csv
     * @param conn Database connection
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    private void importPatients(String patientFile, Connection conn) throws IOException, SQLException {
        try (Reader reader = new FileReader(patientFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<Patient> patients = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                String patientId = record.get("PatientID");
                String name = record.get("Name");
                
                patients.add(new Patient(patientId, name));
            }
            
            patientDAO.insertBatch(patients, conn);
            System.out.println("Imported " + patients.size() + " patients");
        }
    }

    /**
     * Import CMAS data from CSV
     * 
     * @param cmasFile Path to CMAS.csv
     * @param conn Database connection
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    private void importCMAS(String cmasFile, Connection conn) throws IOException, SQLException {
        try (Reader reader = new FileReader(cmasFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<CMAS> cmasEntries = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                // Get the first patient as default since Patient ID is not in CMAS.csv
                Patient patient = patientDAO.getFirst(conn);
                if (patient == null) {
                    throw new SQLException("No patient found in database");
                }
                
                String dateStr = cleanupDateString(record.get("Date"));
                LocalDate date = parseDate(dateStr);
                
                String category = cleanupCMASCategory(record.get("Category"));
                int value = Integer.parseInt(record.get("Value"));
                
                cmasEntries.add(new CMAS(date, category, value, patient.getPatientId()));
            }
            
            cmasDAO.insertBatch(cmasEntries, conn);
            System.out.println("Imported " + cmasEntries.size() + " CMAS entries");
        }
    }
    
    /**
     * Import lab result groups from CSV
     * 
     * @param labResultGroupFile Path to LabResultGroup.csv
     * @param conn Database connection
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    private void importLabResultGroups(String labResultGroupFile, Connection conn) throws IOException, SQLException {
        try (Reader reader = new FileReader(labResultGroupFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<LabResultGroup> groups = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                String groupId = record.get("LabResultGroupID");
                String groupName = record.get("GroupName");
                
                groups.add(new LabResultGroup(groupId, groupName.trim()));
            }
            
            labResultGroupDAO.insertBatch(groups, conn);
            System.out.println("Imported " + groups.size() + " lab result groups");
        }
    }
    
    /**
     * Import lab results from CSV
     * 
     * @param labResultFile Path to LabResult.csv
     * @param labResultsENFile Path to LabResultsEN.csv (with English names)
     * @param conn Database connection
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    private void importLabResults(String labResultFile, String labResultsENFile, Connection conn) 
            throws IOException, SQLException {
        
        // Get the first patient as default since we want all lab results to be associated with them
        Patient patient = patientDAO.getFirst(conn);
        if (patient == null) {
            throw new SQLException("No patient found in database");
        }
        
        // First, load the English names for later reference
        Map<String, String> englishNames = new HashMap<>();
        try (Reader reader = new FileReader(labResultsENFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                String labResultId = record.get("LabResultID");
                String resultNameEnglish = record.get("ResultName_English");
                englishNames.put(labResultId, resultNameEnglish);
            }
        }
        
        // Now import the main lab results
        try (Reader reader = new FileReader(labResultFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<LabResult> labResults = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                String labResultId = record.get("LabResultID");
                String labResultGroupId = record.get("LabResultGroupID");
                String resultName = record.get("ResultName");
                String unit = record.get("Unit");
                
                // Get English name if available
                String resultNameEnglish = englishNames.get(labResultId);
                
                // Use the first patient's ID for all lab results
                labResults.add(new LabResult(labResultId, labResultGroupId, patient.getPatientId(), resultName, unit, resultNameEnglish));
            }
            
            labResultDAO.insertBatch(labResults, conn);
            System.out.println("Imported " + labResults.size() + " lab results");
        }
    }
    
    /**
     * Import measurements from CSV
     * 
     * @param measurementFile Path to Measurement.csv
     * @param conn Database connection
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    private void importMeasurements(String measurementFile, Connection conn) throws IOException, SQLException {
        try (Reader reader = new FileReader(measurementFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            List<Measurement> measurements = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                String measurementId = record.get("MeasurementID");
                String labResultId = record.get("LabResultID");
                String dateTimeStr = record.get("DateTime");
                LocalDateTime dateTime = parseMeasurementDateTime(dateTimeStr);
                String value = record.get("Value");
                
                measurements.add(new Measurement(measurementId, labResultId, dateTime, value.trim()));
            }
            
            measurementDAO.insertBatch(measurements, conn);
            System.out.println("Imported " + measurements.size() + " measurements");
        }
    }
    
    /**
     * Clean up date string from CMAS.csv which may contain special characters
     * 
     * @param dateStr Raw date string
     * @return Cleaned date string
     */
    private String cleanupDateString(String dateStr) {
        // Replace special encoding with normal characters
        return dateStr.replace("+AC0-", "-");
    }
    
    /**
     * Clean up CMAS category which may contain special characters
     * 
     * @param category Raw category string
     * @return Cleaned category string
     */
    private String cleanupCMASCategory(String category) {
        // Replace special encoding with normal characters
        return category.replace("+AD4-", " ").replace("+AC0-", "-");
    }
    
    /**
     * Parse date string from CSV to LocalDate
     * 
     * @param dateStr Date string (expected format: DD-MM-YYYY)
     * @return LocalDate object
     */
    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            // Try alternate format
            try {
                DateTimeFormatter alternateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(dateStr, alternateFormatter);
            } catch (DateTimeParseException ex) {
                System.err.println("Error parsing date: " + dateStr);
                // Return a default date as fallback
                return LocalDate.now();
            }
        }
    }
    
    /**
     * Parse date-time string from measurements CSV to LocalDateTime
     * 
     * @param dateTimeStr Date-time string (expected format: dd-MM-yyyy HH:mm)
     * @return LocalDateTime object
     */
    private LocalDateTime parseMeasurementDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date-time: " + dateTimeStr);
            // Return a default date-time as fallback
            return LocalDateTime.now();
        }
    }
}
