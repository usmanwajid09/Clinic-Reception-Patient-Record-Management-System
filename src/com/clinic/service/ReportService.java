package com.clinic.service;

import com.clinic.model.*;
import com.clinic.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete ReportService adhering to SRP and DIP.
 * Aggregates clinical metrics, doctor workloads, and financial summaries.
 */
public class ReportService implements IReportService {
    private final IAppointmentRepository appointmentRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientRepository patientRepository;
    private final IConsultationRepository consultationRepository;
    private final IBillRepository billRepository;

    public ReportService(IAppointmentRepository appointmentRepository,
                         IDoctorRepository doctorRepository,
                         IPatientRepository patientRepository,
                         IConsultationRepository consultationRepository,
                         IBillRepository billRepository) {
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "appointmentRepository required");
        this.doctorRepository = Objects.requireNonNull(doctorRepository, "doctorRepository required");
        this.patientRepository = Objects.requireNonNull(patientRepository, "patientRepository required");
        this.consultationRepository = Objects.requireNonNull(consultationRepository, "consultationRepository required");
        this.billRepository = Objects.requireNonNull(billRepository, "billRepository required");
    }

    @Override
    public AppointmentReportData getAppointmentReport(LocalDate startDate, LocalDate endDate) {
        List<Appointment> list = appointmentRepository.findByDateRange(startDate, endDate);

        int total = list.size();
        int scheduled = (int) list.stream().filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).count();
        int completed = (int) list.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        int cancelled = (int) list.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        int noShow = (int) list.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();

        return new AppointmentReportData(startDate, endDate, total, scheduled, completed, cancelled, noShow, list);
    }

    @Override
    public List<DoctorReportData> getDoctorPerformanceReports(LocalDate startDate, LocalDate endDate) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Consultation> consultations = consultationRepository.findByDateRange(startDate, endDate);
        List<Bill> bills = billRepository.findByDateRange(startDate, endDate);

        List<DoctorReportData> reports = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<Consultation> docConsults = consultations.stream()
                    .filter(c -> c.getDoctorId().equalsIgnoreCase(doctor.getDoctorId()))
                    .toList();

            int totalConsults = docConsults.size();
            int uniquePatients = (int) docConsults.stream()
                    .map(Consultation::getPatientId)
                    .distinct()
                    .count();

            double revenue = bills.stream()
                    .filter(b -> b.getDoctorId().equalsIgnoreCase(doctor.getDoctorId()) && b.isPaid())
                    .mapToDouble(Bill::getTotalAmount)
                    .sum();

            reports.add(new DoctorReportData(
                    doctor.getDoctorId(),
                    doctor.getName(),
                    doctor.getDepartment().getName(),
                    doctor.getSpecialization(),
                    totalConsults,
                    uniquePatients,
                    revenue
            ));
        }

        reports.sort(Comparator.comparingInt(DoctorReportData::totalConsultations).reversed());
        return reports;
    }

    @Override
    public PatientHistoryReportData getPatientVisitHistoryReport(String patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ValidationException("Patient not found: " + patientId));

        List<Consultation> history = consultationRepository.findByPatientId(patientId);
        return new PatientHistoryReportData(
                patient.getPatientId(),
                patient.getName(),
                patient.getContactNumber(),
                history.size(),
                history
        );
    }

    @Override
    public RevenueReportData getRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findByDateRange(startDate, endDate);

        int totalBills = bills.size();
        int paidCount = (int) bills.stream().filter(Bill::isPaid).count();
        int unpaidCount = totalBills - paidCount;

        double doctorFees = bills.stream().filter(Bill::isPaid).mapToDouble(Bill::getDoctorConsultationFee).sum();
        double clinicCharges = bills.stream().filter(Bill::isPaid).mapToDouble(Bill::getClinicServiceCharge).sum();
        double totalCollected = bills.stream().filter(Bill::isPaid).mapToDouble(Bill::getTotalAmount).sum();
        double outstanding = bills.stream().filter(b -> !b.isPaid()).mapToDouble(Bill::getTotalAmount).sum();

        return new RevenueReportData(
                startDate,
                endDate,
                totalBills,
                paidCount,
                unpaidCount,
                doctorFees,
                clinicCharges,
                totalCollected,
                outstanding,
                bills
        );
    }
}

// FR27-FR30: Multi-dimensional clinical report aggregation
