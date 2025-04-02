package com.jdm.dao;

import com.jdm.models.Measurement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Measurement
 */
public class MeasurementDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    
    /**
     * Get all measurements for a lab result
     * 
     * @param labResultId Lab result ID
     * @return List of measurements
     * @throws SQLException if database operation fails
     */
    public List<Measurement> getAllForLabResult(String labResultId) throws SQLException {
        String sql = "SELECT measurement_id, lab_result_id, date_time, value " +
                    "FROM measurement WHERE lab_result_id = ? ORDER BY date_time";
        List<Measurement> measurements = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, labResultId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Measurement measurement = new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                    measurements.add(measurement);
                }
            }
        }
        
        return measurements;
    }
    
    /**
     * Get measurement by ID
     * 
     * @param measurementId Measurement ID
     * @return Measurement object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Measurement getById(String measurementId) throws SQLException {
        String sql = "SELECT measurement_id, lab_result_id, date_time, value " +
                    "FROM measurement WHERE measurement_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, measurementId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get measurements for a patient within a date range
     * 
     * @param patientId Patient ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of measurements
     * @throws SQLException if database operation fails
     */
    public List<Measurement> getForPatientInDateRange(String patientId, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT m.measurement_id, m.lab_result_id, m.date_time, m.value " +
                    "FROM measurement m " +
                    "JOIN lab_result lr ON m.lab_result_id = lr.lab_result_id " +
                    "WHERE lr.patient_id = ? AND m.date_time BETWEEN ? AND ? " +
                    "ORDER BY m.date_time";
        List<Measurement> measurements = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Measurement measurement = new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                    measurements.add(measurement);
                }
            }
        }
        
        return measurements;
    }
    
    /**
     * Get measurements grouped by lab result for a patient
     * 
     * @param patientId Patient ID
     * @return Map of lab result ID to list of measurements
     * @throws SQLException if database operation fails
     */
    public Map<String, List<Measurement>> getGroupedByLabResult(String patientId) throws SQLException {
        String sql = "SELECT m.measurement_id, m.lab_result_id, m.date_time, m.value " +
                    "FROM measurement m " +
                    "JOIN lab_result lr ON m.lab_result_id = lr.lab_result_id " +
                    "WHERE lr.patient_id = ? " +
                    "ORDER BY lr.result_name, m.date_time";
        Map<String, List<Measurement>> groupedMeasurements = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Measurement measurement = new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                    
                    String labResultId = measurement.getLabResultId();
                    if (!groupedMeasurements.containsKey(labResultId)) {
                        groupedMeasurements.put(labResultId, new ArrayList<>());
                    }
                    
                    groupedMeasurements.get(labResultId).add(measurement);
                }
            }
        }
        
        return groupedMeasurements;
    }
    
    /**
     * Get latest measurement for each lab result of a patient
     * 
     * @param patientId Patient ID
     * @return Map of lab result ID to latest measurement
     * @throws SQLException if database operation fails
     */
    public Map<String, Measurement> getLatestForPatient(String patientId) throws SQLException {
        String sql = "SELECT m.measurement_id, m.lab_result_id, m.date_time, m.value " +
                    "FROM measurement m " +
                    "JOIN (SELECT lab_result_id, MAX(date_time) as max_date " +
                          "FROM measurement " +
                          "GROUP BY lab_result_id) latest " +
                    "ON m.lab_result_id = latest.lab_result_id AND m.date_time = latest.max_date " +
                    "JOIN lab_result lr ON m.lab_result_id = lr.lab_result_id " +
                    "WHERE lr.patient_id = ?";
        Map<String, Measurement> latestMeasurements = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Measurement measurement = new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                    
                    latestMeasurements.put(measurement.getLabResultId(), measurement);
                }
            }
        }
        
        return latestMeasurements;
    }
    
    /**
     * Get trend data for a specific lab result
     * 
     * @param labResultId Lab result ID
     * @param limit Maximum number of data points to return (0 for all)
     * @return List of measurements
     * @throws SQLException if database operation fails
     */
    public List<Measurement> getTrend(String labResultId, int limit) throws SQLException {
        String sql = "SELECT measurement_id, lab_result_id, date_time, value " +
                    "FROM measurement WHERE lab_result_id = ? " +
                    "ORDER BY date_time DESC" +
                    (limit > 0 ? " LIMIT ?" : "");
        List<Measurement> measurements = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, labResultId);
            if (limit > 0) {
                stmt.setInt(2, limit);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Measurement measurement = new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                    measurements.add(measurement);
                }
            }
        }
        
        // Reverse to get chronological order
        List<Measurement> chronological = new ArrayList<>(measurements);
        java.util.Collections.reverse(chronological);
        return chronological;
    }
    
    /**
     * Insert a measurement
     * 
     * @param measurement Measurement to insert
     * @throws SQLException if database operation fails
     */
    public void insert(Measurement measurement) throws SQLException {
        String sql = "INSERT INTO measurement (measurement_id, lab_result_id, date_time, value) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, measurement.getMeasurementId());
            stmt.setString(2, measurement.getLabResultId());
            stmt.setString(3, measurement.getDateTime().toString());
            stmt.setString(4, measurement.getValue());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insert multiple measurements in a batch
     * 
     * @param measurements List of Measurement objects to insert
     * @param conn Database connection
     * @throws SQLException if database operation fails
     */
    public void insertBatch(List<Measurement> measurements, Connection conn) throws SQLException {
        String sql = "INSERT INTO measurement (measurement_id, lab_result_id, date_time, value) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Measurement measurement : measurements) {
                stmt.setString(1, measurement.getMeasurementId());
                stmt.setString(2, measurement.getLabResultId());
                stmt.setString(3, measurement.getDateTime().toString());
                stmt.setString(4, measurement.getValue());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Update a measurement
     * 
     * @param measurement Measurement to update
     * @throws SQLException if database operation fails
     */
    public void update(Measurement measurement) throws SQLException {
        String sql = "UPDATE measurement SET lab_result_id = ?, date_time = ?, value = ? " +
                    "WHERE measurement_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, measurement.getLabResultId());
            stmt.setString(2, measurement.getDateTime().toString());
            stmt.setString(3, measurement.getValue());
            stmt.setString(4, measurement.getMeasurementId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a measurement
     * 
     * @param measurementId Measurement ID to delete
     * @throws SQLException if database operation fails
     */
    public void delete(String measurementId) throws SQLException {
        String sql = "DELETE FROM measurement WHERE measurement_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, measurementId);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get all measurements grouped by lab result for all patients
     * 
     * @return Map of lab result ID to list of measurements
     * @throws SQLException if database operation fails
     */
    public Map<String, List<Measurement>> getAllGroupedByLabResult() throws SQLException {
        String sql = "SELECT m.measurement_id, m.lab_result_id, m.date_time, m.value " +
                    "FROM measurement m " +
                    "JOIN lab_result lr ON m.lab_result_id = lr.lab_result_id " +
                    "ORDER BY lr.result_name, m.date_time";
        Map<String, List<Measurement>> groupedMeasurements = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Measurement measurement = new Measurement(
                        rs.getString("measurement_id"),
                        rs.getString("lab_result_id"),
                        LocalDateTime.parse(rs.getString("date_time")),
                        rs.getString("value")
                    );
                    
                    String labResultId = measurement.getLabResultId();
                    if (!groupedMeasurements.containsKey(labResultId)) {
                        groupedMeasurements.put(labResultId, new ArrayList<>());
                    }
                    
                    groupedMeasurements.get(labResultId).add(measurement);
                }
            }
        }
        
        return groupedMeasurements;
    }
}
