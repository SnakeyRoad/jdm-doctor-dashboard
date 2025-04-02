package com.jdm.dao;

import com.jdm.models.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Patient
 */
public class PatientDAO {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    
    /**
     * Get all patients
     * 
     * @return List of all patients
     * @throws SQLException if database operation fails
     */
    public List<Patient> getAll() throws SQLException {
        String sql = "SELECT patient_id, name FROM patient";
        List<Patient> patients = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getString("patient_id"),
                    rs.getString("name")
                );
                patients.add(patient);
            }
        }
        
        return patients;
    }
    
    /**
     * Get a patient by ID
     * 
     * @param patientId Patient ID
     * @return Patient object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Patient getById(String patientId) throws SQLException {
        String sql = "SELECT patient_id, name FROM patient WHERE patient_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getString("patient_id"),
                        rs.getString("name")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the first patient (useful for the demo data which has only one patient)
     * 
     * @param conn Database connection
     * @return First patient or null if none exists
     * @throws SQLException if database operation fails
     */
    public Patient getFirst(Connection conn) throws SQLException {
        String sql = "SELECT patient_id, name FROM patient LIMIT 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new Patient(
                    rs.getString("patient_id"),
                    rs.getString("name")
                );
            }
        }
        
        return null;
    }
    
    /**
     * Insert a patient
     * 
     * @param patient Patient to insert
     * @throws SQLException if database operation fails
     */
    public void insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patient (patient_id, name) VALUES (?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patient.getPatientId());
            stmt.setString(2, patient.getName());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Insert multiple patients in a batch
     * 
     * @param patients List of patients to insert
     * @param conn Database connection
     * @throws SQLException if database operation fails
     */
    public void insertBatch(List<Patient> patients, Connection conn) throws SQLException {
        String sql = "INSERT INTO patient (patient_id, name) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Patient patient : patients) {
                stmt.setString(1, patient.getPatientId());
                stmt.setString(2, patient.getName());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Update a patient
     * 
     * @param patient Patient to update
     * @throws SQLException if database operation fails
     */
    public void update(Patient patient) throws SQLException {
        String sql = "UPDATE patient SET name = ? WHERE patient_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patient.getName());
            stmt.setString(2, patient.getPatientId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a patient
     * 
     * @param patientId Patient ID to delete
     * @throws SQLException if database operation fails
     */
    public void delete(String patientId) throws SQLException {
        String sql = "DELETE FROM patient WHERE patient_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patientId);
            
            stmt.executeUpdate();
        }
    }
}
