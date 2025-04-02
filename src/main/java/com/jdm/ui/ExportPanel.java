package com.jdm.ui;

import com.jdm.controllers.CMASController;
import com.jdm.controllers.LabResultController;
import com.jdm.models.CMAS;
import com.jdm.models.LabResult;
import com.jdm.models.LabResultGroup;
import com.jdm.models.Measurement;
import com.jdm.models.Patient;
import com.jdm.utils.ExportUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Panel for exporting data to CSV and Excel
 */
public class ExportPanel extends JPanel {
    
    private final CMASController cmasController;
    private final LabResultController labResultController;
    
    private Patient currentPatient;
    private JCheckBox cmasCheckBox;
    private JCheckBox labResultsCheckBox;
    private JComboBox<String> formatComboBox;
    private JButton exportButton;
    private JTextArea exportLogTextArea;
    
    /**
     * Constructor
     * 
     * @param cmasController Controller for CMAS operations
     * @param labResultController Controller for lab result operations
     */
    public ExportPanel(CMASController cmasController, LabResultController labResultController) {
        this.cmasController = cmasController;
        this.labResultController = labResultController;
        
        initializeUI();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set layout
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create options panel
        JPanel optionsPanel = createOptionsPanel();
        add(optionsPanel, BorderLayout.NORTH);
        
        // Create log panel
        JPanel logPanel = createLogPanel();
        add(logPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create the options panel
     * 
     * @return Options panel
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Export Options"));
        
        // Create options for what to export
        JPanel dataOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel dataLabel = new JLabel("Data to Export:");
        dataOptionsPanel.add(dataLabel);
        
        cmasCheckBox = new JCheckBox("CMAS Data", true);
        dataOptionsPanel.add(cmasCheckBox);
        
        labResultsCheckBox = new JCheckBox("Lab Results", true);
        dataOptionsPanel.add(labResultsCheckBox);
        
        panel.add(dataOptionsPanel, BorderLayout.NORTH);
        
        // Create format and export controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel formatLabel = new JLabel("Export Format:");
        controlsPanel.add(formatLabel);
        
        formatComboBox = new JComboBox<>(new String[]{"CSV", "Excel"});
        controlsPanel.add(formatComboBox);
        
        exportButton = new JButton("Export Data");
        exportButton.addActionListener(e -> exportData());
        controlsPanel.add(exportButton);
        
        panel.add(controlsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create the log panel
     * 
     * @return Log panel
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Export Log"));
        
        exportLogTextArea = new JTextArea();
        exportLogTextArea.setEditable(false);
        exportLogTextArea.setRows(10);
        
        JScrollPane scrollPane = new JScrollPane(exportLogTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Set the current patient
     * 
     * @param patient Patient to set
     */
    public void setPatient(Patient patient) {
        this.currentPatient = patient;
    }
    
    /**
     * Export data based on selected options
     */
    private void exportData() {
        // Check if any data type is selected
        if (!cmasCheckBox.isSelected() && !labResultsCheckBox.isSelected()) {
            showError("Please select at least one data type to export.");
            return;
        }
        
        try {
            // Get selected format
            String format = (String) formatComboBox.getSelectedItem();
            boolean isExcel = "Excel".equals(format);
            
            // Create export directory if it doesn't exist
            File exportDir = new File("exports");
            if (!exportDir.exists()) {
                exportDir.mkdir();
            }
            
            // Generate timestamp for filenames
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            if (isExcel) {
                // Export to Excel
                exportToExcel(exportDir, timestamp);
            } else {
                // Export to CSV
                exportToCSV(exportDir, timestamp);
            }
            
        } catch (Exception e) {
            logExport("Error: " + e.getMessage());
            e.printStackTrace();
            showError("Export failed: " + e.getMessage());
        }
    }
    
    /**
     * Export data to CSV files
     * 
     * @param exportDir Export directory
     * @param timestamp Timestamp for filename
     */
    private void exportToCSV(File exportDir, String timestamp) {
        try {
            // Export CMAS data if selected
            if (cmasCheckBox.isSelected()) {
                List<CMAS> allCmasEntries = cmasController.getAllCMAS();
                if (!allCmasEntries.isEmpty()) {
                    String filename = String.format("All_Patients_CMAS_%s.csv", timestamp);
                    Path filePath = exportDir.toPath().resolve(filename);
                    
                    ExportUtils.exportCMASToCSV(allCmasEntries, filePath);
                    logExport("Exported " + allCmasEntries.size() + " CMAS entries to " + filePath);
                } else {
                    logExport("No CMAS data to export.");
                }
            }
            
            // Export lab results if selected
            if (labResultsCheckBox.isSelected()) {
                List<LabResult> allLabResults = labResultController.getAllLabResults();
                Map<String, List<Measurement>> allMeasurementsMap = labResultController.getAllMeasurements();
                
                if (!allLabResults.isEmpty()) {
                    String filename = String.format("All_Patients_LabResults_%s.csv", timestamp);
                    Path filePath = exportDir.toPath().resolve(filename);
                    
                    ExportUtils.exportLabResultsToCSV(allLabResults, allMeasurementsMap, filePath);
                    
                    int totalMeasurements = 0;
                    for (List<Measurement> measurements : allMeasurementsMap.values()) {
                        totalMeasurements += measurements.size();
                    }
                    
                    logExport("Exported " + allLabResults.size() + " lab results with " + 
                             totalMeasurements + " measurements to " + filePath);
                } else {
                    logExport("No lab results to export.");
                }
            }
            
            logExport("Export to CSV completed successfully.");
            JOptionPane.showMessageDialog(this, "Export completed successfully.",
                                         "Export Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            logExport("Error exporting to CSV: " + e.getMessage());
            e.printStackTrace();
            showError("Export to CSV failed: " + e.getMessage());
        }
    }
    
    /**
     * Export data to Excel file
     * 
     * @param exportDir Export directory
     * @param timestamp Timestamp for filename
     */
    private void exportToExcel(File exportDir, String timestamp) {
        try {
            // Get all data
            List<CMAS> cmasEntries = cmasCheckBox.isSelected() ?
                cmasController.getAllCMAS() : null;
            
            List<LabResult> labResults = labResultsCheckBox.isSelected() ?
                labResultController.getAllLabResults() : null;
            
            // Skip export if no data selected or no data available
            if ((cmasEntries == null || cmasEntries.isEmpty()) && 
                (labResults == null || labResults.isEmpty())) {
                logExport("No data to export.");
                showError("No data to export.");
                return;
            }
            
            // Get additional data needed for Excel
            Map<String, List<CMAS>> cmasByCategory = cmasCheckBox.isSelected() ?
                cmasController.getCMASGroupedByCategory() : null;
            
            Map<String, List<LabResult>> labResultsByGroup = labResultsCheckBox.isSelected() ?
                labResultController.getLabResultsGrouped() : null;
            
            Map<String, List<Measurement>> measurementsMap = labResultsCheckBox.isSelected() ?
                labResultController.getAllMeasurements() : null;
            
            // Get group names
            Map<String, String> groupNames = labResultsCheckBox.isSelected() ?
                labResultController.getGroupNames() : null;
            
            // Create Excel file
            String filename = String.format("All_Patients_Report_%s.xlsx", timestamp);
            Path filePath = exportDir.toPath().resolve(filename);
            
            ExportUtils.exportPatientReportToExcel(
                null,
                cmasEntries,
                cmasByCategory,
                labResults,
                labResultsByGroup,
                measurementsMap,
                groupNames,
                filePath
            );
            
            logExport("Exported patient report to " + filePath);
            logExport("Export to Excel completed successfully.");
            
            JOptionPane.showMessageDialog(this, "Export completed successfully.",
                                         "Export Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            logExport("Error exporting to Excel: " + e.getMessage());
            e.printStackTrace();
            showError("Export to Excel failed: " + e.getMessage());
        }
    }
    
    /**
     * Log export activity to the text area
     * 
     * @param message Log message
     */
    private void logExport(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        exportLogTextArea.append(timestamp + " - " + message + "\n");
        exportLogTextArea.setCaretPosition(exportLogTextArea.getDocument().getLength());
    }
    
    /**
     * Show error message
     * 
     * @param message Error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
