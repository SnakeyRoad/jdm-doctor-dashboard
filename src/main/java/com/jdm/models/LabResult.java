package com.jdm.models;

import java.util.Objects;

/**
 * Represents a laboratory result type
 */
public class LabResult {
    private String labResultId;
    private String labResultGroupId;
    private String patientId;
    private String resultName;
    private String unit;
    private String resultNameEnglish; // Added to support LabResultsEN.csv

    /**
     * Default constructor
     */
    public LabResult() {
    }

    /**
     * Constructor with all fields
     * 
     * @param labResultId The unique identifier for this lab result
     * @param labResultGroupId The group this lab result belongs to
     * @param patientId The patient's unique identifier
     * @param resultName The name of the result in original language
     * @param unit The unit of measurement
     */
    public LabResult(String labResultId, String labResultGroupId, String patientId, String resultName, String unit) {
        this.labResultId = labResultId;
        this.labResultGroupId = labResultGroupId;
        this.patientId = patientId;
        this.resultName = resultName;
        this.unit = unit;
    }

    /**
     * Constructor with English result name
     * 
     * @param labResultId The unique identifier for this lab result
     * @param labResultGroupId The group this lab result belongs to
     * @param patientId The patient's unique identifier
     * @param resultName The name of the result in original language
     * @param unit The unit of measurement
     * @param resultNameEnglish The name of the result in English
     */
    public LabResult(String labResultId, String labResultGroupId, String patientId, String resultName, String unit, String resultNameEnglish) {
        this.labResultId = labResultId;
        this.labResultGroupId = labResultGroupId;
        this.patientId = patientId;
        this.resultName = resultName;
        this.unit = unit;
        this.resultNameEnglish = resultNameEnglish;
    }

    // Getters and Setters
    public String getLabResultId() {
        return labResultId;
    }

    public void setLabResultId(String labResultId) {
        this.labResultId = labResultId;
    }

    public String getLabResultGroupId() {
        return labResultGroupId;
    }

    public void setLabResultGroupId(String labResultGroupId) {
        this.labResultGroupId = labResultGroupId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getResultNameEnglish() {
        return resultNameEnglish;
    }

    public void setResultNameEnglish(String resultNameEnglish) {
        this.resultNameEnglish = resultNameEnglish;
    }

    /**
     * Get the display name of the result, preferring English if available
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return (resultNameEnglish != null && !resultNameEnglish.isEmpty()) 
                ? resultNameEnglish : resultName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabResult labResult = (LabResult) o;
        return Objects.equals(labResultId, labResult.labResultId) &&
               Objects.equals(labResultGroupId, labResult.labResultGroupId) &&
               Objects.equals(patientId, labResult.patientId) &&
               Objects.equals(resultName, labResult.resultName) &&
               Objects.equals(unit, labResult.unit) &&
               Objects.equals(resultNameEnglish, labResult.resultNameEnglish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labResultId, labResultGroupId, patientId, resultName, unit, resultNameEnglish);
    }

    @Override
    public String toString() {
        return "LabResult{" +
                "labResultId='" + labResultId + '\'' +
                ", labResultGroupId='" + labResultGroupId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", resultName='" + resultName + '\'' +
                ", unit='" + unit + '\'' +
                ", resultNameEnglish='" + resultNameEnglish + '\'' +
                '}';
    }
}
