package com.clinic.repository;

import com.clinic.config.AppConfig;
import com.clinic.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Pre-populates realistic clinical data into repositories so the desktop application
 * is immediately functional, interactive, and demonstrable upon launch.
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

        // 2. Seed Doctors (FR6, FR7, FR8)
        Doctor doc1 = new Doctor("DOC-101", "Dr. Sarah Jenkins", "Cardiology", "MBBS, MD (Cardiology), FACC", 85.0, cardiology);
        doc1.setWorkingDays(weekDays);
        doc1.setConsultationSlots(allDaySlots);

        Doctor doc2 = new Doctor("DOC-102", "Dr. Michael Chen", "Pediatrics", "MBBS, DCH, FAAP", 60.0, pediatrics);
        doc2.setWorkingDays(weekDays);
        doc2.setConsultationSlots(morningSlots);

        Doctor doc3 = new Doctor("DOC-103", "Dr. Robert Sterling", "General Medicine", "MBBS, MRCGP, MD", 50.0, genMed);
        doc3.setWorkingDays(weekDays);
        doc3.setConsultationSlots(allDaySlots);

        Doctor doc4 = new Doctor("DOC-104", "Dr. Emily Hayes", "Orthopedics", "MBBS, MS (Ortho), FRCS", 90.0, orthopedics);
        doc4.setWorkingDays(altDays);
        doc4.setConsultationSlots(afternoonSlots);

        Doctor doc5 = new Doctor("DOC-105", "Dr. Zainab Al-Mansoor", "Dermatology", "MBBS, DDV, American Board Cert.", 75.0, dermatology);
        doc5.setWorkingDays(altDays);
        doc5.setConsultationSlots(morningSlots);

        Doctor doc6 = new Doctor("DOC-106", "Dr. James Wilson", "Cardiology", "MBBS, FESC", 80.0, cardiology);
        doc6.setWorkingDays(weekDays);
        doc6.setConsultationSlots(afternoonSlots);

        doctorRepo.save(doc1);
        doctorRepo.save(doc2);
        doctorRepo.save(doc3);
        doctorRepo.save(doc4);
        doctorRepo.save(doc5);
        doctorRepo.save(doc6);

        // 3. Seed Patients (FR1)
        Patient pat1 = new Patient("PAT-1001", "Alice Harrison", LocalDate.of(1989, 4, 15), "Female",
                "742 Evergreen Terrace, Springfield", "555-0101", "Bob Harrison (555-0102)");
        Patient pat2 = new Patient("PAT-1002", "David Miller", LocalDate.of(1975, 11, 23), "Male",
                "124 Conch Street, Riverdale", "555-0201", "Sarah Miller (555-0202)");
        Patient pat3 = new Patient("PAT-1003", "Sophia Rodriguez", LocalDate.of(2001, 8, 9), "Female",
                "456 Elm Street, Metropol", "555-0301", "Elena Rodriguez (555-0302)");
        Patient pat4 = new Patient("PAT-1004", "William Parker", LocalDate.of(1963, 2, 17), "Male",
                "89 Ocean Drive, Bay City", "555-0401", "Grace Parker (555-0402)");
        Patient pat5 = new Patient("PAT-1005", "Chloe Kim", LocalDate.of(2018, 6, 30), "Female",
                "32 Pine Crest Lane, Greenfield", "555-0501", "Hannah Kim (555-0502)");
        Patient pat6 = new Patient("PAT-1006", "Marcus Vance", LocalDate.of(1995, 12, 5), "Male",
                "501 Beacon Hill, Grandview", "555-0601", "Tyler Vance (555-0602)");

        patientRepo.save(pat1);
        patientRepo.save(pat2);
        patientRepo.save(pat3);
        patientRepo.save(pat4);
        patientRepo.save(pat5);
        patientRepo.save(pat6);

        // 4. Seed Historical Appointments, Consultations & Bills (FR4, FR11, FR12, FR18, FR21, FR24, FR26)
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Historical Consultation for pat1 with doc1
        Appointment pastAppt1 = new Appointment("APT-5001", pat1.getPatientId(), doc1.getDoctorId(), yesterday, morningSlots.get(0), 1);
        pastAppt1.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(pastAppt1);

        Consultation consult1 = new Consultation("CON-7001", pastAppt1.getAppointmentId(), pat1.getPatientId(), doc1.getDoctorId());
        consult1.setSymptoms("Shortness of breath on mild exertion, intermittent palpitations");
        consult1.setDiagnosis("Stage 1 Essential Hypertension, mild sinus tachycardia");
        consult1.setNotes("Advised low-sodium diet, regular aerobic exercise, and BP monitoring log.");

        Prescription rx1 = new Prescription("RX-9001", consult1.getConsultationId());
        rx1.addItem(new PrescriptionItem("Amlodipine 5mg", "1 tablet", "Once daily in morning", "30 days"));
        rx1.addItem(new PrescriptionItem("Nebivolol 2.5mg", "1 tablet", "Once daily after breakfast", "30 days"));
        rx1.setGeneralAdvice("Monitor blood pressure morning and night. Recheck in 4 weeks.");
        consult1.setPrescription(rx1);
        consult1.complete(); // Record locked (FR23)
        consultationRepo.save(consult1);
        pat1.addConsultationRecord(consult1.getConsultationId());

        Bill bill1 = new Bill("INV-3001", consult1.getConsultationId(), pat1.getPatientId(), doc1.getDoctorId(), doc1.getConsultationFee(), AppConfig.FIXED_CLINIC_SERVICE_CHARGE);
        bill1.recordPayment(PaymentMethod.CREDIT_CARD, "REC-8001");
        billRepo.save(bill1);

        // Today's Scheduled Appointments (FR11, FR12, FR16)
        Appointment todayAppt1 = new Appointment("APT-5002", pat2.getPatientId(), doc1.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt1.setNotes("Routine cardiology follow-up");
        appointmentRepo.save(todayAppt1);

        Appointment todayAppt2 = new Appointment("APT-5003", pat3.getPatientId(), doc1.getDoctorId(), today, morningSlots.get(1), 2);
        todayAppt2.setNotes("Chest discomfort after exercise");
        appointmentRepo.save(todayAppt2);

        Appointment todayAppt3 = new Appointment("APT-5004", pat5.getPatientId(), doc2.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt3.setNotes("Mild seasonal cough and low fever");
        appointmentRepo.save(todayAppt3);

        Appointment todayAppt4 = new Appointment("APT-5005", pat4.getPatientId(), doc3.getDoctorId(), today, morningSlots.get(0), 1);
        todayAppt4.setNotes("Routine health checkup");
        appointmentRepo.save(todayAppt4);
    }
}

// Initial clinical seed dataset for live demonstration
