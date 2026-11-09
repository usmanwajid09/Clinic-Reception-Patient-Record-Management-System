package com.clinic.model;

/**
 * Enumeration representing user roles in the clinic management system.
 * Governs role-based access control (RBAC).
 */
public enum Role {
    RECEPTIONIST("Receptionist", "Front desk operations, patient intake, appointment booking, billing"),
    DOCTOR("Doctor", "Clinical consultations, medical history review, electronic prescriptions"),
    ADMINISTRATOR("Administrator", "Doctor registration, schedule management, clinic configurations, audit & reports");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
