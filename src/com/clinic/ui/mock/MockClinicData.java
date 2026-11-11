package com.clinic.ui.mock;

import java.time.LocalDate;
import java.util.*;

/**
 * Lightweight static sample data holder solely for rendering GUI tables,
 * dropdowns, and visual screen transitions during desktop UI walkthroughs.
 */
public final class MockClinicData {

    public enum UiRole {
        RECEPTIONIST("Receptionist", "Front Desk, Patient Intake, Appointments & Cashier"),
        DOCTOR("Doctor", "Clinical Workstation, Patient Queue & Electronic Prescriptions"),
        ADMINISTRATOR("Administrator", "Clinician Directory, Work Schedules & Executive Reports");

        private final String displayName;
        private final String description;

        UiRole(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public record UiUser(String id, String fullName, String username, UiRole role, String linkedDoctorId) {
        @Override
        public String toString() {
            return fullName + " (" + role.getDisplayName() + ")";
        }
    }

    public static class PatientRow {
        public String id;
        public String name;
        public String dob;
        public String gender;
        public String phone;
        public String address;
        public String emergencyContact;

        public PatientRow(String id, String name, String dob, String gender, String phone, String address, String emergencyContact) {
            this.id = id;
            this.name = name;
            this.dob = dob;
            this.gender = gender;
            this.phone = phone;
            this.address = address;
            this.emergencyContact = emergencyContact;
        }

        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
    }

    public record HistoryRow(String dateTime, String doctorName, String department, String symptoms, String diagnosis, String rxSummary) {}

    public static class DoctorRow {
        public String id;
        public String name;
        public String department;
        public String specialization;
        public String qualification;
        public double fee;
        public String workingDays;
        public String status;
        public List<String> slots;

        public DoctorRow(String id, String name, String department, String specialization, String qualification,
                         double fee, String workingDays, String status, List<String> slots) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.specialization = specialization;
            this.qualification = qualification;
            this.fee = fee;
            this.workingDays = workingDays;
            this.status = status;
            this.slots = new ArrayList<>(slots);
        }

        @Override
        public String toString() {
            return name + " — " + specialization;
        }
    }

    public static class AppointmentRow {
        public int tokenNumber;
        public String apptId;
        public String date;
        public String timeSlot;
        public String patientId;
        public String patientName;
        public String doctorId;
        public String doctorName;
        public String department;
        public String status;
        public String notes;

        public AppointmentRow(int tokenNumber, String apptId, String date, String timeSlot,
                              String patientId, String patientName, String doctorId,
                              String doctorName, String department, String status, String notes) {
            this.tokenNumber = tokenNumber;
            this.apptId = apptId;
            this.date = date;
            this.timeSlot = timeSlot;
            this.patientId = patientId;
            this.patientName = patientName;
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.department = department;
            this.status = status;
            this.notes = notes;
        }

        @Override
        public String toString() {
            return "Token #" + tokenNumber + " - " + patientName + " (" + timeSlot + ") [" + status + "]";
        }
    }

    public static class RxItemRow {
        public final String medicine;
        public final String dosage;
        public final String frequency;
        public final String duration;
        public final String instructions;

        public RxItemRow(String medicine, String dosage, String frequency, String duration, String instructions) {
            this.medicine = medicine;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
            this.instructions = instructions;
        }
    }

    public static class ConsultationMock {
        public String apptId;
        public String symptoms;
        public String diagnosis;
        public String notes;
        public boolean locked;
        public List<RxItemRow> items;

        public ConsultationMock(String apptId, String symptoms, String diagnosis, String notes, boolean locked, List<RxItemRow> items) {
            this.apptId = apptId;
            this.symptoms = symptoms;
            this.diagnosis = diagnosis;
            this.notes = notes;
            this.locked = locked;
            this.items = new ArrayList<>(items);
        }
    }

    public static class BillRow {
        public String billId;
        public String date;
        public String consultationId;
        public String patientName;
        public String patientId;
        public String patientContact;
        public String doctorName;
        public String doctorId;
        public String department;
        public String specialty;
        public double doctorFee;
        public double clinicCharge;
        public double totalAmount;
        public String status;
        public String method;
        public String receiptNumber;

