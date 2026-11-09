package com.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an invoice / bill created for a completed consultation (FR24, FR25, FR26).
 * Computes total using doctor's consultation fee and fixed clinic service charges (FR24).
 */
public class Bill {
    private final String billId;
    private final String consultationId;
    private final String patientId;
    private final String doctorId;
    private final double doctorConsultationFee;
    private final double clinicServiceCharge;
    private final double totalAmount;
    private final LocalDateTime billedAt;

    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    private String receiptNumber;

    public Bill(String billId, String consultationId, String patientId, String doctorId,
                double doctorConsultationFee, double clinicServiceCharge) {
        this.billId = Objects.requireNonNull(billId, "billId required");
        this.consultationId = Objects.requireNonNull(consultationId, "consultationId required");
        this.patientId = Objects.requireNonNull(patientId, "patientId required");
        this.doctorId = Objects.requireNonNull(doctorId, "doctorId required");
        this.doctorConsultationFee = Math.max(0.0, doctorConsultationFee);
        this.clinicServiceCharge = Math.max(0.0, clinicServiceCharge);
        this.totalAmount = this.doctorConsultationFee + this.clinicServiceCharge;
        this.billedAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public String getBillId() {
        return billId;
    }

    public String getConsultationId() {
        return consultationId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public double getDoctorConsultationFee() {
        return doctorConsultationFee;
    }

    public double getClinicServiceCharge() {
        return clinicServiceCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getBilledAt() {
        return billedAt;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    /**
     * Records payment for this bill (FR25) and generates a printable receipt number (FR26).
     */
    public synchronized void recordPayment(PaymentMethod method, String receiptNumber) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentMethod = Objects.requireNonNull(method, "payment method required");
        this.paidAt = LocalDateTime.now();
        this.receiptNumber = receiptNumber != null ? receiptNumber : "REC-" + billId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill bill)) return false;
        return billId.equalsIgnoreCase(bill.billId);
    }

    @Override
    public int hashCode() {
        return billId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Bill #" + billId + " - Total: Rs. " + String.format("%.2f", totalAmount) + " (" + paymentStatus + ")";
    }
}
