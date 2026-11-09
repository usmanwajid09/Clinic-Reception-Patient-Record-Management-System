package com.clinic.model;

/**
 * Status of a clinical bill payment (FR25).
 */
public enum PaymentStatus {
    UNPAID("Unpaid"),
    PAID("Paid");

    private final String displayName;

    PaymentStatus(String displayName) {
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
