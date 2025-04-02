package com.jdm.dao;

import com.jdm.models.LabResultGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for LabResultGroup
 */
public class LabResultGroupDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    
    /**
     * Get all lab result groups
     * 
     * @return List of all lab result groups
     * @throws SQLException if database operation fails
     */
    public List<LabResultGroup> getAll() throws SQLException {
        String sql = "SELECT lab_result_group_id, group_name FROM lab_result_group ORDER BY group_name";
        List<LabResultGroup> groups = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                LabResultGroup group = new LabResultGroup(
                    rs.getString("lab_result_group_id"),
                    rs.getString("group_name")
                );
                groups.add(group);
            }
        }
        
        return groups;
    }
    
    /**
     * Get a lab result group by ID
     * 
     * @param groupId Lab result group ID
     * @return LabResultGroup object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public LabResultGroup getById(String groupId) throws SQLException {
        String sql = "SELECT lab_result_group_id, group_name FROM lab_result_group WHERE lab_result_group_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, groupId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new LabResultGroup(
                        rs.getString("lab_result_group_id"),
                        rs.getString("group_name")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Insert a lab result group
     * 
     * @param group LabResultGroup to insert
     * @throws SQLException if database operation fails
     */
    public void insert(LabResultGroup group) throws SQLException {
        String sql = "INSERT INTO lab_result_group (lab_result_group_id, group_name) VALUES (?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, group.getLabResultGroupId());
            stmt.setString(2, group.getGroupName());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insert multiple lab result groups in a batch
     * 
     * @param groups List of LabResultGroup objects to insert
     * @param conn Database connection
     * @throws SQLException if database operation fails
     */
    public void insertBatch(List<LabResultGroup> groups, Connection conn) throws SQLException {
        String sql = "INSERT INTO lab_result_group (lab_result_group_id, group_name) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (LabResultGroup group : groups) {
                stmt.setString(1, group.getLabResultGroupId());
                stmt.setString(2, group.getGroupName());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Update a lab result group
     * 
     * @param group LabResultGroup to update
     * @throws SQLException if database operation fails
     */
    public void update(LabResultGroup group) throws SQLException {
        String sql = "UPDATE lab_result_group SET group_name = ? WHERE lab_result_group_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, group.getGroupName());
            stmt.setString(2, group.getLabResultGroupId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a lab result group
     * 
     * @param groupId Lab result group ID to delete
     * @throws SQLException if database operation fails
     */
    public void delete(String groupId) throws SQLException {
        String sql = "DELETE FROM lab_result_group WHERE lab_result_group_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, groupId);
            
            stmt.executeUpdate();
        }
    }
}
