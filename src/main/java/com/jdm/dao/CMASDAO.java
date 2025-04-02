package com.jdm.dao;

import com.jdm.models.CMAS;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for CMAS
 */
public class CMASDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    
    /**
     * Get all CMAS entries for a patient
     * 
     * @param patientId Patient ID
     * @return List of CMAS entries
     * @throws SQLException if database operation fails
     */
    public List<CMAS> getAllForPatient(String patientId) throws SQLException {
        String sql = "SELECT id, date, category, value, patient_id FROM cmas WHERE patient_id = ? ORDER BY date";
        List<CMAS> cmasEntries = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CMAS cmas = new CMAS(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getInt("value"),
                        rs.getString("patient_id")
                    );
                    cmasEntries.add(cmas);
                }
            }
        }
        
        return cmasEntries;
    }
    
    /**
     * Get CMAS entry by ID
     * 
     * @param id CMAS ID
     * @return CMAS entry if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public CMAS getById(int id) throws SQLException {
        String sql = "SELECT id, date, category, value, patient_id FROM cmas WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CMAS(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getInt("value"),
                        rs.getString("patient_id")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get CMAS entries by category for a patient
     * 
     * @param patientId Patient ID
     * @param category CMAS category
     * @return List of CMAS entries
     * @throws SQLException if database operation fails
     */
    public List<CMAS> getByCategory(String patientId, String category) throws SQLException {
        String sql = "SELECT id, date, category, value, patient_id FROM cmas " +
                    "WHERE patient_id = ? AND category = ? ORDER BY date";
        List<CMAS> cmasEntries = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CMAS cmas = new CMAS(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getInt("value"),
                        rs.getString("patient_id")
                    );
                    cmasEntries.add(cmas);
                }
            }
        }
        
        return cmasEntries;
    }
    
    /**
     * Get CMAS entries grouped by category for a patient
     * 
     * @param patientId Patient ID
     * @return Map of category to list of CMAS entries
     * @throws SQLException if database operation fails
     */
    public Map<String, List<CMAS>> getGroupedByCategory(String patientId) throws SQLException {
        String sql = "SELECT id, date, category, value, patient_id FROM cmas " +
                    "WHERE patient_id = ? ORDER BY category, date";
        Map<String, List<CMAS>> groupedEntries = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CMAS cmas = new CMAS(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getInt("value"),
                        rs.getString("patient_id")
                    );
                    
                    String category = cmas.getCategory();
                    if (!groupedEntries.containsKey(category)) {
                        groupedEntries.put(category, new ArrayList<>());
                    }
                    
                    groupedEntries.get(category).add(cmas);
                }
            }
        }
        
        return groupedEntries;
    }
    
    /**
     * Get CMAS trends (average value per month) for a patient
     * 
     * @param patientId Patient ID
     * @param category CMAS category
     * @return List of maps containing month and average value
     * @throws SQLException if database operation fails
     */
    public List<Map<String, Object>> getTrendsByMonth(String patientId, String category) throws SQLException {
        String sql = "SELECT strftime('%Y-%m', date) as month, AVG(value) as avg_value " +
                    "FROM cmas WHERE patient_id = ? AND category = ? " +
                    "GROUP BY strftime('%Y-%m', date) ORDER BY month";
        List<Map<String, Object>> trends = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("month", rs.getString("month"));
                    trend.put("avgValue", rs.getDouble("avg_value"));
                    trends.add(trend);
                }
            }
        }
        
        return trends;
    }
    
    /**
     * Insert a CMAS entry
     * 
     * @param cmas CMAS entry to insert
     * @return ID of the inserted entry
     * @throws SQLException if database operation fails
     */
    public int insert(CMAS cmas) throws SQLException {
        String sql = "INSERT INTO cmas (date, category, value, patient_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, cmas.getDate().toString());
            stmt.setString(2, cmas.getCategory());
            stmt.setInt(3, cmas.getValue());
            stmt.setString(4, cmas.getPatientId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating CMAS entry failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating CMAS entry failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Insert multiple CMAS entries in a batch
     * 
     * @param cmasEntries List of CMAS entries to insert
     * @param conn Database connection
     * @throws SQLException if database operation fails
     */
    public void insertBatch(List<CMAS> cmasEntries, Connection conn) throws SQLException {
        String sql = "INSERT INTO cmas (date, category, value, patient_id) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CMAS cmas : cmasEntries) {
                stmt.setString(1, cmas.getDate().toString());
                stmt.setString(2, cmas.getCategory());
                stmt.setInt(3, cmas.getValue());
                stmt.setString(4, cmas.getPatientId());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Update a CMAS entry
     * 
     * @param cmas CMAS entry to update
     * @throws SQLException if database operation fails
     */
    public void update(CMAS cmas) throws SQLException {
        String sql = "UPDATE cmas SET date = ?, category = ?, value = ?, patient_id = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cmas.getDate().toString());
            stmt.setString(2, cmas.getCategory());
            stmt.setInt(3, cmas.getValue());
            stmt.setString(4, cmas.getPatientId());
            stmt.setInt(5, cmas.getId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a CMAS entry
     * 
     * @param id CMAS ID to delete
     * @throws SQLException if database operation fails
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM cmas WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get CMAS statistics for a patient (min, max, avg)
     * 
     * @param patientId Patient ID
     * @param category CMAS category
     * @return Map containing statistics
     * @throws SQLException if database operation fails
     */
    public Map<String, Object> getStatistics(String patientId, String category) throws SQLException {
        String sql = "SELECT MIN(value) as min_value, MAX(value) as max_value, AVG(value) as avg_value " +
                    "FROM cmas WHERE patient_id = ? AND category = ?";
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("minValue", rs.getInt("min_value"));
                    stats.put("maxValue", rs.getInt("max_value"));
                    stats.put("avgValue", rs.getDouble("avg_value"));
                }
            }
        }
        
        return stats;
    }
    
    /**
     * Get all CMAS entries for all patients
     * 
     * @return List of CMAS entries
     * @throws SQLException if database operation fails
     */
    public List<CMAS> getAll() throws SQLException {
        String sql = "SELECT id, date, category, value, patient_id FROM cmas ORDER BY patient_id, date";
        List<CMAS> cmasEntries = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CMAS cmas = new CMAS(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getInt("value"),
                        rs.getString("patient_id")
                    );
                    cmasEntries.add(cmas);
                }
            }
        }
        
        return cmasEntries;
    }
    
    /**
     * Get CMAS entries grouped by category for all patients
     * 
     * @return Map of category to list of CMAS entries
     * @throws SQLException if database operation fails
     */
    public Map<String, List<CMAS>> getAllGroupedByCategory() throws SQLException {
        String sql = "SELECT id, date, category, value, patient_id FROM cmas ORDER BY category, date";
        Map<String, List<CMAS>> groupedEntries = new HashMap<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CMAS cmas = new CMAS(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getInt("value"),
                        rs.getString("patient_id")
                    );
                    
                    String category = cmas.getCategory();
                    if (!groupedEntries.containsKey(category)) {
                        groupedEntries.put(category, new ArrayList<>());
                    }
                    
                    groupedEntries.get(category).add(cmas);
                }
            }
        }
        
        return groupedEntries;
    }
}
