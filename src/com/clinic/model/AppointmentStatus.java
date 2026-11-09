package com.clinic.model;

/**
 * Represents the lifecycle status of an appointment (FR15).
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No-Show");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
