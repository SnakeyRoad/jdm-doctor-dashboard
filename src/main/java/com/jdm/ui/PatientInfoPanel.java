package com.jdm.ui;

import com.jdm.controllers.PatientController;
import com.jdm.models.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel for displaying patient information
 */
public class PatientInfoPanel extends JPanel {
    
    private final PatientController patientController;
    
    private JLabel patientNameLabel;
    private JLabel patientIdLabel;
    private JLabel diagnosisLabel;
    private JLabel statusLabel;
    
    /**
     * Constructor
     * 
     * @param patientController Controller for patient operations
     */
    public PatientInfoPanel(PatientController patientController) {
        this.patientController = patientController;
        
        initializeUI();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set layout and border
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Patient Information"));
        setPreferredSize(new Dimension(250, 0));
        
        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create constraints for GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add patient name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        patientNameLabel = new JLabel();
        contentPanel.add(patientNameLabel, gbc);
        
        // Add patient ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("ID:"), gbc);
        
        gbc.gridx = 1;
        patientIdLabel = new JLabel();
        contentPanel.add(patientIdLabel, gbc);
        
        // Add diagnosis
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Diagnosis:"), gbc);
        
        gbc.gridx = 1;
        diagnosisLabel = new JLabel("Juvenile Dermatomyositis (JDM)");
        contentPanel.add(diagnosisLabel, gbc);
        
        // Add status
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        statusLabel = new JLabel("Active");
        contentPanel.add(statusLabel, gbc);
        
        // Add spacer to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        contentPanel.add(Box.createVerticalGlue(), gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Update the panel with patient data
     * 
     * @param patient Patient to display
     */
    public void updatePatient(Patient patient) {
        if (patient != null) {
            patientNameLabel.setText(patient.getName());
            patientIdLabel.setText(patient.getPatientId());
            // Other fields would be populated from patient data if available
        } else {
            patientNameLabel.setText("N/A");
            patientIdLabel.setText("N/A");
            diagnosisLabel.setText("N/A");
            statusLabel.setText("N/A");
        }
    }
}
