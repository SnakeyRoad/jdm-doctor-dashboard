package com.jdm.models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a CMAS (Children's Myositis Assessment Scale) measurement
 * for tracking JDM (Juvenile Dermatomyositis) disease progression.
 */
public class CMAS {
    private int id;
    private LocalDate date;
    private String category;
    private int value;
    private String patientId;

    /**
     * Default constructor
     */
    public CMAS() {
    }

    /**
     * Constructor with all fields
     * 
     * @param id The unique identifier for this CMAS record
     * @param date The date the measurement was taken
     * @param category The CMAS category (e.g., "CMAS Score 10" or "CMAS Score 4-9")
     * @param value The actual numerical score value
     * @param patientId The patient's unique identifier
     */
    public CMAS(int id, LocalDate date, String category, int value, String patientId) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.value = value;
        this.patientId = patientId;
    }

    /**
     * Constructor without ID (for new records)
     * 
     * @param date The date the measurement was taken
     * @param category The CMAS category (e.g., "CMAS Score 10" or "CMAS Score 4-9")
     * @param value The actual numerical score value
     * @param patientId The patient's unique identifier
     */
    public CMAS(LocalDate date, String category, int value, String patientId) {
        this.date = date;
        this.category = category;
        this.value = value;
        this.patientId = patientId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CMAS cmas = (CMAS) o;
        return id == cmas.id &&
                value == cmas.value &&
                Objects.equals(date, cmas.date) &&
                Objects.equals(category, cmas.category) &&
                Objects.equals(patientId, cmas.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, category, value, patientId);
    }

    @Override
    public String toString() {
        return "CMAS{" +
                "id=" + id +
                ", date=" + date +
                ", category='" + category + '\'' +
                ", value=" + value +
                ", patientId='" + patientId + '\'' +
                '}';
    }
}
