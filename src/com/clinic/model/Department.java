package com.clinic.model;

import java.util.Objects;

/**
 * Represents a medical department within the clinic (FR7).
 */
public class Department {
    private final String departmentId;
    private String name;
    private String description;

    public Department(String departmentId, String name, String description) {
        this.departmentId = Objects.requireNonNull(departmentId, "departmentId required");
        this.name = Objects.requireNonNull(name, "department name required");
        this.description = description != null ? description : "";
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return departmentId.equalsIgnoreCase(that.departmentId);
    }

    @Override
    public int hashCode() {
        return departmentId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}

