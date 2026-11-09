package com.clinic.repository;

import com.clinic.model.Bill;
import com.clinic.model.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Billing & Invoicing (FR24, FR25, FR26, FR30).
 */
public interface IBillRepository extends IRepository<Bill, String> {
    /**
     * Finds bill associated with a specific consultation ID (FR24).
     */
    Optional<Bill> findByConsultationId(String consultationId);

    /**
     * Finds bills by payment status (Unpaid / Paid) (FR25).
     */
    List<Bill> findByPaymentStatus(PaymentStatus status);

    /**
     * Finds bills generated or paid within a specified date range (FR30).
     */
    List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Generates next available unique bill ID.
     */
    String generateNextBillId();

    /**
     * Generates a unique sequential receipt number for paid bills (FR26).
     */
    String generateNextReceiptNumber();
}

// FR26: Sequential receipt token contract