        public BillRow(String billId, String date, String consultationId, String patientId, String patientName,
                       String patientContact, String doctorId, String doctorName, String department,
                       String specialty, double doctorFee, double clinicCharge, String status, String method, String receiptNumber) {
            this.billId = billId;
            this.date = date;
            this.consultationId = consultationId;
            this.patientId = patientId;
            this.patientName = patientName;
            this.patientContact = patientContact;
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.department = department;
            this.specialty = specialty;
            this.doctorFee = doctorFee;
            this.clinicCharge = clinicCharge;
            this.totalAmount = doctorFee + clinicCharge;
            this.status = status;
            this.method = method;
            this.receiptNumber = receiptNumber;
        }
    }

    public static final List<String> DEPARTMENTS = List.of(
            "Cardiology", "Pediatrics", "General Medicine", "Orthopedics", "Dermatology"
    );

    public static final List<String> STANDARD_SLOTS = List.of(
            "09:00 AM - 09:30 AM", "09:30 AM - 10:00 AM", "10:00 AM - 10:30 AM",
            "10:30 AM - 11:00 AM", "11:00 AM - 11:30 AM", "11:30 AM - 12:00 PM",
            "02:00 PM - 02:30 PM", "02:30 PM - 03:00 PM", "03:00 PM - 03:30 PM",
            "03:30 PM - 04:00 PM", "04:00 PM - 04:30 PM", "04:30 PM - 05:00 PM"
    );

    public static final List<UiUser> DEMO_USERS = List.of(
            new UiUser("USR-REC-1", "Ayesha Siddiqui", "reception.ayesha", UiRole.RECEPTIONIST, null),
            new UiUser("USR-REC-2", "Salman Tariq", "reception.salman", UiRole.RECEPTIONIST, null),
            new UiUser("USR-DOC-1", "Dr. Muhammad Bilal", "dr.bilal", UiRole.DOCTOR, "DOC-101"),
            new UiUser("USR-DOC-2", "Dr. Ayesha Tariq", "dr.ayesha", UiRole.DOCTOR, "DOC-102"),
            new UiUser("USR-ADM-1", "Usman Wajid", "admin.usman", UiRole.ADMINISTRATOR, null)
    );

    private static final List<PatientRow> PATIENTS = new ArrayList<>();
    private static final List<DoctorRow> DOCTORS = new ArrayList<>();
    private static final List<AppointmentRow> APPOINTMENTS = new ArrayList<>();
    private static final Map<String, ConsultationMock> CONSULTATIONS = new LinkedHashMap<>();
    private static final Map<String, List<HistoryRow>> PATIENT_HISTORY = new LinkedHashMap<>();
    private static final List<BillRow> BILLS = new ArrayList<>();

    static {
        resetDemoData();
    }

    private MockClinicData() {}

