package com.jdm.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a specific laboratory measurement/result value
 */
public class Measurement {
    private String measurementId;
    private String labResultId;
    private LocalDateTime dateTime;
    private String value;

    /**
     * Default constructor
     */
    public Measurement() {
    }

    /**
     * Constructor with all fields
     * 
     * @param measurementId The unique identifier for this measurement
     * @param labResultId The lab result type this measurement belongs to
     * @param dateTime The date and time the measurement was taken
     * @param value The value of the measurement
     */
    public Measurement(String measurementId, String labResultId, LocalDateTime dateTime, String value) {
        this.measurementId = measurementId;
        this.labResultId = labResultId;
        this.dateTime = dateTime;
        this.value = value;
    }

    // Getters and Setters
    public String getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
    }

    public String getLabResultId() {
        return labResultId;
    }

    public void setLabResultId(String labResultId) {
        this.labResultId = labResultId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Try to get the value as a numeric value (double)
     * 
     * @return The value as a double, or null if it cannot be parsed
     */
    public Double getNumericValue() {
        try {
            return Double.parseDouble(value.replace(",", ".").trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Measurement that = (Measurement) o;
        return Objects.equals(measurementId, that.measurementId) &&
               Objects.equals(labResultId, that.labResultId) &&
               Objects.equals(dateTime, that.dateTime) &&
               Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurementId, labResultId, dateTime, value);
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "measurementId='" + measurementId + '\'' +
                ", labResultId='" + labResultId + '\'' +
                ", dateTime=" + dateTime +
                ", value='" + value + '\'' +
                '}';
    }
}
