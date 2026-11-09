# Clinic Reception & Patient Record Management System

A desktop application engineered in Java for managing clinic operations, patient records, doctor schedules, appointments, clinical consultations, prescriptions, billing, and reporting.

Developed for the **Software Design & Analysis (SDA)** Course Project (Project #2: Clinic Reception & Patient Record Management System).

---

## 📌 Project Overview

This desktop system manages the end-to-end operational workflow of a healthcare clinic:
1. **Front-Desk Patient Registration & History**: Recording and validating patient details, maintaining lifetime medical histories and visit logs.
2. **Doctor & Schedule Management**: Managing doctor profiles, qualifications, specializations, consultation fees, weekly availability, and day-specific leave/unavailability.
3. **Appointment Scheduling Engine**: Token generation, strict conflict detection (preventing doctor double-booking), appointment status tracking (`Scheduled`, `Completed`, `Cancelled`, `No-Show`), and rescheduling.
4. **Doctor Consultation Station**: Reviewing scheduled patients, logging symptoms, diagnosis, and clinical notes, creating structured electronic prescriptions (with medicine name, dosage, frequency, and duration), and ensuring consultation records become immutable once finalized.
5. **Billing & Invoicing**: Automatic calculation of consultation fees plus clinic service charges, recording payments, and generating printable receipts.
6. **Executive Reporting & Analytics**: Daily and date-range appointment reports, doctor productivity & consultation analytics, patient visit histories, and revenue reports.

---

## 🏛️ Architectural Design

The application follows the **Layered Architecture** and **Model-View-Controller (MVC)** patterns to maintain separation of concerns, high cohesion, low coupling, and testability:

```
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER (GUI)                     │
│  Java Swing Desktop UI (FlatLaf / Modern CardLayout Frame)  │
│  - Login & Role Selector Frame                              │
│  - Receptionist Dashboard (Patient Registration & Appts)    │
│  - Doctor Consultation Station (Medical Records & Rx)       │
│  - Admin Console (Doctor Schedules, Billing, Reports)       │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    SERVICE / LOGIC LAYER                    │
│  - PatientService         - DoctorService                   │
│  - AppointmentService     - ConsultationService             │
│  - BillingService         - ReportService                   │
│  - ValidationEngine       - AuthContext                     │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│              DATA ACCESS & REPOSITORY LAYER                 │
│  - PatientRepository      - DoctorRepository                │
│  - AppointmentRepository  - ConsultationRepository          │
│  - BillRepository         - InMemoryDataStore / File Store  │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                      DOMAIN MODEL LAYER                     │
│  Patient, Doctor, Department, TimeSlot, Appointment,        │
│  Consultation, Prescription, PrescriptionItem, Bill, User   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Fixed Functional Requirements Traceability (FR1 – FR30)

| Requirement ID | Module | Specification Description |
| :--- | :--- | :--- |
| **FR1** | Patient Registration | Register a patient using patient ID, name, date of birth, gender, address, contact number, and emergency contact. |
| **FR2** | Patient Registration | Search an existing patient by patient ID, name, or contact number. |
| **FR3** | Patient Registration | Allow authorized staff to update permitted patient information. |
| **FR4** | Patient Registration | Maintain a patient's previous visits and consultation records. |
| **FR5** | Patient Registration | Validate mandatory patient information before saving a new record. |
| **FR6** | Doctor Management | Register doctors with doctor ID, name, specialization, qualification, and consultation fee. |
| **FR7** | Doctor Management | Associate a doctor with one department. |
| **FR8** | Doctor Management | Define each doctor's working days and consultation time slots. |
| **FR9** | Doctor Management | Allow authorized staff to mark a doctor as Available or Unavailable for a specified day. |
| **FR10** | Doctor Management | Display the list of doctors by department and specialization. |
| **FR11** | Appointment Management | Create an appointment for a registered patient with an available doctor. |
| **FR12** | Appointment Management | Record appointment date, time, token number, and appointment status. |
| **FR13** | Appointment Management | Prevent two appointments from being assigned to the same doctor at the same date and time (Strict Conflict Detection). |
| **FR14** | Appointment Management | Allow the receptionist to reschedule or cancel an appointment. |
| **FR15** | Appointment Management | Maintain appointment status as Scheduled, Completed, Cancelled, or No-Show. |
| **FR16** | Appointment Management | Display a doctor's appointment list for a selected date. |
| **FR17** | Consultation & Rx | Allow the doctor to open the record of a patient scheduled with that doctor. |
| **FR18** | Consultation & Rx | Allow the doctor to record symptoms, diagnosis, and consultation notes. |
| **FR19** | Consultation & Rx | Allow a doctor to create one prescription for a consultation. |
| **FR20** | Consultation & Rx | Allow a prescription to contain one or more medicines. |
| **FR21** | Consultation & Rx | Record medicine name, dosage, frequency, and duration for each prescribed medicine. |
| **FR22** | Consultation & Rx | Store each completed consultation in the patient's medical history. |
| **FR23** | Consultation & Rx | Prevent unauthorized users from modifying a completed consultation (Record Immutability). |
| **FR24** | Billing & Reports | Create a bill for a completed consultation using the doctor's consultation fee and fixed clinic service charges. |
| **FR25** | Billing & Reports | Allow the receptionist to record payment and payment status (`Unpaid`, `Paid`). |
| **FR26** | Billing & Reports | Generate a printable receipt for a paid bill. |
| **FR27** | Billing & Reports | Provide daily and date-range appointment reports. |
| **FR28** | Billing & Reports | Provide doctor-wise patient and consultation reports. |
| **FR29** | Billing & Reports | Provide patient visit-history reports. |
| **FR30** | Billing & Reports | Provide daily and date-range revenue reports. |

---

## 👥 User Roles & Access Control

| Role | Permitted Actions & Navigation Views |
| :--- | :--- |
| **Receptionist** | Patient Registration & Search, Appointment Scheduling / Rescheduling / Cancellation, Daily Queue & Token issuance, Payment Recording & Receipt Printing. |
| **Doctor** | Doctor Appointment Queue, View Scheduled Patient Records & Previous Medical History, Record Symptoms & Diagnosis, Build Prescription, Finalize & Lock Consultations. |
| **Administrator** | Doctor Profile & Department Registration, Working Days & Slot Definition, Availability Toggles, Clinic Charges Configuration, System-wide Audit & Reports (Appointments, Doctors, Revenue). |

---

## 💻 Tech Stack & Tools

- **Language**: Java 17 / 21
- **UI Framework**: Java Swing (`javax.swing.*`, `java.awt.*`) with clean, modern layout architecture
- **Design Pattern**: MVC (Model-View-Controller) / Layered Architecture
- **Data Persistence**: Thread-safe in-memory data store with predefined demo data seeder & JSON/CSV file persistence support
- **Testing**: JUnit 5 unit and integration tests

---

## 📂 Proposed Project Structure

```
SDA Project/
├── .gitignore
├── Functional_Requirements_proposals.pdf
├── README.md
├── IMPLEMENTATION_PLAN.md
├── docs/
│   └── plans/
│       └── 2026-11-09-clinic-desktop-app-implementation-plan.md
└── src/
    └── com/
        └── clinic/
            ├── Main.java
            ├── model/
            │   ├── Patient.java
            │   ├── Doctor.java
            │   ├── Department.java
            │   ├── TimeSlot.java
            │   ├── Appointment.java
            │   ├── AppointmentStatus.java
            │   ├── Consultation.java
            │   ├── Prescription.java
            │   ├── PrescriptionItem.java
            │   ├── Bill.java
            │   ├── PaymentStatus.java
            │   └── User.java / Role.java
            ├── repository/
            │   ├── PatientRepository.java
            │   ├── DoctorRepository.java
            │   ├── AppointmentRepository.java
            │   ├── ConsultationRepository.java
            │   ├── BillRepository.java
            │   └── DataStore.java
            ├── service/
            │   ├── PatientService.java
            │   ├── DoctorService.java
            │   ├── AppointmentService.java
            │   ├── ConsultationService.java
            │   ├── BillingService.java
            │   ├── ReportService.java
            │   └── AuthService.java
            ├── ui/
            │   ├── LoginFrame.java
            │   ├── MainApplicationFrame.java
            │   ├── components/
            │   │   ├── NavigationBar.java
            │   │   ├── CardHeader.java
            │   │   └── ReceiptDialog.java
            │   └── panels/
            │       ├── PatientManagementPanel.java
            │       ├── DoctorManagementPanel.java
            │       ├── AppointmentManagementPanel.java
            │       ├── DoctorConsultationPanel.java
            │       ├── BillingPanel.java
            │       └── ReportsPanel.java
            └── util/
                ├── ValidationUtil.java
                ├── DateTimeUtil.java
                └── ReceiptPrinter.java
```

---

## 🚀 How to Compile and Run

### Prerequisites
- JDK 17 or JDK 21 installed.

### Compilation
From the `SDA Project` root directory:
```powershell
# Create bin directory
mkdir bin -Force

# Compile all Java source files
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
```

### Running the Desktop Application
```powershell
java -cp bin com.clinic.Main
```

---

## 🧩 SOLID & SDA Design Principles Applied

This project explicitly adheres to the **SOLID Design Principles** and **Software Design & Analysis (SDA)** architectural standards:

1. **Single Responsibility Principle (SRP)**:
   - Every class has one reason to change. Business logic resides in dedicated services (`PatientService`, `AppointmentService`, etc.), persistence logic is isolated in repositories (`PatientRepository`), receipt formatting is isolated in `ReceiptPrinter`, and UI presentation resides strictly in GUI panels.
2. **Open/Closed Principle (OCP)**:
   - System modules are open for extension but closed for modification through interface-driven abstractions (`IPatientRepository`, `IDoctorRepository`, `IBillingStrategy`, `IReportExporter`).
3. **Liskov Substitution Principle (LSP)**:
   - Subtypes (`ReceptionistUser`, `DoctorUser`, `AdminUser`) are cleanly substitutable for the base `User` class without unexpected exceptions or behavior violations.
4. **Interface Segregation Principle (ISP)**:
   - Interfaces are highly focused and segregated (`Searchable`, `PrintableReport`, `Repository<T, ID>`) so classes are never forced to implement methods they do not require.
5. **Dependency Inversion Principle (DIP)**:
   - High-level business services depend on abstractions (repository interfaces) rather than low-level concrete implementations. Dependencies are injected via constructors.


