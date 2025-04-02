package com.jdm.controllers;

import com.jdm.dao.PatientDAO;
import com.jdm.models.Patient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for patient operations
 */
public class PatientController {
    
    private final PatientDAO patientDAO;
    
    /**
     * Constructor
     */
    public PatientController() {
        this.patientDAO = new PatientDAO();
    }
    
    /**
     * Get all patients
     * 
     * @return List of all patients
     * @throws SQLException if database operation fails
     */
    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAll();
    }
    
    /**
     * Get a patient by ID
     * 
     * @param patientId Patient ID
     * @return Patient object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Patient getPatientById(String patientId) throws SQLException {
        return patientDAO.getById(patientId);
    }
    
    /**
     * Get the first patient (useful for demo)
     * 
     * @return First patient or null if none exists
     * @throws SQLException if database operation fails
     */
    public Patient getFirstPatient() throws SQLException {
        Connection conn = null;
        try {
            conn = com.jdm.dao.DatabaseManager.getInstance().getConnection();
            return patientDAO.getFirst(conn);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Insert a patient
     * 
     * @param patient Patient to insert
     * @throws SQLException if database operation fails
     */
    public void insertPatient(Patient patient) throws SQLException {
        patientDAO.insert(patient);
    }
    
    /**
     * Update a patient
     * 
     * @param patient Patient to update
     * @throws SQLException if database operation fails
     */
    public void updatePatient(Patient patient) throws SQLException {
        patientDAO.update(patient);
    }
    
    /**
     * Delete a patient
     * 
     * @param patientId Patient ID to delete
     * @throws SQLException if database operation fails
     */
    public void deletePatient(String patientId) throws SQLException {
        patientDAO.delete(patientId);
    }
}
