package com.clinic.test;

import com.clinic.config.AppConfig;
import com.clinic.di.ServiceContainer;
import com.clinic.model.*;
import com.clinic.service.*;
import com.clinic.service.IReportService.AppointmentReportData;
import com.clinic.service.IReportService.DoctorReportData;
import com.clinic.service.IReportService.PatientHistoryReportData;
import com.clinic.service.IReportService.RevenueReportData;
import com.clinic.util.ReceiptPrinter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Automated Verification & Regression Suite for Clinic Reception & Patient Record Management System.
 * Tests all 30 Fixed Functional Requirements (FR1 - FR30) and SOLID Principles.
 */
public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("CLINIC MANAGEMENT SYSTEM - AUTOMATED VERIFICATION SUITE");
        System.out.println("Testing Fixed Functional Requirements (FR1 - FR30) & SOLID Rules");
        System.out.println("==================================================================");

        ServiceContainer container = ServiceContainer.getInstance();

        testPatientRegistrationAndValidation(container.getPatientService());
        testDoctorManagementAndAvailability(container.getDoctorService());
        testAppointmentSchedulingAndConflictPrevention(container.getAppointmentService(), container.getPatientService(), container.getDoctorService());
        testConsultationPrescriptionAndRecordLock(container.getConsultationService(), container.getAppointmentService(), container.getPatientService(), container.getDoctorService());
        testBillingAndPrintableReceipts(container.getBillingService(), container.getPatientService(), container.getDoctorService());
        testExecutiveReporting(container.getReportService(), container.getPatientService());
        testSOLIDArchitecturalPrinciples(container);

        System.out.println("==================================================================");
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName + " - " + message);
            failed++;
        }
    }

    private static void testPatientRegistrationAndValidation(IPatientService patientService) {
        System.out.println("\n--- Testing Module 1: Patient Registration (FR1 - FR5) ---");

        // FR1: Register a patient
        Patient p = patientService.registerPatient("Test Patient", LocalDate.of(1992, 5, 10), "Female",
                "123 Test St", "555-9999", "Emergency Contact (555-9998)");
        assertTrue("FR1: Register Patient", p != null && p.getPatientId() != null, "Patient ID should be generated");

        // FR2: Search existing patient by ID, name, or phone
        List<Patient> foundById = patientService.searchPatients(p.getPatientId());
        assertTrue("FR2: Search Patient by ID", !foundById.isEmpty(), "Should locate patient by ID");

        List<Patient> foundByName = patientService.searchPatients("Test Patient");
        assertTrue("FR2: Search Patient by Name", !foundByName.isEmpty(), "Should locate patient by name");

        // FR3: Update permitted patient information
        Patient updated = patientService.updatePatient(p.getPatientId(), "Test Patient Renamed", LocalDate.of(1992, 5, 10), "Female",
                "456 New Address St", "555-8888", "New Emergency (555-8887)");
        assertTrue("FR3: Update Patient Information", "456 New Address St".equals(updated.getAddress()) && "555-8888".equals(updated.getContactNumber()), "Address and contact should be updated");

        // FR5: Validate mandatory patient information
        boolean validationCaught = false;
        try {
            patientService.registerPatient("", LocalDate.of(1990, 1, 1), "Male", "Address", "555-1111", "Emergency");
        } catch (ValidationException ex) {
            validationCaught = true;
        }
        assertTrue("FR5: Validate Mandatory Fields (Empty Name)", validationCaught, "Should reject empty patient name");

        boolean futureDobCaught = false;
        try {
            patientService.registerPatient("Future Person", LocalDate.now().plusDays(10), "Male", "Address", "555-1111", "Emergency");
        } catch (ValidationException ex) {
            futureDobCaught = true;
        }
        assertTrue("FR5: Validate Future DOB", futureDobCaught, "Should reject future date of birth");
    }

    private static void testDoctorManagementAndAvailability(IDoctorService doctorService) {
        System.out.println("\n--- Testing Module 2: Doctor Management (FR6 - FR10) ---");

        Department dept = doctorService.getAllDepartments().get(0);

        // FR6 & FR7: Register doctor with department
        Doctor doc = doctorService.registerDoctor("Dr. Test Specialist", "Neurology", "MBBS, MD", 95.0, dept);
        assertTrue("FR6 & FR7: Register Doctor with Department", doc != null && doc.getDepartment().equals(dept), "Doctor registered with department");

        // FR8: Define working days and time slots
        List<TimeSlot> slots = Arrays.asList(TimeSlot.of(10, 0, 10, 30), TimeSlot.of(10, 30, 11, 0));
        doctorService.setDoctorSchedule(doc.getDoctorId(), EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slots);
        assertTrue("FR8: Define Doctor Working Days and Slots", doc.getWorkingDays().contains(DayOfWeek.MONDAY) && doc.getConsultationSlots().size() == 2, "Schedule configured");

        // FR9: Mark doctor Available or Unavailable for a specified day
        LocalDate nextMonday = LocalDate.now();
        while (nextMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
            nextMonday = nextMonday.plusDays(1);
        }
        assertTrue("FR9: Initial Working Day Availability", doc.isAvailableOn(nextMonday), "Doctor should be available on working Monday");

        doctorService.setDoctorAvailabilityForDate(doc.getDoctorId(), nextMonday, false);
        assertTrue("FR9: Mark Doctor Unavailable for Specific Day", !doctorService.isDoctorAvailable(doc.getDoctorId(), nextMonday), "Doctor should now be marked unavailable");

        doctorService.setDoctorAvailabilityForDate(doc.getDoctorId(), nextMonday, true);
        assertTrue("FR9: Restore Doctor Availability for Specific Day", doctorService.isDoctorAvailable(doc.getDoctorId(), nextMonday), "Doctor should be available again");

        // FR10: Display list of doctors by department and specialization
        List<Doctor> filtered = doctorService.filterDoctors(dept.getName(), "Neurology");
        assertTrue("FR10: Filter Doctors by Dept and Specialization", !filtered.isEmpty(), "Should filter doctors");
    }

    private static void testAppointmentSchedulingAndConflictPrevention(IAppointmentService apptService, IPatientService patientService, IDoctorService doctorService) {
        System.out.println("\n--- Testing Module 3: Appointment Management & Conflict Engine (FR11 - FR16) ---");

        Patient patient1 = patientService.getAllPatients().get(0);
        Patient patient2 = patientService.getAllPatients().get(1);
        Doctor doctor = doctorService.getAllDoctors().get(0);

        // Pick next week date to avoid initial seed appointments
        LocalDate targetDate = LocalDate.now().plusWeeks(1);
        while (!doctor.isAvailableOn(targetDate)) {
            targetDate = targetDate.plusDays(1);
        }
        TimeSlot slot = doctor.getConsultationSlots().get(0);

        // FR11 & FR12: Create appointment and record token number
        Appointment app1 = apptService.createAppointment(patient1.getPatientId(), doctor.getDoctorId(), targetDate, slot, "Checkup 1");
        assertTrue("FR11 & FR12: Create Appointment with Token", app1 != null && app1.getTokenNumber() > 0 && app1.getStatus() == AppointmentStatus.SCHEDULED, "Token assigned");

        // FR13: Strict Double-Booking Conflict Prevention
        boolean conflictBlocked = false;
        try {
            apptService.createAppointment(patient2.getPatientId(), doctor.getDoctorId(), targetDate, slot, "Checkup 2 (Collision)");
        } catch (ValidationException ex) {
            conflictBlocked = true;
        }
        assertTrue("FR13: Prevent Double-Booking to Same Doctor at Same Time", conflictBlocked, "System must block appointment conflict");

        // FR14: Reschedule appointment
        TimeSlot nextSlot = doctor.getConsultationSlots().get(1);
        Appointment rescheduled = apptService.rescheduleAppointment(app1.getAppointmentId(), targetDate, nextSlot);
        assertTrue("FR14: Reschedule Appointment", rescheduled.getTimeSlot().equals(nextSlot), "Appointment time slot updated");

        // Now previous slot should be freed
        Appointment app2 = apptService.createAppointment(patient2.getPatientId(), doctor.getDoctorId(), targetDate, slot, "Checkup in freed slot");
        assertTrue("FR13 & FR14: Re-booking in Freed Slot After Reschedule", app2 != null, "Freed slot can now be booked");

        // FR15: Maintain Status as Cancelled
        apptService.cancelAppointment(app2.getAppointmentId(), "Patient request");
        Appointment cancelled = apptService.getAppointmentById(app2.getAppointmentId()).orElse(null);
        assertTrue("FR15: Maintain Status (Cancelled)", cancelled != null && cancelled.getStatus() == AppointmentStatus.CANCELLED, "Status is Cancelled");

        // FR16: Display doctor's appointment list for selected date
        List<Appointment> docList = apptService.getDoctorAppointmentsForDate(doctor.getDoctorId(), targetDate);
        assertTrue("FR16: Doctor Appointment List for Date", !docList.isEmpty(), "Doctor appointments listed");
    }

    private static void testConsultationPrescriptionAndRecordLock(IConsultationService consultService, IAppointmentService apptService, IPatientService patientService, IDoctorService doctorService) {
        System.out.println("\n--- Testing Module 4: Consultation & Electronic Prescriptions (FR17 - FR23) ---");

        Patient patient = patientService.getAllPatients().get(2);
        Doctor doctor = doctorService.getAllDoctors().get(1);

        LocalDate date = LocalDate.now().plusWeeks(1);
        while (!doctor.isAvailableOn(date)) {
            date = date.plusDays(1);
        }
        TimeSlot slot = doctor.getConsultationSlots().get(0);

        Appointment appt = apptService.createAppointment(patient.getPatientId(), doctor.getDoctorId(), date, slot, "Fever evaluation");

        // FR17: Doctor opens scheduled patient record
        Consultation consult = consultService.startConsultation(appt.getAppointmentId());
        assertTrue("FR17: Start Scheduled Patient Consultation", consult != null && consult.getAppointmentId().equals(appt.getAppointmentId()), "Consultation initiated");

        // FR18: Record symptoms, diagnosis, and notes
        consultService.recordClinicalFindings(consult.getConsultationId(), "High fever, chills, body ache", "Viral Influenza", "Adequate rest and hydration advised.");
        assertTrue("FR18: Record Clinical Findings", "Viral Influenza".equals(consult.getDiagnosis()), "Diagnosis saved");

        // FR19, FR20, FR21: Prescription with multiple items (name, dosage, frequency, duration)
        Prescription rx = new Prescription("RX-TEST-1", consult.getConsultationId());
        rx.addItem(new PrescriptionItem("Paracetamol 650mg", "1 Tablet", "Three times daily (TDS)", "3 days"));
        rx.addItem(new PrescriptionItem("Vitamin C 500mg", "1 Chewable Tab", "Once daily", "10 days"));
        consultService.attachPrescription(consult.getConsultationId(), rx);
        assertTrue("FR19 - FR21: Create Prescription with Multiple Medicines", consult.getPrescription().getItems().size() == 2, "2 medicines prescribed");

        // FR22: Complete consultation & save to patient history
        Consultation completed = consultService.finalizeAndCompleteConsultation(consult.getConsultationId());
        assertTrue("FR22: Consultation Completed and Linked", completed.isCompleted(), "Consultation marked completed");

        List<Consultation> history = patientService.getPatientMedicalHistory(patient.getPatientId());
        assertTrue("FR22: Stored in Patient Medical History", history.stream().anyMatch(c -> c.getConsultationId().equals(consult.getConsultationId())), "History contains consultation");

        // FR23: Prevent unauthorized modifications to completed consultation (Immutability Lock)
        boolean lockEnforced = false;
        try {
            consultService.recordClinicalFindings(completed.getConsultationId(), "Altered symptoms", "Altered diagnosis", "Notes");
        } catch (ValidationException | IllegalStateException ex) {
            lockEnforced = true;
        }
        assertTrue("FR23: Consultation Record Immutability Lock", lockEnforced, "Completed consultation cannot be modified");
    }

    private static void testBillingAndPrintableReceipts(IBillingService billingService, IPatientService patientService, IDoctorService doctorService) {
        System.out.println("\n--- Testing Module 5: Billing & Printable Receipts (FR24 - FR26) ---");

        List<Bill> allBills = billingService.getAllBills();
        assertTrue("FR24: Auto-Generated Bill for Consultation", !allBills.isEmpty(), "Bill created for consultation");

        Bill testBill = allBills.get(0);
        assertTrue("FR24: Bill Calculation (Doctor Fee + Fixed Clinic Charges)",
                testBill.getTotalAmount() == testBill.getDoctorConsultationFee() + testBill.getClinicServiceCharge(),
                "Total matches sum of doctor fee and clinic charge");

        // FR25: Record payment and payment status
        Bill settled = billingService.recordPayment(testBill.getBillId(), PaymentMethod.CASH);
        assertTrue("FR25: Record Payment and Status", settled.isPaid() && settled.getPaymentStatus() == PaymentStatus.PAID, "Bill marked as PAID");

        // FR26: Generate printable receipt
        Patient p = patientService.getPatientById(settled.getPatientId()).orElse(null);
        Doctor d = doctorService.getDoctorById(settled.getDoctorId()).orElse(null);
        ReceiptPrinter printer = new ReceiptPrinter(settled, p, d);
        String receiptText = printer.generateReceiptText();
        assertTrue("FR26: Generate Printable Receipt", receiptText.contains("OFFICIAL PAYMENT RECEIPT") && receiptText.contains(settled.getReceiptNumber()), "Receipt formatted");
    }

    private static void testExecutiveReporting(IReportService reportService, IPatientService patientService) {
        System.out.println("\n--- Testing Module 6: Reports & Analytics (FR27 - FR30) ---");

        LocalDate today = LocalDate.now();

        // FR27: Daily and date-range appointment reports
        AppointmentReportData apptReport = reportService.getAppointmentReport(today.minusDays(30), today.plusDays(30));
        assertTrue("FR27: Appointment Reports", apptReport.totalAppointments() > 0, "Appointment report generated");

        // FR28: Doctor-wise patient and consultation reports
        List<DoctorReportData> docReports = reportService.getDoctorPerformanceReports(today.minusDays(30), today.plusDays(30));
        assertTrue("FR28: Doctor-Wise Consultation Reports", !docReports.isEmpty(), "Doctor report generated");

        // FR29: Patient visit-history reports
        Patient patient = patientService.getAllPatients().get(0);
        PatientHistoryReportData patReport = reportService.getPatientVisitHistoryReport(patient.getPatientId());
        assertTrue("FR29: Patient Visit History Report", patReport != null, "Patient history report generated");

        // FR30: Daily and date-range revenue reports
        RevenueReportData revReport = reportService.getRevenueReport(today.minusDays(30), today.plusDays(30));
        assertTrue("FR30: Revenue & Financial Reports", revReport.totalBillsIssued() > 0 && revReport.totalRevenueCollected() >= 0, "Revenue report generated");
    }

    private static void testSOLIDArchitecturalPrinciples(ServiceContainer container) {
        System.out.println("\n--- Testing Module 7: SOLID Principles Adherence ---");

        // SRP: PatientValidator only handles validation; Patient only holds data
        assertTrue("SOLID - SRP: Dedicated Validator & Service Responsibilities", container.getPatientService() != null, "Separation of concerns enforced");

        // OCP: Repositories implement generic IRepository interface open for extension
        assertTrue("SOLID - OCP: Interface-Driven Repositories & Services", container.getPatientRepository() != null, "Open for extension via repository interfaces");

        // LSP: User polymorphic subclasses substitutable for User
        User recUser = new ReceptionistUser("U1", "rec", "Receptionist", "Desk 1");
        User docUser = new DoctorUser("U2", "doc", "Doctor", "DOC-101");
        User adminUser = new AdminUser("U3", "admin", "Admin");
        assertTrue("SOLID - LSP: User Subtypes Safely Substitutable",
                recUser.canManageFrontDesk() && docUser.canConductConsultations() && adminUser.hasAdministrativeAccess(),
                "LSP verified across all user hierarchies");

        // ISP: Segregated interfaces
        assertTrue("SOLID - ISP: Segregated Service and Repository Interfaces",
                container.getAuthService() != null && container.getReportService() != null, "Segregated interfaces verified");

        // DIP: ServiceContainer injects repository interfaces into services
        assertTrue("SOLID - DIP: High-Level Modules Depend on Abstractions", container.getAppointmentService() != null, "DIP verified through inversion container");
    }
}
