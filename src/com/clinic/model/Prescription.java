package com.clinic.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an electronic medical prescription created during a consultation (FR19, FR20, FR21).
 * Contains one or more medicines with their specific dosage, frequency, and duration.
 */
public class Prescription {
    private final String prescriptionId;
    private final String consultationId;
    private final List<PrescriptionItem> items;
    private String generalAdvice;
    private final LocalDateTime issuedAt;

    public Prescription(String prescriptionId, String consultationId) {
        this.prescriptionId = Objects.requireNonNull(prescriptionId, "prescriptionId required");
        this.consultationId = Objects.requireNonNull(consultationId, "consultationId required");
        this.items = new ArrayList<>();
        this.generalAdvice = "";
        this.issuedAt = LocalDateTime.now();
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getConsultationId() {
        return consultationId;
    }

    public List<PrescriptionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public synchronized void addItem(PrescriptionItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    public synchronized void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public String getGeneralAdvice() {
        return generalAdvice;
    }

    public void setGeneralAdvice(String generalAdvice) {
        this.generalAdvice = generalAdvice;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public String toString() {
        return "Prescription #" + prescriptionId + " (" + items.size() + " items)";
    }
}
