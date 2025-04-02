package com.jdm.ui;

import com.jdm.controllers.CMASController;
import com.jdm.models.CMAS;
import com.jdm.models.Patient;
import com.jdm.utils.DateUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel for displaying CMAS charts and statistics
 */
public class CMASChartPanel extends JPanel {
    
    private final CMASController cmasController;
    
    private Patient currentPatient;
    private JPanel chartPanel;
    private JPanel statsPanel;
    
    /**
     * Constructor
     * 
     * @param cmasController Controller for CMAS operations
     */
    public CMASChartPanel(CMASController cmasController) {
        this.cmasController = cmasController;
        
        initializeUI();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set layout
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(new TitledBorder("CMAS Progress Chart"));
        add(chartPanel, BorderLayout.CENTER);
        
        // Create statistics panel
        statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create the statistics panel
     * 
     * @return Statistics panel
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(new TitledBorder("CMAS Statistics"));
        
        // Placeholder stats panels
        panel.add(createStatCard("Current Score", "N/A"));
        panel.add(createStatCard("Average Score", "N/A"));
        panel.add(createStatCard("Trend", "N/A"));
        
        return panel;
    }
    
    /**
     * Create a card for displaying a statistic
     * 
     * @param title Title of the statistic
     * @param value Initial value
     * @return Panel with the statistic
     */
    private JPanel createStatCard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(valueLabel.getFont().getName(), Font.PLAIN, 24));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Load patient data and update the chart
     * 
     * @param patient Patient to display
     */
    public void loadPatientData(Patient patient) {
        this.currentPatient = patient;
        updateChart();
    }
    
    /**
     * Update the chart based on current filters
     */
    private void updateChart() {
        if (currentPatient == null) {
            return;
        }
        
        try {
            // Get CMAS data for the patient
            List<CMAS> cmasEntries = cmasController.getAllCMAS(currentPatient.getPatientId());
            
            // Create and display the chart
            if (!cmasEntries.isEmpty()) {
                createCMASChart(cmasEntries);
                updateStatistics(cmasEntries);
            } else {
                showNoDataMessage();
            }
        } catch (SQLException e) {
            showError("Error loading CMAS data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create and display the CMAS chart
     * 
     * @param cmasEntries List of CMAS entries to display
     */
    private void createCMASChart(List<CMAS> cmasEntries) {
        // Group entries by category
        Map<String, List<CMAS>> entriesByCategory = cmasEntries.stream()
            .collect(Collectors.groupingBy(CMAS::getCategory));
        
        // Create time series dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        // Add a series for each category
        for (Map.Entry<String, List<CMAS>> entry : entriesByCategory.entrySet()) {
            String category = entry.getKey();
            List<CMAS> categoryEntries = entry.getValue();
            
            TimeSeries series = new TimeSeries(category);
            
            for (CMAS cmas : categoryEntries) {
                // Convert LocalDate to java.util.Date for JFreeChart
                LocalDate localDate = cmas.getDate();
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
                Date date = calendar.getTime();
                
                series.add(new Day(date), cmas.getValue());
            }
            
            dataset.addSeries(series);
        }
        
        // Create the chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "CMAS Progress Over Time",  // title
            "Date",                    // x-axis label
            "CMAS Score",              // y-axis label
            dataset,                   // data
            true,                      // include legend
            true,                      // tooltips
            false                      // URLs
        );
        
        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        
        // Set the renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        if (dataset.getSeriesCount() > 1) {
            renderer.setSeriesPaint(1, Color.RED);
        }
        plot.setRenderer(renderer);
        
        // Remove existing components from chart panel
        chartPanel.removeAll();
        
        // Add the chart to the panel
        ChartPanel jfreeChartPanel = new ChartPanel(chart);
        jfreeChartPanel.setPreferredSize(new Dimension(600, 400));
        jfreeChartPanel.setMouseWheelEnabled(true);
        chartPanel.add(jfreeChartPanel, BorderLayout.CENTER);
        
        // Repaint the panel
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * Update the statistics panel with data
     * 
     * @param cmasEntries List of CMAS entries
     */
    private void updateStatistics(List<CMAS> cmasEntries) {
        // Calculate statistics
        if (!cmasEntries.isEmpty()) {
            // Sort by date
            cmasEntries.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            
            // Get current (latest) score
            CMAS latestEntry = cmasEntries.get(cmasEntries.size() - 1);
            int currentScore = latestEntry.getValue();
            
            // Calculate average score
            double avgScore = cmasEntries.stream()
                .mapToInt(CMAS::getValue)
                .average()
                .orElse(0.0);
            
            // Determine trend
            String trendText;
            if (cmasEntries.size() > 1) {
                CMAS firstEntry = cmasEntries.get(0);
                int firstScore = firstEntry.getValue();
                
                double changePercent = ((double) currentScore - firstScore) / firstScore * 100;
                
                if (changePercent > 5) {
                    trendText = String.format("+%.1f%%", changePercent);
                } else if (changePercent < -5) {
                    trendText = String.format("%.1f%%", changePercent);
                } else {
                    trendText = "Stable";
                }
            } else {
                trendText = "N/A";
            }
            
            // Update stats panel
            statsPanel.removeAll();
            statsPanel.add(createStatCard("Current Score", String.valueOf(currentScore)));
            statsPanel.add(createStatCard("Average Score", String.format("%.1f", avgScore)));
            statsPanel.add(createStatCard("Trend", trendText));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        }
    }
    
    /**
     * Show a message when no data is available
     */
    private void showNoDataMessage() {
        chartPanel.removeAll();
        
        JLabel noDataLabel = new JLabel("No CMAS data available for the selected filters.");
        noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chartPanel.add(noDataLabel, BorderLayout.CENTER);
        
        chartPanel.revalidate();
        chartPanel.repaint();
        
        // Reset statistics
        statsPanel.removeAll();
        statsPanel.add(createStatCard("Current Score", "N/A"));
        statsPanel.add(createStatCard("Average Score", "N/A"));
        statsPanel.add(createStatCard("Trend", "N/A"));
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }
    
    /**
     * Clear all data from the panel
     */
    public void clearData() {
        chartPanel.removeAll();
        statsPanel.removeAll();
        
        statsPanel.add(createStatCard("Current Score", "N/A"));
        statsPanel.add(createStatCard("Average Score", "N/A"));
        statsPanel.add(createStatCard("Trend", "N/A"));
        
        chartPanel.revalidate();
        chartPanel.repaint();
        statsPanel.revalidate();
        statsPanel.repaint();
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
