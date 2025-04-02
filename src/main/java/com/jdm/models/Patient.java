package com.jdm.models;

import java.util.Objects;

/**
 * Represents a patient with JDM (Juvenile Dermatomyositis)
 */
public class Patient {
    private String patientId;
    private String name;

    /**
     * Default constructor
     */
    public Patient() {
    }

    /**
     * Constructor with all fields
     * 
     * @param patientId The unique identifier for this patient
     * @param name The patient's name
     */
    public Patient(String patientId, String name) {
        this.patientId = patientId;
        this.name = name;
    }

    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(patientId, patient.patientId) &&
               Objects.equals(name, patient.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, name);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