    public static void resetDemoData() {
        PATIENTS.clear();
        DOCTORS.clear();
        APPOINTMENTS.clear();
        CONSULTATIONS.clear();
        PATIENT_HISTORY.clear();
        BILLS.clear();

        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        PATIENTS.add(new PatientRow("PAT-1001", "Ahmed Raza Khan", "1986-04-12", "Male", "0300-4123456", "House 14, Block K, Gulberg III, Lahore", "0321-4123999 (Brother)"));
        PATIENTS.add(new PatientRow("PAT-1002", "Fatima Zahra", "1993-08-25", "Female", "0333-8451209", "Street 7, DHA Phase 4, Lahore", "0301-8450011 (Husband)"));
        PATIENTS.add(new PatientRow("PAT-1003", "Hamza Ali Qureshi", "2018-02-14", "Male", "0322-7741023", "Apartment 5B, Askari XI, Lahore", "0322-7741000 (Father)"));
        PATIENTS.add(new PatientRow("PAT-1004", "Zainab Malik", "1979-11-03", "Female", "0345-5910234", "32-C, Model Town Extension, Lahore", "0300-5919922 (Son)"));
        PATIENTS.add(new PatientRow("PAT-1005", "Bilal Mehmood", "1998-06-19", "Male", "0312-9034512", "Sector G-10/3, Islamabad", "0313-9030044 (Mother)"));
        PATIENTS.add(new PatientRow("PAT-1006", "Hira Yousaf", "1991-01-29", "Female", "0304-6621890", "Block H, Johar Town, Lahore", "0300-6621111 (Father)"));

        DOCTORS.add(new DoctorRow("DOC-101", "Dr. Muhammad Bilal", "Cardiology", "Cardiology", "MBBS, FCPS (Cardiology), MRCP (UK)", 2500.0, "MON, TUE, WED, THU, FRI", "AVAILABLE", STANDARD_SLOTS.subList(0, 8)));
        DOCTORS.add(new DoctorRow("DOC-102", "Dr. Ayesha Tariq", "Pediatrics", "Pediatrics", "MBBS, FCPS (Pediatrics), DCH (Glasgow)", 2000.0, "MON, TUE, WED, THU, FRI", "AVAILABLE", STANDARD_SLOTS.subList(0, 6)));
        DOCTORS.add(new DoctorRow("DOC-103", "Dr. Usman Farooq", "General Medicine", "General Medicine", "MBBS, MCPS (Family Medicine)", 1500.0, "MON, TUE, WED, THU, FRI, SAT", "AVAILABLE", STANDARD_SLOTS));
        DOCTORS.add(new DoctorRow("DOC-104", "Dr. Hamza Siddiqui", "Orthopedics", "Orthopedic Surgery", "MBBS, MS (Orthopedic Surgery), FRCS", 3000.0, "MON, WED, FRI, SAT", "AVAILABLE", STANDARD_SLOTS.subList(6, 12)));
        DOCTORS.add(new DoctorRow("DOC-105", "Dr. Zainab Bukhari", "Dermatology", "Dermatology", "MBBS, FCPS (Dermatology)", 2200.0, "TUE, THU, SAT", "UNAVAILABLE", STANDARD_SLOTS.subList(0, 6)));

        APPOINTMENTS.add(new AppointmentRow(1, "APT-201", today, "09:00 AM - 09:30 AM", "PAT-1001", "Ahmed Raza Khan", "DOC-101", "Dr. Muhammad Bilal", "Cardiology", "COMPLETED", "Routine ECG & BP review"));
        APPOINTMENTS.add(new AppointmentRow(2, "APT-202", today, "09:30 AM - 10:00 AM", "PAT-1004", "Zainab Malik", "DOC-101", "Dr. Muhammad Bilal", "Cardiology", "SCHEDULED", "Chest tightness follow-up"));
        APPOINTMENTS.add(new AppointmentRow(3, "APT-203", today, "10:00 AM - 10:30 AM", "PAT-1005", "Bilal Mehmood", "DOC-101", "Dr. Muhammad Bilal", "Cardiology", "SCHEDULED", "Hypertension check"));
        APPOINTMENTS.add(new AppointmentRow(4, "APT-204", today, "10:30 AM - 11:00 AM", "PAT-1003", "Hamza Ali Qureshi", "DOC-102", "Dr. Ayesha Tariq", "Pediatrics", "SCHEDULED", "Seasonal flu & vaccination"));
        APPOINTMENTS.add(new AppointmentRow(5, "APT-205", today, "11:00 AM - 11:30 AM", "PAT-1002", "Fatima Zahra", "DOC-103", "Dr. Usman Farooq", "General Medicine", "COMPLETED", "Migraine evaluation"));
        APPOINTMENTS.add(new AppointmentRow(6, "APT-206", yesterday, "02:00 PM - 02:30 PM", "PAT-1006", "Hira Yousaf", "DOC-104", "Dr. Hamza Siddiqui", "Orthopedics", "NO_SHOW", "Knee joint pain"));

        CONSULTATIONS.put("APT-201", new ConsultationMock(
                "APT-201",
                "Mild palpitations and elevated morning BP (145/92 mmHg).",
                "Essential Hypertension Stage 1",
                "Advise low-sodium diet, 30 min daily walk, and repeat ECG after 4 weeks.",
                true,
                List.of(
                        new RxItemRow("Concor (Bisoprolol) 5mg", "1 Tablet", "Once daily (OD) - Morning", "30 days", "Monitor pulse before dose"),
                        new RxItemRow("Ascard (Aspirin) 75mg", "1 Tablet", "Once daily (OD) - After lunch", "30 days", "Take with water")
                )
        ));

        CONSULTATIONS.put("APT-202", new ConsultationMock(
                "APT-202",
                "Occasional exertional tightness and fatigue over 3 days.",
                "",
                "",
                false,
                List.of()
        ));

        CONSULTATIONS.put("APT-203", new ConsultationMock(
                "APT-203",
                "",
                "",
                "",
                false,
                List.of()
        ));

        PATIENT_HISTORY.put("PAT-1001", List.of(
                new HistoryRow(today + " 09:20", "Dr. Muhammad Bilal", "Cardiology", "Mild palpitations, BP 145/92", "Essential Hypertension Stage 1", "2 medications (Concor 5mg, Ascard 75mg)"),
                new HistoryRow("2026-09-18 11:10", "Dr. Usman Farooq", "General Medicine", "Seasonal viral fever & body aches", "Acute Viral Upper Respiratory Infection", "2 medications (Panadol Extra, Vitamin C)")
        ));
        PATIENT_HISTORY.put("PAT-1002", List.of(
                new HistoryRow(today + " 11:15", "Dr. Usman Farooq", "General Medicine", "Throbbing unilateral headache & photophobia", "Acute Migraine Episode", "2 medications (Sumatriptan 50mg, Motilium)")
        ));

        BILLS.add(new BillRow("INV-5001", today, "CON-301", "PAT-1001", "Ahmed Raza Khan", "0300-4123456", "DOC-101", "Dr. Muhammad Bilal", "Cardiology", "Cardiology", 2500.0, 500.0, "PAID", "Card", "RCP-90012"));
        BILLS.add(new BillRow("INV-5002", today, "CON-302", "PAT-1002", "Fatima Zahra", "0333-8451209", "DOC-103", "Dr. Usman Farooq", "General Medicine", "General Medicine", 1500.0, 500.0, "UNPAID", "N/A", null));
        BILLS.add(new BillRow("INV-5003", yesterday, "CON-298", "PAT-1004", "Zainab Malik", "0345-5910234", "DOC-101", "Dr. Muhammad Bilal", "Cardiology", "Cardiology", 2500.0, 500.0, "PAID", "Cash", "RCP-90008"));
    }

