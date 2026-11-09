package com.clinic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled patient appointment with a doctor (FR11, FR12, FR13, FR14, FR15, FR16).
 */
public class Appointment {
    private final String appointmentId;
    private final String patientId;
    private final String doctorId;
    private LocalDate appointmentDate;
    private TimeSlot timeSlot;
    private int tokenNumber;
    private AppointmentStatus status;
    private String notes;
    private final LocalDateTime createdAt;

    public Appointment(String appointmentId, String patientId, String doctorId,
                       LocalDate appointmentDate, TimeSlot timeSlot, int tokenNumber) {
        this.appointmentId = Objects.requireNonNull(appointmentId, "appointmentId required");
        this.patientId = Objects.requireNonNull(patientId, "patientId required");
        this.doctorId = Objects.requireNonNull(doctorId, "doctorId required");
        this.appointmentDate = Objects.requireNonNull(appointmentDate, "appointmentDate required");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot required");
        this.tokenNumber = tokenNumber;
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.notes = "";
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = Objects.requireNonNull(appointmentDate, "appointmentDate required");
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot required");
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(int tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = Objects.requireNonNull(status, "status required");
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment that)) return false;
        return appointmentId.equalsIgnoreCase(that.appointmentId);
    }

    @Override
    public int hashCode() {
        return appointmentId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Token #" + tokenNumber + " - " + appointmentDate + " (" + status + ")";
    }
}

// Immutable appointment token assigned sequentially
