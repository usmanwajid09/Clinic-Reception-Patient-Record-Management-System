package com.clinic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a registered patient in the clinic management system (FR1, FR3, FR4, FR5).
 */
public class Patient {
    private final String patientId;
    private String name;
    private LocalDate dateOfBirth;
    private String gender; // Male, Female, Other
    private String address;
    private String contactNumber;
    private String emergencyContact;
    private final LocalDateTime registeredAt;

    // List of consultation IDs associated with this patient (FR4)
    private final List<String> consultationHistoryIds;

    public Patient(String patientId, String name, LocalDate dateOfBirth, String gender,
                   String address, String contactNumber, String emergencyContact) {
        this.patientId = Objects.requireNonNull(patientId, "patientId required");
        this.name = Objects.requireNonNull(name, "name required");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "dateOfBirth required");
        this.gender = Objects.requireNonNull(gender, "gender required");
        this.address = Objects.requireNonNull(address, "address required");
        this.contactNumber = Objects.requireNonNull(contactNumber, "contactNumber required");
        this.emergencyContact = Objects.requireNonNull(emergencyContact, "emergencyContact required");
        this.registeredAt = LocalDateTime.now();
        this.consultationHistoryIds = new ArrayList<>();
    }

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public List<String> getConsultationHistoryIds() {
        return Collections.unmodifiableList(consultationHistoryIds);
    }

    public synchronized void addConsultationRecord(String consultationId) {
        if (consultationId != null && !consultationHistoryIds.contains(consultationId)) {
            consultationHistoryIds.add(consultationId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient patient)) return false;
        return patientId.equalsIgnoreCase(patient.patientId);
    }

    @Override
    public int hashCode() {
        return patientId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + patientId + ")";
    }
}

// SRP: Patient entity solely holds patient data and state