    public static List<UiUser> getUsersForRole(UiRole role) {
        return DEMO_USERS.stream().filter(u -> u.role() == role).toList();
    }

    public static List<PatientRow> getPatients() { return PATIENTS; }
    public static List<DoctorRow> getDoctors() { return DOCTORS; }
    public static List<AppointmentRow> getAppointments() { return APPOINTMENTS; }
    public static List<BillRow> getBills() { return BILLS; }

    public static Optional<DoctorRow> findDoctorById(String id) {
        return DOCTORS.stream().filter(d -> d.id.equalsIgnoreCase(id)).findFirst();
    }

    public static Optional<PatientRow> findPatientById(String id) {
        return PATIENTS.stream().filter(p -> p.id.equalsIgnoreCase(id)).findFirst();
    }

    public static List<HistoryRow> getHistoryForPatient(String patientId) {
        return PATIENT_HISTORY.getOrDefault(patientId, List.of());
    }

    public static ConsultationMock getOrCreateMockConsultation(String apptId) {
        return CONSULTATIONS.computeIfAbsent(apptId, id -> new ConsultationMock(id, "", "", "", false, List.of()));
    }

    public static String nextPatientId() { return "PAT-" + (1001 + PATIENTS.size()); }
    public static String nextDoctorId() { return "DOC-" + (101 + DOCTORS.size()); }
    public static String nextAppointmentId() { return "APT-" + (201 + APPOINTMENTS.size()); }
    public static int nextTokenNumber() { return APPOINTMENTS.size() + 1; }
    public static String nextReceiptNumber() { return "RCP-" + (90010 + BILLS.size()); }
}
