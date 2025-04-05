package com.jdm.dao;

import com.jdm.models.Measurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeasurementDAOTest {

    @Mock
    private DatabaseManager mockDbManager;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    private MeasurementDAO measurementDAO;
    
    @BeforeEach
    void setUp() throws SQLException {
        measurementDAO = new MeasurementDAO(mockDbManager);
        when(mockDbManager.getConnection()).thenReturn(mockConnection);
    }
    
    @Test
    void getAllForLabResult_ShouldReturnMeasurements() throws Exception {
        // Arrange
        String labResultId = "test-lab-result-1";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("measurement_id")).thenReturn("m1", "m2");
        when(mockResultSet.getString("lab_result_id")).thenReturn(labResultId, labResultId);
        when(mockResultSet.getString("date_time")).thenReturn("2024-01-01T10:00:00", "2024-01-01T11:00:00");
        when(mockResultSet.getString("value")).thenReturn("100.5", "101.2");
        
        // Act
        List<Measurement> measurements = measurementDAO.getAllForLabResult(labResultId);
        
        // Assert
        assertNotNull(measurements);
        assertEquals(2, measurements.size());
        assertEquals("m1", measurements.get(0).getMeasurementId());
        assertEquals("m2", measurements.get(1).getMeasurementId());
        verify(mockPreparedStatement).setString(1, labResultId);
    }
    
    @Test
    void getById_ShouldReturnMeasurement() throws Exception {
        // Arrange
        String measurementId = "test-measurement-1";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("measurement_id")).thenReturn(measurementId);
        when(mockResultSet.getString("lab_result_id")).thenReturn("test-lab-result-1");
        when(mockResultSet.getString("date_time")).thenReturn("2024-01-01T10:00:00");
        when(mockResultSet.getString("value")).thenReturn("100.5");
        
        // Act
        Measurement measurement = measurementDAO.getById(measurementId);
        
        // Assert
        assertNotNull(measurement);
        assertEquals(measurementId, measurement.getMeasurementId());
        verify(mockPreparedStatement).setString(1, measurementId);
    }
    
    @Test
    void insert_ShouldCreateNewMeasurement() throws Exception {
        // Arrange
        Measurement measurement = new Measurement(
            "test-measurement-1",
            "test-lab-result-1",
            LocalDateTime.now(),
            "100.5"
        );
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        measurementDAO.insert(measurement);
        
        // Assert
        verify(mockPreparedStatement).setString(1, measurement.getMeasurementId());
        verify(mockPreparedStatement).setString(2, measurement.getLabResultId());
        verify(mockPreparedStatement).setString(3, measurement.getDateTime().toString());
        verify(mockPreparedStatement).setString(4, measurement.getValue());
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void update_ShouldModifyExistingMeasurement() throws Exception {
        // Arrange
        Measurement measurement = new Measurement(
            "test-measurement-1",
            "test-lab-result-1",
            LocalDateTime.now(),
            "100.5"
        );
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        measurementDAO.update(measurement);
        
        // Assert
        verify(mockPreparedStatement).setString(1, measurement.getLabResultId());
        verify(mockPreparedStatement).setString(2, measurement.getDateTime().toString());
        verify(mockPreparedStatement).setString(3, measurement.getValue());
        verify(mockPreparedStatement).setString(4, measurement.getMeasurementId());
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void delete_ShouldRemoveMeasurement() throws Exception {
        // Arrange
        String measurementId = "test-measurement-1";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        measurementDAO.delete(measurementId);
        
        // Assert
        verify(mockPreparedStatement).setString(1, measurementId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void getForPatientInDateRange_ShouldReturnFilteredMeasurements() throws Exception {
        // Arrange
        String patientId = "test-patient-1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("measurement_id")).thenReturn("m1");
        when(mockResultSet.getString("lab_result_id")).thenReturn("lr1");
        when(mockResultSet.getString("date_time")).thenReturn(startDate.plusDays(1).toString());
        when(mockResultSet.getString("value")).thenReturn("100.5");
        
        // Act
        List<Measurement> measurements = measurementDAO.getForPatientInDateRange(patientId, startDate, endDate);
        
        // Assert
        assertNotNull(measurements);
        assertEquals(1, measurements.size());
        verify(mockPreparedStatement).setString(1, patientId);
        verify(mockPreparedStatement).setString(2, startDate.toString());
        verify(mockPreparedStatement).setString(3, endDate.toString());
    }
} 