package com.jdm.ui;

import com.jdm.controllers.LabResultController;
import com.jdm.models.LabResult;
import com.jdm.models.LabResultGroup;
import com.jdm.models.Measurement;
import com.jdm.models.Patient;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * Panel for displaying laboratory results
 */
public class LabResultPanel extends JPanel {
    
    private final LabResultController labResultController;
    
    private Patient currentPatient;
    private JComboBox<String> groupComboBox;
    private JPanel resultPanel;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JPanel chartPanel;
    private Map<String, String> groupIdToNameMap;
    
    /**
     * Constructor
     * 
     * @param labResultController Controller for lab result operations
     */
    public LabResultPanel(LabResultController labResultController) {
        this.labResultController = labResultController;
        this.groupIdToNameMap = new HashMap<>();
        
        initializeUI();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set layout
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Create main panel with split between table and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Equal resize
        
        // Create result table panel
        JPanel tablePanel = createTablePanel();
        splitPane.setTopComponent(tablePanel);
        
        // Create chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(new TitledBorder("Result Trend"));
        splitPane.setBottomComponent(chartPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Create result panel for showing selected result details
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("Result Details"));
        resultPanel.setPreferredSize(new Dimension(250, 0));
        resultPanel.setMinimumSize(new Dimension(200, 0)); // Add minimum size
        add(resultPanel, BorderLayout.EAST);
    }
    
