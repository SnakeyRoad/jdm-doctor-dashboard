package com.jdm.dao;

import com.jdm.models.CMAS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CMASDAOTest {

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private CMASDAO cmasDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        // Use reflection to inject mock DatabaseManager
        try {
            java.lang.reflect.Field dbManagerField = CMASDAO.class.getDeclaredField("dbManager");
            dbManagerField.setAccessible(true);
            cmasDAO = new CMASDAO();
            dbManagerField.set(cmasDAO, dbManager);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    @Test
    void getAllForPatient_ShouldReturnCMASList() throws SQLException {
        // Arrange
        String patientId = "P001";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("date")).thenReturn("2024-01-01", "2024-01-02");
        when(resultSet.getString("category")).thenReturn("Physical", "Mental");
        when(resultSet.getInt("value")).thenReturn(85, 90);
        when(resultSet.getString("patient_id")).thenReturn(patientId, patientId);

        // Act
        List<CMAS> result = cmasDAO.getAllForPatient(patientId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Physical", result.get(0).getCategory());
        assertEquals("Mental", result.get(1).getCategory());
        verify(preparedStatement).setString(1, patientId);
    }

    @Test
    void getById_ShouldReturnCMAS() throws SQLException {
        // Arrange
        int cmasId = 1;
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(cmasId);
        when(resultSet.getString("date")).thenReturn("2024-01-01");
        when(resultSet.getString("category")).thenReturn("Physical");
        when(resultSet.getInt("value")).thenReturn(85);
        when(resultSet.getString("patient_id")).thenReturn("P001");

        // Act
        CMAS result = cmasDAO.getById(cmasId);

        // Assert
        assertNotNull(result);
        assertEquals(cmasId, result.getId());
        assertEquals("Physical", result.getCategory());
        assertEquals(85, result.getValue());
        verify(preparedStatement).setInt(1, cmasId);
    }

    @Test
    void insert_ShouldCreateNewCMAS() throws SQLException {
        // Arrange
        CMAS cmas = new CMAS(LocalDate.now(), "Physical", 85, "P001");
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        // Act
        int id = cmasDAO.insert(cmas);

        // Assert
        assertEquals(1, id);
        verify(preparedStatement).setString(1, cmas.getDate().toString());
        verify(preparedStatement).setString(2, cmas.getCategory());
        verify(preparedStatement).setInt(3, cmas.getValue());
        verify(preparedStatement).setString(4, cmas.getPatientId());
    }

    @Test
    void insertBatch_ShouldCreateMultipleCMAS() throws SQLException {
        // Arrange
        List<CMAS> cmasList = new ArrayList<>();
        cmasList.add(new CMAS(LocalDate.now(), "Physical", 85, "P001"));
        cmasList.add(new CMAS(LocalDate.now(), "Mental", 90, "P001"));
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1, 1});

        // Act
        cmasDAO.insertBatch(cmasList, connection);

        // Assert
        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement).executeBatch();
    }

    @Test
    void getByCategory_ShouldReturnFilteredCMAS() throws SQLException {
        // Arrange
        String patientId = "P001";
        String category = "Physical";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("date")).thenReturn("2024-01-01");
        when(resultSet.getString("category")).thenReturn(category);
        when(resultSet.getInt("value")).thenReturn(85);
        when(resultSet.getString("patient_id")).thenReturn(patientId);

        // Act
        List<CMAS> result = cmasDAO.getByCategory(patientId, category);

        // Assert
        assertEquals(1, result.size());
        assertEquals(category, result.get(0).getCategory());
        verify(preparedStatement).setString(1, patientId);
        verify(preparedStatement).setString(2, category);
    }

    @Test
    void getGroupedByCategory_ShouldReturnGroupedCMAS() throws SQLException {
        // Arrange
        String patientId = "P001";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("date")).thenReturn("2024-01-01", "2024-01-02");
        when(resultSet.getString("category")).thenReturn("Physical", "Mental");
        when(resultSet.getInt("value")).thenReturn(85, 90);
        when(resultSet.getString("patient_id")).thenReturn(patientId, patientId);

        // Act
        Map<String, List<CMAS>> result = cmasDAO.getGroupedByCategory(patientId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Physical"));
        assertTrue(result.containsKey("Mental"));
        assertEquals(1, result.get("Physical").size());
        assertEquals(1, result.get("Mental").size());
        verify(preparedStatement).setString(1, patientId);
    }

    @Test
    void getTrendsByMonth_ShouldReturnTrendData() throws SQLException {
        // Arrange
        String patientId = "P001";
        String category = "Physical";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("month")).thenReturn("2024-01");
        when(resultSet.getDouble("avg_value")).thenReturn(85.5);

        // Act
        List<Map<String, Object>> result = cmasDAO.getTrendsByMonth(patientId, category);

        // Assert
        assertEquals(1, result.size());
        assertEquals("2024-01", result.get(0).get("month"));
        assertEquals(85.5, result.get(0).get("avgValue"));
        verify(preparedStatement).setString(1, patientId);
        verify(preparedStatement).setString(2, category);
    }
}
