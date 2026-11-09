package com.clinic.service;

import com.clinic.model.Bill;
import com.clinic.model.Consultation;
import com.clinic.model.PaymentMethod;
import com.clinic.model.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service contract for Billing & Payments (FR24, FR25, FR26).
 */
public interface IBillingService {
    Bill generateBillForConsultation(Consultation consultation);

    Bill recordPayment(String billId, PaymentMethod paymentMethod);

    List<Bill> getBillsByStatus(PaymentStatus status);

    List<Bill> getAllBills();

    Optional<Bill> getBillById(String billId);

    Optional<Bill> getBillForConsultation(String consultationId);

    List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate);
}

