package com.jdm.ui;

import com.jdm.controllers.CMASController;
import com.jdm.controllers.DashboardController;
import com.jdm.controllers.LabResultController;
import com.jdm.controllers.PatientController;
import com.jdm.models.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Main dashboard panel that contains all UI components
 */
public class DashboardPanel extends JPanel {
    
    private final DashboardController dashboardController;
    private final PatientController patientController;
    private final CMASController cmasController;
    private final LabResultController labResultController;
    
    private PatientInfoPanel patientInfoPanel;
    private JTabbedPane tabbedPane;
    private CMASChartPanel cmasChartPanel;
    private LabResultPanel labResultPanel;
    private ExportPanel exportPanel;
    
    /**
     * Constructor
     */
    public DashboardPanel() {
        // Initialize controllers
        this.dashboardController = new DashboardController();
        this.patientController = new PatientController();
        this.cmasController = new CMASController();
        this.labResultController = new LabResultController();
        
        // Initialize UI
        initializeUI();
        
        // Load initial data
        loadData();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set layout
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header panel with logo and title
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Left panel with patient info
        patientInfoPanel = new PatientInfoPanel(patientController);
        add(patientInfoPanel, BorderLayout.WEST);
        
        // Center tabbed pane for different views
        tabbedPane = new JTabbedPane();
        
        // Create CMAS tab
        cmasChartPanel = new CMASChartPanel(cmasController);
        tabbedPane.addTab("CMAS Progress", new JScrollPane(cmasChartPanel));
        
        // Create Lab Results tab
        labResultPanel = new LabResultPanel(labResultController);
        tabbedPane.addTab("Lab Results", new JScrollPane(labResultPanel));
        
        // Create Export tab
        exportPanel = new ExportPanel(cmasController, labResultController);
        tabbedPane.addTab("Export Data", new JScrollPane(exportPanel));
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Footer panel with status and version info
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create the header panel with logo and title
     * 
     * @return Header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // App title
        JLabel titleLabel = new JLabel("JDM Doctor Dashboard 2.0");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshData());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create the footer panel with status and version info
     * 
     * @return Footer panel
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Status message
        JLabel statusLabel = new JLabel("Ready");
        footerPanel.add(statusLabel, BorderLayout.WEST);
        
        // Version info
        JLabel versionLabel = new JLabel("v2.0");
        footerPanel.add(versionLabel, BorderLayout.EAST);
        
        return footerPanel;
    }
    
    /**
     * Load initial data
     */
    private void loadData() {
        try {
            // Get the first patient (for this demo)
            Patient patient = patientController.getFirstPatient();
            if (patient != null) {
                // Update UI components with patient data
                updateUIWithPatient(patient);
            } else {
                showError("No patients found in database.");
            }
        } catch (SQLException e) {
            showError("Error loading patient data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Refresh data
     */
    private void refreshData() {
        // Clear existing data
        cmasChartPanel.clearData();
        labResultPanel.clearData();
        
        // Reload data
        loadData();
    }
    
    /**
     * Update UI components with patient data
     * 
     * @param patient Patient to display
     */
    private void updateUIWithPatient(Patient patient) {
        // Update patient info panel
        patientInfoPanel.updatePatient(patient);
        
        // Load CMAS data for this patient
        cmasChartPanel.loadPatientData(patient);
        
        // Load lab results for this patient
        labResultPanel.loadPatientData(patient);
        
        // Update export panel
        exportPanel.setPatient(patient);
    }
    
    /**
     * Show error message dialog
     * 
     * @param message Error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
