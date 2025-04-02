package com.jdm.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connections and initialization
 */
public class DatabaseManager {
    private static final String DB_FILE_NAME = "jdm_dashboard.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_FILE_NAME;
    private static DatabaseManager instance;
    
    private DatabaseManager() {
        // Private constructor for singleton pattern
    }
    
    /**
     * Get the singleton instance of DatabaseManager
     * 
     * @return DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Check if the database file exists
     * 
     * @return true if the database file exists, false otherwise
     */
    public boolean databaseExists() {
        File dbFile = new File(DB_FILE_NAME);
        return dbFile.exists();
    }
    
    /**
     * Create a new database connection
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }
    
    /**
     * Initialize the database schema
     * 
     * @throws SQLException if initialization fails
     */
    public void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Patient table
            stmt.execute("CREATE TABLE IF NOT EXISTS patient (" +
                    "patient_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL)");
            
            // Create CMAS table
            stmt.execute("CREATE TABLE IF NOT EXISTS cmas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT NOT NULL, " + 
                    "category TEXT NOT NULL, " +
                    "value INTEGER NOT NULL, " +
                    "patient_id TEXT NOT NULL, " +
                    "FOREIGN KEY (patient_id) REFERENCES patient (patient_id))");
            
            // Create Lab Result Group table
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_result_group (" +
                    "lab_result_group_id TEXT PRIMARY KEY, " +
                    "group_name TEXT NOT NULL)");
            
            // Create Lab Result table
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_result (" +
                    "lab_result_id TEXT PRIMARY KEY, " +
                    "lab_result_group_id TEXT NOT NULL, " +
                    "patient_id TEXT NOT NULL, " +
                    "result_name TEXT NOT NULL, " +
                    "unit TEXT, " +
                    "result_name_english TEXT, " +
                    "FOREIGN KEY (lab_result_group_id) REFERENCES lab_result_group (lab_result_group_id), " +
                    "FOREIGN KEY (patient_id) REFERENCES patient (patient_id))");
            
            // Create Measurement table
            stmt.execute("CREATE TABLE IF NOT EXISTS measurement (" +
                    "measurement_id TEXT PRIMARY KEY, " +
                    "lab_result_id TEXT NOT NULL, " +
                    "date_time TEXT NOT NULL, " +
                    "value TEXT NOT NULL, " +
                    "FOREIGN KEY (lab_result_id) REFERENCES lab_result (lab_result_id))");
            
            // Create indexes to improve query performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cmas_patient_id ON cmas (patient_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cmas_date ON cmas (date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lab_result_patient_id ON lab_result (patient_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lab_result_group_id ON lab_result (lab_result_group_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_measurement_lab_result_id ON measurement (lab_result_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_measurement_date_time ON measurement (date_time)");
        }
    }
    
    /**
     * Close database connection
     * 
     * @param conn Connection to close
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
