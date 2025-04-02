package com.jdm.dao;

import com.jdm.models.LabResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for LabResult
 */
public class LabResultDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    
    /**
     * Get all lab results for a patient
     * 
     * @param patientId Patient ID
     * @return List of lab results
     * @throws SQLException if database operation fails
     */
    public List<LabResult> getAllForPatient(String patientId) throws SQLException {
        String sql = "SELECT lab_result_id, lab_result_group_id, patient_id, result_name, unit, result_name_english " +
                    "FROM lab_result WHERE patient_id = ?";
        List<LabResult> results = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LabResult result = new LabResult(
                        rs.getString("lab_result_id"),
                        rs.getString("lab_result_group_id"),
                        rs.getString("patient_id"),
                        rs.getString("result_name"),
                        rs.getString("unit"),
                        rs.getString("result_name_english")
                    );
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Get lab result by ID
     * 
     * @param labResultId Lab result ID
     * @return LabResult object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public LabResult getById(String labResultId) throws SQLException {
        String sql = "SELECT lab_result_id, lab_result_group_id, patient_id, result_name, unit, result_name_english " +
                    "FROM lab_result WHERE lab_result_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, labResultId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new LabResult(
                        rs.getString("lab_result_id"),
                        rs.getString("lab_result_group_id"),
                        rs.getString("patient_id"),
                        rs.getString("result_name"),
                        rs.getString("unit"),
                        rs.getString("result_name_english")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get lab results by group for a patient
     * 
     * @param patientId Patient ID
     * @param groupId Lab result group ID
     * @return List of lab results
     * @throws SQLException if database operation fails
     */
    public List<LabResult> getByGroup(String patientId, String groupId) throws SQLException {
        String sql = "SELECT lab_result_id, lab_result_group_id, patient_id, result_name, unit, result_name_english " +
                    "FROM lab_result WHERE patient_id = ? AND lab_result_group_id = ?";
        List<LabResult> results = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, groupId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LabResult result = new LabResult(
                        rs.getString("lab_result_id"),
                        rs.getString("lab_result_group_id"),
                        rs.getString("patient_id"),
                        rs.getString("result_name"),
                        rs.getString("unit"),
                        rs.getString("result_name_english")
                    );
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Get lab results grouped by group for a patient
     * 
     * @param patientId Patient ID
     * @return Map of group ID to list of lab results
     * @throws SQLException if database operation fails
     */
    public Map<String, List<LabResult>> getGrouped(String patientId) throws SQLException {
        String sql = "SELECT lr.lab_result_id, lr.lab_result_group_id, lr.patient_id, lr.result_name, lr.unit, " +
                    "lr.result_name_english, lrg.group_name " +
                    "FROM lab_result lr " +
                    "JOIN lab_result_group lrg ON lr.lab_result_group_id = lrg.lab_result_group_id " +
                    "WHERE lr.patient_id = ? " +
                    "ORDER BY lrg.group_name, lr.result_name";
        Map<String, List<LabResult>> groupedResults = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LabResult result = new LabResult(
                        rs.getString("lab_result_id"),
                        rs.getString("lab_result_group_id"),
                        rs.getString("patient_id"),
                        rs.getString("result_name"),
                        rs.getString("unit"),
                        rs.getString("result_name_english")
                    );
                    
                    String groupId = result.getLabResultGroupId();
                    if (!groupedResults.containsKey(groupId)) {
                        groupedResults.put(groupId, new ArrayList<>());
                    }
                    
                    groupedResults.get(groupId).add(result);
                }
            }
        }
        
        return groupedResults;
    }
    
    /**
     * Insert a lab result
     * 
     * @param labResult LabResult to insert
     * @throws SQLException if database operation fails
     */
    public void insert(LabResult labResult) throws SQLException {
        String sql = "INSERT INTO lab_result (lab_result_id, lab_result_group_id, patient_id, result_name, unit, result_name_english) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, labResult.getLabResultId());
            stmt.setString(2, labResult.getLabResultGroupId());
            stmt.setString(3, labResult.getPatientId());
            stmt.setString(4, labResult.getResultName());
            stmt.setString(5, labResult.getUnit());
            stmt.setString(6, labResult.getResultNameEnglish());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insert multiple lab results in a batch
     * 
     * @param labResults List of LabResult objects to insert
     * @param conn Database connection
     * @throws SQLException if database operation fails
     */
    public void insertBatch(List<LabResult> labResults, Connection conn) throws SQLException {
        String sql = "INSERT INTO lab_result (lab_result_id, lab_result_group_id, patient_id, result_name, unit, result_name_english) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (LabResult labResult : labResults) {
                stmt.setString(1, labResult.getLabResultId());
                stmt.setString(2, labResult.getLabResultGroupId());
                stmt.setString(3, labResult.getPatientId());
                stmt.setString(4, labResult.getResultName());
                stmt.setString(5, labResult.getUnit());
                stmt.setString(6, labResult.getResultNameEnglish());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Update a lab result
     * 
     * @param labResult LabResult to update
     * @throws SQLException if database operation fails
     */
    public void update(LabResult labResult) throws SQLException {
        String sql = "UPDATE lab_result SET lab_result_group_id = ?, patient_id = ?, result_name = ?, " +
                    "unit = ?, result_name_english = ? WHERE lab_result_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, labResult.getLabResultGroupId());
            stmt.setString(2, labResult.getPatientId());
            stmt.setString(3, labResult.getResultName());
            stmt.setString(4, labResult.getUnit());
            stmt.setString(5, labResult.getResultNameEnglish());
            stmt.setString(6, labResult.getLabResultId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a lab result
     * 
     * @param labResultId Lab result ID to delete
     * @throws SQLException if database operation fails
     */
    public void delete(String labResultId) throws SQLException {
        String sql = "DELETE FROM lab_result WHERE lab_result_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, labResultId);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get all lab results from all patients
     * 
     * @return List of lab results
     * @throws SQLException if database operation fails
     */
    public List<LabResult> getAll() throws SQLException {
        String sql = "SELECT lab_result_id, lab_result_group_id, patient_id, result_name, unit, result_name_english " +
                    "FROM lab_result ORDER BY patient_id, result_name";
        List<LabResult> results = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LabResult result = new LabResult(
                        rs.getString("lab_result_id"),
                        rs.getString("lab_result_group_id"),
                        rs.getString("patient_id"),
                        rs.getString("result_name"),
                        rs.getString("unit"),
                        rs.getString("result_name_english")
                    );
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Get all lab results grouped by group for all patients
     * 
     * @return Map of group ID to list of lab results
     * @throws SQLException if database operation fails
     */
    public Map<String, List<LabResult>> getAllGrouped() throws SQLException {
        String sql = "SELECT lr.lab_result_id, lr.lab_result_group_id, lr.patient_id, lr.result_name, lr.unit, " +
                    "lr.result_name_english, lrg.group_name " +
                    "FROM lab_result lr " +
                    "JOIN lab_result_group lrg ON lr.lab_result_group_id = lrg.lab_result_group_id " +
                    "ORDER BY lrg.group_name, lr.result_name";
        Map<String, List<LabResult>> groupedResults = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LabResult result = new LabResult(
                        rs.getString("lab_result_id"),
                        rs.getString("lab_result_group_id"),
                        rs.getString("patient_id"),
                        rs.getString("result_name"),
                        rs.getString("unit"),
                        rs.getString("result_name_english")
                    );
                    
                    String groupId = result.getLabResultGroupId();
                    if (!groupedResults.containsKey(groupId)) {
                        groupedResults.put(groupId, new ArrayList<>());
                    }
                    
                    groupedResults.get(groupId).add(result);
                }
            }
        }
        
        return groupedResults;
    }
}
