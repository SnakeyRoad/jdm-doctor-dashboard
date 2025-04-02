package com.jdm.controllers;

import com.jdm.dao.LabResultDAO;
import com.jdm.dao.LabResultGroupDAO;
import com.jdm.dao.MeasurementDAO;
import com.jdm.models.LabResult;
import com.jdm.models.LabResultGroup;
import com.jdm.models.Measurement;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for laboratory result operations
 */
public class LabResultController {
    
    private final LabResultDAO labResultDAO;
    private final LabResultGroupDAO labResultGroupDAO;
    private final MeasurementDAO measurementDAO;
    
    /**
     * Constructor
     */
    public LabResultController() {
        this.labResultDAO = new LabResultDAO();
        this.labResultGroupDAO = new LabResultGroupDAO();
        this.measurementDAO = new MeasurementDAO();
    }
    
    /**
     * Get all lab result groups
     * 
     * @return List of lab result groups
     * @throws SQLException if database operation fails
     */
    public List<LabResultGroup> getAllLabResultGroups() throws SQLException {
        return labResultGroupDAO.getAll();
    }
    
    /**
     * Get a map of group IDs to group names
     * 
     * @return Map of group ID to group name
     * @throws SQLException if database operation fails
     */
    public Map<String, String> getGroupNames() throws SQLException {
        List<LabResultGroup> groups = labResultGroupDAO.getAll();
        Map<String, String> groupNames = new HashMap<>();
        
        for (LabResultGroup group : groups) {
            groupNames.put(group.getLabResultGroupId(), group.getGroupName());
        }
        
        return groupNames;
    }
    
    /**
     * Get all lab results for a patient
     * 
     * @param patientId Patient ID
     * @return List of lab results
     * @throws SQLException if database operation fails
     */
    public List<LabResult> getAllLabResults(String patientId) throws SQLException {
        return labResultDAO.getAllForPatient(patientId);
    }
    
    /**
     * Get lab results by group for a patient
     * 
     * @param patientId Patient ID
     * @param groupId Lab result group ID
     * @return List of lab results
     * @throws SQLException if database operation fails
     */
    public List<LabResult> getLabResultsByGroup(String patientId, String groupId) throws SQLException {
        return labResultDAO.getByGroup(patientId, groupId);
    }
    
    /**
     * Get lab results grouped by group for a patient
     * 
     * @param patientId Patient ID
     * @return Map of group ID to list of lab results
     * @throws SQLException if database operation fails
     */
    public Map<String, List<LabResult>> getLabResultsGrouped(String patientId) throws SQLException {
        return labResultDAO.getGrouped(patientId);
    }
    
    /**
     * Get measurements for a lab result
     * 
     * @param labResultId Lab result ID
     * @return List of measurements
     * @throws SQLException if database operation fails
     */
    public List<Measurement> getMeasurements(String labResultId) throws SQLException {
        return measurementDAO.getAllForLabResult(labResultId);
    }
    
    /**
     * Get all measurements for a patient grouped by lab result
     * 
     * @param patientId Patient ID
     * @return Map of lab result ID to list of measurements
     * @throws SQLException if database operation fails
     */
    public Map<String, List<Measurement>> getAllMeasurements(String patientId) throws SQLException {
        return measurementDAO.getGroupedByLabResult(patientId);
    }
    
    /**
     * Get latest measurement for each lab result of a patient
     * 
     * @param patientId Patient ID
     * @return Map of lab result ID to latest measurement
     * @throws SQLException if database operation fails
     */
    public Map<String, Measurement> getLatestMeasurements(String patientId) throws SQLException {
        return measurementDAO.getLatestForPatient(patientId);
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
    public List<Measurement> getMeasurementsInDateRange(String patientId, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return measurementDAO.getForPatientInDateRange(patientId, startDate, endDate);
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
        return measurementDAO.getTrend(labResultId, limit);
    }
    
    /**
     * Insert a lab result
     * 
     * @param labResult Lab result to insert
     * @throws SQLException if database operation fails
     */
    public void insertLabResult(LabResult labResult) throws SQLException {
        labResultDAO.insert(labResult);
    }
    
    /**
     * Update a lab result
     * 
     * @param labResult Lab result to update
     * @throws SQLException if database operation fails
     */
    public void updateLabResult(LabResult labResult) throws SQLException {
        labResultDAO.update(labResult);
    }
    
    /**
     * Delete a lab result
     * 
     * @param labResultId Lab result ID to delete
     * @throws SQLException if database operation fails
     */
    public void deleteLabResult(String labResultId) throws SQLException {
        labResultDAO.delete(labResultId);
    }
    
    /**
     * Insert a measurement
     * 
     * @param measurement Measurement to insert
     * @throws SQLException if database operation fails
     */
    public void insertMeasurement(Measurement measurement) throws SQLException {
        measurementDAO.insert(measurement);
    }
    
    /**
     * Update a measurement
     * 
     * @param measurement Measurement to update
     * @throws SQLException if database operation fails
     */
    public void updateMeasurement(Measurement measurement) throws SQLException {
        measurementDAO.update(measurement);
    }
    
    /**
     * Delete a measurement
     * 
     * @param measurementId Measurement ID to delete
     * @throws SQLException if database operation fails
     */
    public void deleteMeasurement(String measurementId) throws SQLException {
        measurementDAO.delete(measurementId);
    }
    
    /**
     * Get all lab results from all patients
     * 
     * @return List of lab results
     * @throws SQLException if database operation fails
     */
    public List<LabResult> getAllLabResults() throws SQLException {
        return labResultDAO.getAll();
    }
    
    /**
     * Get all lab results grouped by group for all patients
     * 
     * @return Map of group ID to list of lab results
     * @throws SQLException if database operation fails
     */
    public Map<String, List<LabResult>> getLabResultsGrouped() throws SQLException {
        return labResultDAO.getAllGrouped();
    }
    
    /**
     * Get all measurements for all patients grouped by lab result
     * 
     * @return Map of lab result ID to list of measurements
     * @throws SQLException if database operation fails
     */
    public Map<String, List<Measurement>> getAllMeasurements() throws SQLException {
        return measurementDAO.getAllGroupedByLabResult();
    }
}
