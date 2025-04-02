package com.jdm.models;

import java.util.Objects;

/**
 * Represents a group of laboratory result types
 */
public class LabResultGroup {
    private String labResultGroupId;
    private String groupName;

    /**
     * Default constructor
     */
    public LabResultGroup() {
    }

    /**
     * Constructor with all fields
     * 
     * @param labResultGroupId The unique identifier for this lab result group
     * @param groupName The name of the lab result group
     */
    public LabResultGroup(String labResultGroupId, String groupName) {
        this.labResultGroupId = labResultGroupId;
        this.groupName = groupName;
    }

    // Getters and Setters
    public String getLabResultGroupId() {
        return labResultGroupId;
    }

    public void setLabResultGroupId(String labResultGroupId) {
        this.labResultGroupId = labResultGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabResultGroup that = (LabResultGroup) o;
        return Objects.equals(labResultGroupId, that.labResultGroupId) &&
               Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labResultGroupId, groupName);
    }

    @Override
    public String toString() {
        return "LabResultGroup{" +
                "labResultGroupId='" + labResultGroupId + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