    /**
     * Create the control panel with filters and options
     * 
     * @return Control panel
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // Group filter
        panel.add(new JLabel("Result Group:"));
        groupComboBox = new JComboBox<>();
        groupComboBox.addItem("All Groups");
        groupComboBox.addActionListener(e -> updateResultTable());
        panel.add(groupComboBox);
        
        // Add date filter options if needed
        
        // Update button
        JButton updateButton = new JButton("Update Results");
        updateButton.addActionListener(e -> updateResultTable());
        panel.add(updateButton);
        
        return panel;
    }
    
    /**
     * Create the table panel for showing lab results
     * 
     * @return Table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Lab Results"));
        
        // Create table model
        String[] columns = {"Result Name", "Group", "Latest Value", "Unit", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        // Create table
        resultTable = new JTable(tableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String resultName = (String) tableModel.getValueAt(selectedRow, 0);
                    String group = (String) tableModel.getValueAt(selectedRow, 1);
                    showResultDetails(resultName, group);
                }
            }
        });
        
        // Set minimum row height for better readability
        resultTable.setRowHeight(resultTable.getRowHeight() + 4);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Load patient data and update UI
     * 
     * @param patient Patient to display
     */
    public void loadPatientData(Patient patient) {
        this.currentPatient = patient;
        
        try {
            // Load lab result groups
            List<LabResultGroup> groups = labResultController.getAllLabResultGroups();
            
            // Update group combo box
            groupComboBox.removeAllItems();
            groupComboBox.addItem("All Groups");
            
            groupIdToNameMap.clear();
            for (LabResultGroup group : groups) {
                String groupId = group.getLabResultGroupId();
                String groupName = group.getGroupName();
                groupIdToNameMap.put(groupId, groupName);
                groupComboBox.addItem(groupName);
            }
            
            // Load and display lab results from all patients
            updateResultTable();
            
        } catch (SQLException e) {
            showError("Error loading lab result groups: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update the result table based on current filters
     */
    private void updateResultTable() {
        if (currentPatient == null) {
            return;
        }
        
        try {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Get all lab results
            List<LabResult> results = labResultController.getAllLabResults();
            
            // Get selected group
            String selectedGroup = (String) groupComboBox.getSelectedItem();
            String selectedGroupId = null;
            if (selectedGroup != null && !selectedGroup.equals("All Groups")) {
                for (Map.Entry<String, String> entry : groupIdToNameMap.entrySet()) {
                    if (entry.getValue().equals(selectedGroup)) {
                        selectedGroupId = entry.getKey();
                        break;
                    }
                }
            }
            
            // Add results to table
            for (LabResult result : results) {
                // Skip if group filter is active and doesn't match
                if (selectedGroupId != null && !result.getLabResultGroupId().equals(selectedGroupId)) {
                    continue;
                }
                
                // Get group name
                String groupName = groupIdToNameMap.get(result.getLabResultGroupId());
                if (groupName == null) {
                    groupName = "Unknown Group";
                }
                
                // Get latest measurement
                List<Measurement> measurements = labResultController.getMeasurements(result.getLabResultId());
                String latestValue = "";
                String latestDate = "";
                if (!measurements.isEmpty()) {
                    // Sort measurements by date (most recent first)
                    measurements.sort(Comparator.comparing(Measurement::getDateTime).reversed());
                    Measurement latest = measurements.get(0);
                    latestValue = latest.getValue();
                    latestDate = latest.getDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
                }
                
                // Add row to table
                tableModel.addRow(new Object[]{
                    result.getResultName(),
                    groupName,
                    latestValue,
                    result.getUnit(),
                    latestDate
                });
            }
            
        } catch (SQLException e) {
            showError("Error updating lab results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show details for a selected lab result
     * 
     * @param resultName Name of the selected result
     * @param groupName Group name of the selected result
     */
    private void showResultDetails(String resultName, String groupName) {
        try {
            // Find the selected lab result
            LabResult selectedResult = null;
            List<LabResult> allResults = labResultController.getAllLabResults();
            
            for (LabResult result : allResults) {
                if (result.getResultName().equals(resultName)) {
                    String resultGroupId = result.getLabResultGroupId();
                    String resultGroupName = groupIdToNameMap.getOrDefault(resultGroupId, "Unknown Group");
                    
                    if (resultGroupName.equals(groupName)) {
                        selectedResult = result;
                        break;
                    }
                }
            }
            
            if (selectedResult != null) {
                // Get measurements for this lab result
                List<Measurement> measurements = labResultController.getMeasurements(selectedResult.getLabResultId());
                
                // Create time series chart
                createMeasurementChart(selectedResult, measurements);
                
                // Show details in the details panel
                showResultDetailsPanel(selectedResult, measurements);
            }
            
        } catch (SQLException e) {
            showError("Error loading result details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a time series chart for measurements
     * 
     * @param labResult Lab result to chart
     * @param measurements List of measurements
     */
    private void createMeasurementChart(LabResult labResult, List<Measurement> measurements) {
        // Check if there are numeric measurements
        boolean hasNumericValues = false;
        for (Measurement m : measurements) {
            if (m.getNumericValue() != null) {
                hasNumericValues = true;
                break;
            }
        }
        
        if (!hasNumericValues || measurements.isEmpty()) {
            chartPanel.removeAll();
            JLabel noDataLabel = new JLabel("No numeric data available for charting.");
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            chartPanel.add(noDataLabel, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            return;
        }
        
        // Create time series
        TimeSeries series = new TimeSeries(labResult.getDisplayName());
        
        // Sort measurements by date
        measurements.sort(Comparator.comparing(Measurement::getDateTime));
        
        // Add measurements to series
        for (Measurement measurement : measurements) {
            Double value = measurement.getNumericValue();
            if (value != null) {
                // Convert LocalDateTime to java.util.Date for JFreeChart
                LocalDateTime dateTime = measurement.getDateTime();
                Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                series.add(new Day(date), value);
            }
        }
        
        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        
        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            labResult.getDisplayName(),  // title
            "Date",                     // x-axis label
            labResult.getUnit(),        // y-axis label
            dataset,                    // data
            true,                       // include legend
            true,                       // tooltips
            false                       // URLs
        );
        
        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        
        // Set the renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(renderer);
        
        // Format date axis
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("dd-MMM-yyyy"));
        
        // Remove existing components from chart panel
        chartPanel.removeAll();
        
        // Add the chart to the panel
        ChartPanel jfreeChartPanel = new ChartPanel(chart);
        jfreeChartPanel.setPreferredSize(new Dimension(600, 300));
        jfreeChartPanel.setMouseWheelEnabled(true);
        chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);
        
        // Repaint the panel
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * Show result details in the details panel
     * 
     * @param labResult Lab result to display
     * @param measurements Measurements for this lab result
     */
    private void showResultDetailsPanel(LabResult labResult, List<Measurement> measurements) {
        // Clear panel
        resultPanel.removeAll();
        
        // Create panel with GridBagLayout for details
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Create a panel for basic details with custom layout
        JPanel basicDetailsPanel = new JPanel();
        basicDetailsPanel.setLayout(new BoxLayout(basicDetailsPanel, BoxLayout.Y_AXIS));
        basicDetailsPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        // Add result name (with word wrap)
        JPanel namePanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel("Result Name:");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        namePanel.add(nameLabel, BorderLayout.NORTH);
        
        JTextArea nameValue = new JTextArea(labResult.getResultName());
        nameValue.setWrapStyleWord(true);
        nameValue.setLineWrap(true);
        nameValue.setOpaque(false);
        nameValue.setEditable(false);
        nameValue.setBorder(null);
        // Set fixed width to prevent text flowing out of panel
        nameValue.setPreferredSize(new Dimension(220, nameValue.getPreferredSize().height));
        namePanel.add(nameValue, BorderLayout.CENTER);
        basicDetailsPanel.add(namePanel);
        basicDetailsPanel.add(Box.createVerticalStrut(5));
        
        // Add group name (with word wrap)
        JPanel groupPanel = new JPanel(new BorderLayout());
        JLabel groupLabel = new JLabel("Group:");
        groupLabel.setFont(groupLabel.getFont().deriveFont(Font.BOLD));
        groupPanel.add(groupLabel, BorderLayout.NORTH);
        
        String groupName = groupIdToNameMap.getOrDefault(labResult.getLabResultGroupId(), "Unknown Group");
        JTextArea groupValue = new JTextArea(groupName);
        groupValue.setWrapStyleWord(true);
        groupValue.setLineWrap(true);
        groupValue.setOpaque(false);
        groupValue.setEditable(false);
        groupValue.setBorder(null);
        // Set fixed width to prevent text flowing out of panel
        groupValue.setPreferredSize(new Dimension(220, groupValue.getPreferredSize().height));
        groupPanel.add(groupValue, BorderLayout.CENTER);
        basicDetailsPanel.add(groupPanel);
        basicDetailsPanel.add(Box.createVerticalStrut(5));
        
        // Add unit
        JPanel unitPanel = new JPanel(new BorderLayout());
        JLabel unitLabel = new JLabel("Unit:");
        unitLabel.setFont(unitLabel.getFont().deriveFont(Font.BOLD));
        unitPanel.add(unitLabel, BorderLayout.NORTH);
        unitPanel.add(new JLabel(labResult.getUnit() != null ? labResult.getUnit() : "N/A"), BorderLayout.CENTER);
        basicDetailsPanel.add(unitPanel);
        
        // Add basic details panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        detailsPanel.add(basicDetailsPanel, gbc);
        
        // Add measurement statistics if available
        if (!measurements.isEmpty()) {
            // Sort measurements by date
            measurements.sort(Comparator.comparing(Measurement::getDateTime).reversed());
            Measurement latest = measurements.get(0);
            
            // Create measurement stats panel
            JPanel statsPanel = new JPanel();
            statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
            statsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
            
            // Add latest value
            JPanel latestValuePanel = new JPanel(new BorderLayout());
            JLabel latestLabel = new JLabel("Latest Value:");
            latestLabel.setFont(latestLabel.getFont().deriveFont(Font.BOLD));
            latestValuePanel.add(latestLabel, BorderLayout.NORTH);
            
            JTextArea latestValue = new JTextArea(latest.getValue());
            latestValue.setWrapStyleWord(true);
            latestValue.setLineWrap(true);
            latestValue.setOpaque(false);
            latestValue.setEditable(false);
            latestValue.setBorder(null);
            // Set fixed width to prevent text flowing out of panel
            latestValue.setPreferredSize(new Dimension(220, latestValue.getPreferredSize().height));
            latestValuePanel.add(latestValue, BorderLayout.CENTER);
            statsPanel.add(latestValuePanel);
            statsPanel.add(Box.createVerticalStrut(5));
            
            // Add latest date
            JPanel datePanel = new JPanel(new BorderLayout());
            JLabel dateLabel = new JLabel("Latest Date:");
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD));
            datePanel.add(dateLabel, BorderLayout.NORTH);
            datePanel.add(new JLabel(latest.getDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))), BorderLayout.CENTER);
            statsPanel.add(datePanel);
            statsPanel.add(Box.createVerticalStrut(5));
            
            // Add total measurements
            JPanel totalPanel = new JPanel(new BorderLayout());
            JLabel totalLabel = new JLabel("Total Measurements:");
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
            totalPanel.add(totalLabel, BorderLayout.NORTH);
            totalPanel.add(new JLabel(String.valueOf(measurements.size())), BorderLayout.CENTER);
            statsPanel.add(totalPanel);
            
            // Add stats panel to main panel
            gbc.gridy = 1;
            detailsPanel.add(statsPanel, gbc);
            
            // Add measurement history
            gbc.gridy = 2;
            JLabel historyLabel = new JLabel("Measurement History");
            historyLabel.setFont(historyLabel.getFont().deriveFont(Font.BOLD));
            historyLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            detailsPanel.add(historyLabel, gbc);
            
            // Create history table with fixed column widths
            String[] columns = {"Date", "Value"};
            DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            // Add measurements to table (most recent first)
            for (int i = 0; i < measurements.size(); i++) {
                Measurement m = measurements.get(i);
                historyModel.addRow(new Object[]{
                    // Use shorter date format to fit in column width
                    m.getDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yy")),
                    m.getValue()
                });
            }
            
            JTable historyTable = new JTable(historyModel);
            historyTable.setFillsViewportHeight(true);
            historyTable.setShowGrid(true);
            historyTable.setGridColor(Color.LIGHT_GRAY);
            
            // Increase row height for better readability
            historyTable.setRowHeight(historyTable.getRowHeight() + 4);
            
            // Set fixed column widths with adequate space for Windows rendering
            historyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            historyTable.getColumnModel().getColumn(0).setMinWidth(100);
            historyTable.getColumnModel().getColumn(0).setMaxWidth(120);
            
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(120);
            historyTable.getColumnModel().getColumn(1).setMinWidth(80);
            
            // Enable cell text wrapping
            historyTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JTextArea area = new JTextArea(value != null ? value.toString() : "");
                    area.setWrapStyleWord(true);
                    area.setLineWrap(true);
                    area.setOpaque(true);
                    
                    // Set padding
                    area.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    
                    if (isSelected) {
                        area.setBackground(table.getSelectionBackground());
                        area.setForeground(table.getSelectionForeground());
                    } else {
                        area.setBackground(table.getBackground());
                        area.setForeground(table.getForeground());
                    }
                    return area;
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(historyTable);
            scrollPane.setPreferredSize(new Dimension(230, 150));
            
            gbc.gridy = 3;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            detailsPanel.add(scrollPane, gbc);
        } else {
            gbc.gridy = 1;
            detailsPanel.add(new JLabel("No measurements available"), gbc);
        }
        
        // Add the details panel to a scroll pane
        JScrollPane mainScrollPane = new JScrollPane(detailsPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultPanel.add(mainScrollPane, BorderLayout.CENTER);
        
        // Refresh the panel
        resultPanel.revalidate();
        resultPanel.repaint();
    }
    
    /**
     * Clear all data from the panel
     */
    public void clearData() {
        tableModel.setRowCount(0);
        
        chartPanel.removeAll();
        chartPanel.revalidate();
        chartPanel.repaint();
        
        resultPanel.removeAll();
        resultPanel.revalidate();
        resultPanel.repaint();
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