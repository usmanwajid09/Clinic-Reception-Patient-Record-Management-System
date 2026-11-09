package com.clinic.model;

import java.util.Objects;

/**
 * Represents an individual prescribed medicine in a prescription (FR20, FR21).
 * Records medicine name, dosage, frequency, and duration.
 */
public class PrescriptionItem {
    private final String medicineName;
    private final String dosage;
    private final String frequency;
    private final String duration;
    private final String instructions;

    public PrescriptionItem(String medicineName, String dosage, String frequency, String duration) {
        this(medicineName, dosage, frequency, duration, "");
    }

    public PrescriptionItem(String medicineName, String dosage, String frequency, String duration, String instructions) {
        this.medicineName = Objects.requireNonNull(medicineName, "medicineName required").trim();
        this.dosage = Objects.requireNonNull(dosage, "dosage required").trim();
        this.frequency = Objects.requireNonNull(frequency, "frequency required").trim();
        this.duration = Objects.requireNonNull(duration, "duration required").trim();
        this.instructions = instructions != null ? instructions.trim() : "";
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDuration() {
        return duration;
    }

    public String getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        return medicineName + " | " + dosage + " | " + frequency + " | " + duration;
    }
}

