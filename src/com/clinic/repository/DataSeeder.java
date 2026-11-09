package com.clinic.repository;

import com.clinic.config.AppConfig;
import com.clinic.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Pre-populates realistic clinical data into repositories so the desktop application
 * is immediately functional, interactive, and demonstrable upon launch.
 * Uses realistic Pakistani names, local clinics, addresses, and phone numbers.
 */
public class DataSeeder {
    private final IPatientRepository patientRepo;
    private final IDoctorRepository doctorRepo;
    private final IAppointmentRepository appointmentRepo;
    private final IConsultationRepository consultationRepo;
    private final IBillRepository billRepo;

    public DataSeeder(IPatientRepository patientRepo, IDoctorRepository doctorRepo,
                      IAppointmentRepository appointmentRepo, IConsultationRepository consultationRepo,
                      IBillRepository billRepo) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.consultationRepo = consultationRepo;
        this.billRepo = billRepo;
    }

    public void seed() {
        // 1. Seed Departments (FR7)
        Department cardiology = new Department("DEP-01", "Cardiology", "Cardiovascular health, hypertension, ECG, echocardiography");
        Department pediatrics = new Department("DEP-02", "Pediatrics", "Infant, child, and adolescent healthcare & immunizations");
        Department genMed = new Department("DEP-03", "General Medicine", "Primary healthcare, diagnosis, fever, metabolic disorders");
        Department orthopedics = new Department("DEP-04", "Orthopedics", "Musculoskeletal system, joints, spine, and trauma");
        Department dermatology = new Department("DEP-05", "Dermatology", "Skin disorders, allergies, and dermatological care");

        doctorRepo.saveDepartment(cardiology);
        doctorRepo.saveDepartment(pediatrics);
        doctorRepo.saveDepartment(genMed);
        doctorRepo.saveDepartment(orthopedics);
        doctorRepo.saveDepartment(dermatology);

        // Standard consultation time slots (FR8)
        List<TimeSlot> morningSlots = Arrays.asList(
                TimeSlot.of(9, 0, 9, 30),
                TimeSlot.of(9, 30, 10, 0),
                TimeSlot.of(10, 0, 10, 30),
                TimeSlot.of(10, 30, 11, 0),
                TimeSlot.of(11, 0, 11, 30),
                TimeSlot.of(11, 30, 12, 0)
        );

        List<TimeSlot> afternoonSlots = Arrays.asList(
                TimeSlot.of(14, 0, 14, 30),
                TimeSlot.of(14, 30, 15, 0),
                TimeSlot.of(15, 0, 15, 30),
                TimeSlot.of(15, 30, 16, 0),
                TimeSlot.of(16, 0, 16, 30),
                TimeSlot.of(16, 30, 17, 0)
        );

        List<TimeSlot> allDaySlots = new ArrayList<>();
        allDaySlots.addAll(morningSlots);
        allDaySlots.addAll(afternoonSlots);

        Set<DayOfWeek> weekDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        Set<DayOfWeek> altDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

        // 2. Seed Doctors with Pakistani Names and Credentials (FR6, FR7, FR8)
        Doctor doc1 = new Doctor("DOC-101", "Dr. Muhammad Bilal", "Cardiology", "MBBS, FCPS (Cardiology), MRCP (UK)", 2500.0, cardiology);
        doc1.setWorkingDays(weekDays);
        doc1.setConsultationSlots(allDaySlots);

        Doctor doc2 = new Doctor("DOC-102", "Dr. Ayesha Tariq", "Pediatrics", "MBBS, FCPS (Pediatrics), DCH (Glasgow)", 2000.0, pediatrics);
        doc2.setWorkingDays(weekDays);
        doc2.setConsultationSlots(morningSlots);

        Doctor doc3 = new Doctor("DOC-103", "Dr. Usman Farooq", "General Medicine", "MBBS, MCPS (Family Medicine)", 1500.0, genMed);
        doc3.setWorkingDays(weekDays);
        doc3.setConsultationSlots(allDaySlots);

        Doctor doc4 = new Doctor("DOC-104", "Dr. Hamza Siddiqui", "Orthopedics", "MBBS, MS (Orthopedic Surgery), FRCS", 3000.0, orthopedics);
        doc4.setWorkingDays(altDays);
        doc4.setConsultationSlots(afternoonSlots);

        Doctor doc5 = new Doctor("DOC-105", "Dr. Zainab Bukhari", "Dermatology", "MBBS, FCPS (Dermatology)", 2200.0, dermatology);
        doc5.setWorkingDays(altDays);
        doc5.setConsultationSlots(morningSlots);

        Doctor doc6 = new Doctor("DOC-106", "Dr. Tariq Mehmood", "Cardiology", "MBBS, MD, Dipl. Cardiology", 2500.0, cardiology);
        doc6.setWorkingDays(weekDays);
        doc6.setConsultationSlots(afternoonSlots);

        doctorRepo.save(doc1);
        doctorRepo.save(doc2);
        doctorRepo.save(doc3);
        doctorRepo.save(doc4);
        doctorRepo.save(doc5);
        doctorRepo.save(doc6);

        // 3. Seed Patients with Pakistani Names, Addresses & Contact Numbers (FR1)
        Patient pat1 = new Patient("PAT-1001", "Ali Raza Khan", LocalDate.of(1988, 4, 15), "Male",
                "House 12-A, Street 4, Gulberg III, Lahore", "0300-1234567", "Tariq Khan (Father - 0300-7654321)");
        Patient pat2 = new Patient("PAT-1002", "Fatima Zahra", LocalDate.of(1995, 11, 23), "Female",
                "Flat 302, Al-Rehman Heights, F-10/2, Islamabad", "0321-4567890", "Ahmed Raza (Husband - 0321-0987654)");
        Patient pat3 = new Patient("PAT-1003", "Muhammad Zayan", LocalDate.of(2018, 8, 9), "Male",
                "House 88, Sector C, Bahria Town, Rawalpindi", "0333-5678901", "Maryam Zayan (Mother - 0333-1098765)");
        Patient pat4 = new Patient("PAT-1004", "Chaudhry Bashir Ahmed", LocalDate.of(1960, 2, 17), "Male",
                "Kothi 45, Model Town, Block C, Lahore", "0345-6789012", "Hamza Bashir (Son - 0345-2109876)");
        Patient pat5 = new Patient("PAT-1005", "Amina Bibi", LocalDate.of(1974, 6, 30), "Female",
                "House 19, Street 8, PECHS Block 2, Karachi", "0312-3456789", "Rashid Minhas (Brother - 0312-9876543)");
        Patient pat6 = new Patient("PAT-1006", "Bilal Hassan", LocalDate.of(1995, 12, 5), "Male",
                "House 204, Sector G-11/3, Islamabad", "0301-8765432", "Hassan Mahmood (Father - 0301-2345678)");
        Patient pat7 = new Patient("PAT-1007", "Sana Mir", LocalDate.of(1997, 10, 14), "Female",
                "House 14, Cavalry Ground, Lahore Cantt", "0322-9988776", "Asim Mir (Husband - 0322-1122334)");
        Patient pat8 = new Patient("PAT-1008", "Kamran Akmal", LocalDate.of(1985, 3, 25), "Male",
                "Street 10, Wapda Town, Gujranwala", "0334-4455667", "Naveed Akmal (Brother - 0334-7788990)");

        patientRepo.save(pat1);
        patientRepo.save(pat2);
        patientRepo.save(pat3);
        patientRepo.save(pat4);
        patientRepo.save(pat5);
        patientRepo.save(pat6);
        patientRepo.save(pat7);
        patientRepo.save(pat8);

        // 4. Seed Historical Appointments, Consultations & Bills (FR4, FR11, FR12, FR18, FR21, FR24, FR26)
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Historical Consultation for Ali Raza Khan with Dr. Muhammad Bilal
        Appointment pastAppt1 = new Appointment("APT-5001", pat1.getPatientId(), doc1.getDoctorId(), yesterday, morningSlots.get(0), 1);
        pastAppt1.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(pastAppt1);

        Consultation consult1 = new Consultation("CON-7001", pastAppt1.getAppointmentId(), pat1.getPatientId(), doc1.getDoctorId());
        consult1.setSymptoms("Shortness of breath on mild exertion, intermittent chest heaviness, elevated BP (150/95)");
        consult1.setDiagnosis("Stage 1 Essential Hypertension & Mild Angina Pectoris");
        consult1.setNotes("Advised low-sodium diet, avoid oily and spicy food. Daily 30 mins brisk walking. Monitor BP log.");

        Prescription rx1 = new Prescription("RX-9001", consult1.getConsultationId());
        rx1.addItem(new PrescriptionItem("Concor 5mg (Bisoprolol)", "1 tablet", "Once daily after breakfast", "30 days"));
        rx1.addItem(new PrescriptionItem("Loprin 75mg (Aspirin)", "1 tablet", "Once daily after lunch", "30 days"));
        rx1.addItem(new PrescriptionItem("Risek 20mg (Omeprazole)", "1 capsule", "Once daily before breakfast", "14 days"));
        rx1.setGeneralAdvice("Avoid strenuous heavy lifting. Recheck BP in 3 weeks.");
        consult1.setPrescription(rx1);
        consult1.complete(); // Record locked (FR23)
        consultationRepo.save(consult1);
        pat1.addConsultationRecord(consult1.getConsultationId());

        Bill bill1 = new Bill("INV-3001", consult1.getConsultationId(), pat1.getPatientId(), doc1.getDoctorId(), doc1.getConsultationFee(), AppConfig.FIXED_CLINIC_SERVICE_CHARGE);
        bill1.recordPayment(PaymentMethod.CASH, "REC-8001");
        billRepo.save(bill1);

        // Today's Scheduled Appointments (FR11, FR12, FR16)
        Appointment todayAppt1 = new Appointment("APT-5002", pat2.getPatientId(), doc1.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt1.setNotes("Cardiology follow-up & ECG review");
        appointmentRepo.save(todayAppt1);

        Appointment todayAppt2 = new Appointment("APT-5003", pat3.getPatientId(), doc2.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt2.setNotes("Seasonal dry cough and fever for 2 days");
        appointmentRepo.save(todayAppt2);

        Appointment todayAppt3 = new Appointment("APT-5004", pat4.getPatientId(), doc3.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt3.setNotes("Routine diabetic & hypertension checkup");
        appointmentRepo.save(todayAppt3);

        Appointment todayAppt4 = new Appointment("APT-5005", pat5.getPatientId(), doc5.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt4.setNotes("Skin allergy and itching on arms");
        appointmentRepo.save(todayAppt4);
    }
}
