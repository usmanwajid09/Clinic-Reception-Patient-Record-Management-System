package com.clinic.service;

import com.clinic.config.AppConfig;
import com.clinic.model.*;
import com.clinic.repository.IBillRepository;
import com.clinic.repository.IDoctorRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete BillingService adhering to SRP and DIP.
 * Calculates consultation billing (FR24), records payments (FR25),
 * and issues printable receipt tokens (FR26).
 */
public class BillingService implements IBillingService {
    private final IBillRepository billRepository;
    private final IDoctorRepository doctorRepository;

    public BillingService(IBillRepository billRepository, IDoctorRepository doctorRepository) {
        this.billRepository = Objects.requireNonNull(billRepository, "billRepository required");
        this.doctorRepository = Objects.requireNonNull(doctorRepository, "doctorRepository required");
    }

    @Override
    public synchronized Bill generateBillForConsultation(Consultation consultation) {
        Objects.requireNonNull(consultation, "Consultation required");

        // Check if bill already generated for this consultation
        Optional<Bill> existing = billRepository.findByConsultationId(consultation.getConsultationId());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Get doctor consultation fee
        double doctorFee = doctorRepository.findById(consultation.getDoctorId())
                .map(Doctor::getConsultationFee)
                .orElse(50.0);

        double clinicServiceCharge = AppConfig.FIXED_CLINIC_SERVICE_CHARGE;
        String billId = billRepository.generateNextBillId();

        Bill bill = new Bill(billId, consultation.getConsultationId(), consultation.getPatientId(),
                consultation.getDoctorId(), doctorFee, clinicServiceCharge);

        return billRepository.save(bill);
    }

    @Override
    public synchronized Bill recordPayment(String billId, PaymentMethod paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ValidationException("Bill not found with ID: " + billId));

        if (bill.isPaid()) {
            return bill;
        }

        String receiptNumber = billRepository.generateNextReceiptNumber();
        bill.recordPayment(paymentMethod, receiptNumber);

        return billRepository.save(bill);
    }

    @Override
    public List<Bill> getBillsByStatus(PaymentStatus status) {
        return billRepository.findByPaymentStatus(status);
    }

    @Override
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    public Optional<Bill> getBillById(String billId) {
        return billRepository.findById(billId);
    }

    @Override
    public Optional<Bill> getBillForConsultation(String consultationId) {
        return billRepository.findByConsultationId(consultationId);
    }

    @Override
    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return billRepository.findByDateRange(startDate, endDate);
    }
}

// FR24: Automatically calculates fee = docFee + fixedServiceCharge
