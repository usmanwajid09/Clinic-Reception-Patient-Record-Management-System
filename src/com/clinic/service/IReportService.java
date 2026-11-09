package com.clinic.service;

import com.clinic.model.Appointment;
import com.clinic.model.Bill;
import com.clinic.model.Consultation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service contract for Analytical & Operational Reports (FR27, FR28, FR29, FR30).
 */
public interface IReportService {
    /**
     * Daily and date-range appointment reports (FR27).
     */
    AppointmentReportData getAppointmentReport(LocalDate startDate, LocalDate endDate);

    /**
     * Doctor-wise patient and consultation reports (FR28).
     */
    List<DoctorReportData> getDoctorPerformanceReports(LocalDate startDate, LocalDate endDate);

    /**
     * Patient visit-history reports (FR29).
     */
    PatientHistoryReportData getPatientVisitHistoryReport(String patientId);

    /**
     * Daily and date-range revenue reports (FR30).
     */
    RevenueReportData getRevenueReport(LocalDate startDate, LocalDate endDate);

    // ==========================================
    // Report Data Transfer Objects (DTOs)
    // ==========================================

    record AppointmentReportData(
            LocalDate startDate,
            LocalDate endDate,
            int totalAppointments,
            int scheduledCount,
            int completedCount,
            int cancelledCount,
            int noShowCount,
            List<Appointment> appointments
    ) {}

    record DoctorReportData(
            String doctorId,
            String doctorName,
            String department,
            String specialization,
            int totalConsultations,
            int uniquePatientsSeen,
            double totalRevenueGenerated
    ) {}

    record PatientHistoryReportData(
            String patientId,
            String patientName,
            String contactNumber,
            int totalVisits,
            List<Consultation> consultationHistory
    ) {}

    record RevenueReportData(
            LocalDate startDate,
            LocalDate endDate,
            int totalBillsIssued,
            int paidBillsCount,
            int unpaidBillsCount,
            double totalDoctorFees,
            double totalClinicServiceCharges,
            double totalRevenueCollected,
            double totalOutstandingAmount,
            List<Bill> bills
    ) {}
}

