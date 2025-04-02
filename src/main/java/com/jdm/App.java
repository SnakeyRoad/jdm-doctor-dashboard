package com.jdm;

import com.jdm.dao.DatabaseManager;
import com.jdm.ui.DashboardPanel;
import com.jdm.utils.CSVImporter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Main application class for JDM Doctor Dashboard
 */
public class App {
    private static final String DATA_DIR = "data";
    private static final String PATIENT_CSV = "Patient.csv";
    private static final String CMAS_CSV = "CMAS.csv";
    private static final String LAB_RESULT_GROUP_CSV = "LabResultGroup.csv";
    private static final String LAB_RESULT_CSV = "LabResult.csv";
    private static final String LAB_RESULTS_EN_CSV = "LabResultsEN.csv";
    private static final String MEASUREMENT_CSV = "Measurement.csv";
    
    /**
     * Application entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database if needed
                initializeDatabase();
                
                // Create and show the main application window
                createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error initializing application: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    /**
     * Initialize the database with CSV data if it doesn't exist
     * 
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    private static void initializeDatabase() throws IOException, SQLException {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Check if database exists, create and populate it if not
        if (!dbManager.databaseExists()) {
            System.out.println("Database not found. Creating and importing data...");
            
            // Initialize the database schema
            dbManager.initializeDatabase();
            
            // Import data from CSV files
            CSVImporter importer = new CSVImporter();
            importer.importAllData(
                getDataFilePath(PATIENT_CSV),
                getDataFilePath(CMAS_CSV),
                getDataFilePath(LAB_RESULT_GROUP_CSV),
                getDataFilePath(LAB_RESULT_CSV),
                getDataFilePath(LAB_RESULTS_EN_CSV),
                getDataFilePath(MEASUREMENT_CSV)
            );
            
            System.out.println("Database created and data imported successfully.");
        }
    }
    
    /**
     * Create and show the main application window
     */
    private static void createAndShowGUI() {
        // Create main JFrame
        JFrame frame = new JFrame("JDM Doctor Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(800, 600));
        
        // Create dashboard panel
        DashboardPanel dashboardPanel = new DashboardPanel();
        frame.add(dashboardPanel);
        
        // Center on screen
        frame.setLocationRelativeTo(null);
        
        // Show the frame
        frame.setVisible(true);
    }
    
    /**
     * Get the full path to a data file
     * 
     * @param fileName File name
     * @return Full path to the file
     */
    private static String getDataFilePath(String fileName) {
        return DATA_DIR + File.separator + fileName;
    }
}
