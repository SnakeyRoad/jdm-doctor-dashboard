package com.jdm.utils;

import com.jdm.dao.*;
import com.jdm.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CSVImporterTest {

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Connection connection;

    @Mock
    private PatientDAO patientDAO;

    @Mock
    private CMASDAO cmasDAO;

    @Mock
    private LabResultGroupDAO labResultGroupDAO;

    @Mock
    private LabResultDAO labResultDAO;

    @Mock
    private MeasurementDAO measurementDAO;

    private CSVImporter csvImporter;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dbManager.getConnection()).thenReturn(connection);
        when(dbManager.databaseExists()).thenReturn(true);
        
        csvImporter = new CSVImporter();
        // Use reflection to inject mocks
        try {
            java.lang.reflect.Field dbManagerField = CSVImporter.class.getDeclaredField("dbManager");
            dbManagerField.setAccessible(true);
            dbManagerField.set(csvImporter, dbManager);

            java.lang.reflect.Field patientDAOField = CSVImporter.class.getDeclaredField("patientDAO");
            patientDAOField.setAccessible(true);
            patientDAOField.set(csvImporter, patientDAO);

            java.lang.reflect.Field cmasDAOField = CSVImporter.class.getDeclaredField("cmasDAO");
            cmasDAOField.setAccessible(true);
            cmasDAOField.set(csvImporter, cmasDAO);

            java.lang.reflect.Field labResultGroupDAOField = CSVImporter.class.getDeclaredField("labResultGroupDAO");
            labResultGroupDAOField.setAccessible(true);
            labResultGroupDAOField.set(csvImporter, labResultGroupDAO);

            java.lang.reflect.Field labResultDAOField = CSVImporter.class.getDeclaredField("labResultDAO");
            labResultDAOField.setAccessible(true);
            labResultDAOField.set(csvImporter, labResultDAO);

            java.lang.reflect.Field measurementDAOField = CSVImporter.class.getDeclaredField("measurementDAO");
            measurementDAOField.setAccessible(true);
            measurementDAOField.set(csvImporter, measurementDAO);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }
    }

    @Test
    void testImportAllData_Success() throws IOException, SQLException {
        // Mock DAO responses
        when(patientDAO.getFirst(any(Connection.class))).thenReturn(new Patient("P001", "Test Patient"));
        doNothing().when(patientDAO).insertBatch(any(), any(Connection.class));
        doNothing().when(cmasDAO).insertBatch(any(), any(Connection.class));
        doNothing().when(labResultGroupDAO).insertBatch(any(), any(Connection.class));
        doNothing().when(labResultDAO).insertBatch(any(), any(Connection.class));
        doNothing().when(measurementDAO).insertBatch(any(), any(Connection.class));

        // Execute import with test files
        assertDoesNotThrow(() -> csvImporter.importAllData(
            "data/Patient.csv",
            "data/CMAS.csv",
            "data/LabResultGroup.csv",
            "data/LabResult.csv",
            "data/LabResultsEN.csv",
            "data/Measurement.csv"
        ));

        // Verify DAO interactions
        verify(patientDAO).insertBatch(any(), any(Connection.class));
        verify(cmasDAO).insertBatch(any(), any(Connection.class));
        verify(labResultGroupDAO).insertBatch(any(), any(Connection.class));
        verify(labResultDAO).insertBatch(any(), any(Connection.class));
        verify(measurementDAO).insertBatch(any(), any(Connection.class));
    }

    @Test
    void testImportAllData_RollbackOnError() throws IOException, SQLException {
        // Mock DAO to throw exception during CMAS import
        when(patientDAO.getFirst(any(Connection.class))).thenReturn(new Patient("P001", "Test Patient"));
        doNothing().when(patientDAO).insertBatch(any(), any(Connection.class));
        doThrow(new SQLException("Test error")).when(cmasDAO).insertBatch(any(), any(Connection.class));

        // Execute import and expect exception
        assertThrows(SQLException.class, () -> csvImporter.importAllData(
            "data/Patient.csv",
            "data/CMAS.csv",
            "data/LabResultGroup.csv",
            "data/LabResult.csv",
            "data/LabResultsEN.csv",
            "data/Measurement.csv"
        ));

        // Verify rollback was called
        verify(connection).rollback();
    }
} 