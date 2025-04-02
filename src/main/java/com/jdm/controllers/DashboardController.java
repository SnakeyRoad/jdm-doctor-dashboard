package com.jdm.controllers;

import com.jdm.dao.DatabaseManager;

import java.sql.SQLException;

/**
 * Controller for dashboard operations
 */
public class DashboardController {
    
    private final DatabaseManager dbManager;
    
    /**
     * Constructor
     */
    public DashboardController() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Check if the database exists
     * 
     * @return true if the database exists, false otherwise
     */
    public boolean databaseExists() {
        return dbManager.databaseExists();
    }
    
    /**
     * Initialize the database
     * 
     * @throws SQLException if database initialization fails
     */
    public void initializeDatabase() throws SQLException {
        dbManager.initializeDatabase();
    }
}
