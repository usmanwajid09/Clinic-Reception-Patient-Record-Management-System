package com.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a doctor-patient consultation session (FR17, FR18, FR19, FR22, FR23).
 * Enforces record immutability once completed (FR23).
 */
public class Consultation {
    private final String consultationId;
    private final String appointmentId;
    private final String patientId;
    private final String doctorId;
    private final LocalDateTime consultationTime;

    private String symptoms;
    private String diagnosis;
    private String notes;
    private Prescription prescription; // One prescription per consultation (FR19)
    private boolean completed; // Once true, consultation is locked (FR23)

    public Consultation(String consultationId, String appointmentId, String patientId, String doctorId) {
        this.consultationId = Objects.requireNonNull(consultationId, "consultationId required");
        this.appointmentId = Objects.requireNonNull(appointmentId, "appointmentId required");
        this.patientId = Objects.requireNonNull(patientId, "patientId required");
        this.doctorId = Objects.requireNonNull(doctorId, "doctorId required");
        this.consultationTime = LocalDateTime.now();
        this.symptoms = "";
        this.diagnosis = "";
        this.notes = "";
        this.completed = false;
    }

    public String getConsultationId() {
        return consultationId;
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

    public LocalDateTime getConsultationTime() {
        return consultationTime;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        assertNotCompleted();
        this.symptoms = symptoms != null ? symptoms : "";
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        assertNotCompleted();
        this.diagnosis = diagnosis != null ? diagnosis : "";
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        assertNotCompleted();
        this.notes = notes != null ? notes : "";
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        assertNotCompleted();
        this.prescription = prescription;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Completes and finalizes this consultation.
     * After this call, records become immutable (FR23).
     */
    public synchronized void complete() {
        this.completed = true;
    }

    private void assertNotCompleted() {
        if (completed) {
            throw new IllegalStateException("Consultation is finalized and locked. Modifications are forbidden (FR23).");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consultation that)) return false;
        return consultationId.equalsIgnoreCase(that.consultationId);
    }

    @Override
    public int hashCode() {
        return consultationId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Consultation #" + consultationId + " (" + (completed ? "Completed" : "In-Progress") + ")";
    }
}

// FR23: Strict immutability guard prevents post-completion tampering
