package com.jdm.controllers;

import com.jdm.dao.CMASDAO;
import com.jdm.models.CMAS;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controller for CMAS operations
 */
public class CMASController {
    
    private final CMASDAO cmasDAO;
    
    /**
     * Constructor
     */
    public CMASController() {
        this.cmasDAO = new CMASDAO();
    }
    
    /**
     * Get all CMAS entries for a patient
     * 
     * @param patientId Patient ID
     * @return List of CMAS entries
     * @throws SQLException if database operation fails
     */
    public List<CMAS> getAllCMAS(String patientId) throws SQLException {
        return cmasDAO.getAllForPatient(patientId);
    }
    
    /**
     * Get CMAS entry by ID
     * 
     * @param id CMAS ID
     * @return CMAS entry if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public CMAS getCMASById(int id) throws SQLException {
        return cmasDAO.getById(id);
    }
    
    /**
     * Get CMAS entries by category for a patient
     * 
     * @param patientId Patient ID
     * @param category CMAS category
     * @return List of CMAS entries
     * @throws SQLException if database operation fails
     */
    public List<CMAS> getCMASByCategory(String patientId, String category) throws SQLException {
        return cmasDAO.getByCategory(patientId, category);
    }
    
    /**
     * Get CMAS entries grouped by category for a patient
     * 
     * @param patientId Patient ID
     * @return Map of category to list of CMAS entries
     * @throws SQLException if database operation fails
     */
    public Map<String, List<CMAS>> getCMASGroupedByCategory(String patientId) throws SQLException {
        return cmasDAO.getGroupedByCategory(patientId);
    }
    
    /**
     * Get CMAS trends (average value per month) for a patient
     * 
     * @param patientId Patient ID
     * @param category CMAS category
     * @return List of maps containing month and average value
     * @throws SQLException if database operation fails
     */
    public List<Map<String, Object>> getCMASTrendsByMonth(String patientId, String category) throws SQLException {
        return cmasDAO.getTrendsByMonth(patientId, category);
    }
    
    /**
     * Insert a CMAS entry
     * 
     * @param cmas CMAS entry to insert
     * @return ID of the inserted entry
     * @throws SQLException if database operation fails
     */
    public int insertCMAS(CMAS cmas) throws SQLException {
        return cmasDAO.insert(cmas);
    }
    
    /**
     * Update a CMAS entry
     * 
     * @param cmas CMAS entry to update
     * @throws SQLException if database operation fails
     */
    public void updateCMAS(CMAS cmas) throws SQLException {
        cmasDAO.update(cmas);
    }
    
    /**
     * Delete a CMAS entry
     * 
     * @param id CMAS ID to delete
     * @throws SQLException if database operation fails
     */
    public void deleteCMAS(int id) throws SQLException {
        cmasDAO.delete(id);
    }
    
    /**
     * Get CMAS statistics for a patient (min, max, avg)
     * 
     * @param patientId Patient ID
     * @param category CMAS category
     * @return Map containing statistics
     * @throws SQLException if database operation fails
     */
    public Map<String, Object> getCMASStatistics(String patientId, String category) throws SQLException {
        return cmasDAO.getStatistics(patientId, category);
    }
    
    /**
     * Get all CMAS entries for all patients
     * 
     * @return List of CMAS entries
     * @throws SQLException if database operation fails
     */
    public List<CMAS> getAllCMAS() throws SQLException {
        return cmasDAO.getAll();
    }
    
    /**
     * Get CMAS entries grouped by category for all patients
     * 
     * @return Map of category to list of CMAS entries
     * @throws SQLException if database operation fails
     */
    public Map<String, List<CMAS>> getCMASGroupedByCategory() throws SQLException {
        return cmasDAO.getAllGroupedByCategory();
    }
}
