# Clinic Reception & Patient Record Management System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a robust, modular Java Desktop Application (Swing GUI with Layered Architecture / MVC) implementing all 30 fixed functional requirements (FR1–FR30) for Clinic Reception & Patient Record Management.

**Architecture:** Layered Architecture consisting of Model Layer (Domain Entities), Data Store & Repository Layer (Thread-safe Data Access), Service Layer (Business Validation, Double-Booking Collision Prevention, Billing Computation, Reporting Queries), and Presentation Layer (Modern Java Swing Desktop Frames & Panels using CardLayout navigation).

**Tech Stack:** Java 17/21, Java Swing (`javax.swing.*`, `java.awt.*`), FlatLaf / Modern System L&F, JUnit 5 for unit verification.

---

## Task Breakdown & Verification Steps

### Task 1: Domain Entities & Core Model Layer
**Goal:** Define all business objects representing clinic domain entities with strict typing, encapsulation, and immutability rules.

**Files:**
- Create: `src/com/clinic/model/Role.java`
- Create: `src/com/clinic/model/User.java`
- Create: `src/com/clinic/model/Department.java`
- Create: `src/com/clinic/model/Doctor.java`
- Create: `src/com/clinic/model/TimeSlot.java`
- Create: `src/com/clinic/model/Patient.java`
- Create: `src/com/clinic/model/AppointmentStatus.java`
- Create: `src/com/clinic/model/Appointment.java`
- Create: `src/com/clinic/model/PrescriptionItem.java`
- Create: `src/com/clinic/model/Prescription.java`
- Create: `src/com/clinic/model/Consultation.java`
- Create: `src/com/clinic/model/PaymentStatus.java`
- Create: `src/com/clinic/model/Bill.java`

**Requirements Covered:**
- FR1, FR2, FR6, FR7, FR8, FR12, FR15, FR18, FR20, FR21, FR24, FR25.

**Key Specifications:**
1. `Patient`: `patientId`, `name`, `dateOfBirth`, `gender`, `address`, `contactNumber`, `emergencyContact`, `registrationDate`.
2. `Doctor`: `doctorId`, `name`, `specialization`, `qualification`, `consultationFee`, `department`, `workingDays` (Set of DayOfWeek), `timeSlots` (List of TimeSlot), `unavailabilityDates` (Set of LocalDate).
3. `Appointment`: `appointmentId`, `patientId`, `doctorId`, `appointmentDate`, `timeSlot`, `tokenNumber`, `status` (`Scheduled`, `Completed`, `Cancelled`, `No-Show`).
4. `Consultation`: `consultationId`, `appointmentId`, `patientId`, `doctorId`, `consultationDate`, `symptoms`, `diagnosis`, `notes`, `prescription`, `isCompleted`. Modification locked once `isCompleted == true` (FR23).
5. `Prescription` & `PrescriptionItem`: One prescription containing multiple items (`medicineName`, `dosage`, `frequency`, `duration`).
6. `Bill`: `billId`, `consultationId`, `patientId`, `doctorId`, `consultationFee`, `clinicServiceCharge`, `totalAmount`, `paymentStatus` (`Unpaid`, `Paid`), `paymentDate`, `paymentMethod`.

**Verification:**
- Unit test ensuring `Consultation` cannot be modified after completion (FR23).
- Commit: `feat: add core domain models and validation rules`

---

### Task 2: Thread-Safe Data Store & Repositories with Seeder
**Goal:** Implement data access layer with thread-safe collections and preloaded seed data for doctors, departments, patients, and initial slots to make the desktop app runnable immediately out-of-the-box.

**Files:**
- Create: `src/com/clinic/repository/PatientRepository.java`
- Create: `src/com/clinic/repository/DoctorRepository.java`
- Create: `src/com/clinic/repository/AppointmentRepository.java`
- Create: `src/com/clinic/repository/ConsultationRepository.java`
- Create: `src/com/clinic/repository/BillRepository.java`
- Create: `src/com/clinic/repository/DataStore.java`
- Create: `src/com/clinic/repository/DataSeeder.java`

**Requirements Covered:**
- FR1, FR2, FR4, FR6, FR10, FR16, FR22.

**Key Specifications:**
1. `DataStore`: Central in-memory repository with singleton access and file export/import backup capability.
2. `DataSeeder`: Seeds departments (Cardiology, Pediatrics, General Medicine, Orthopedics, Dermatology), doctors with standard working hours (Mon-Fri 09:00 - 13:00, 14:00 - 17:00), patients with history, and demo appointments.

**Verification:**
- Test `DataSeeder.seed()` populates 5 departments, 6 doctors, 10 patients, and sample appointments.
- Commit: `feat: implement repository layer and initial clinical data seeder`

---

### Task 3: Business Logic Services & Conflict Prevention Engine
**Goal:** Implement business rules, input validation, double-booking collision prevention, consultation finalization, billing calculation, and report aggregation.

**Files:**
- Create: `src/com/clinic/service/ValidationException.java`
- Create: `src/com/clinic/service/PatientService.java`
- Create: `src/com/clinic/service/DoctorService.java`
- Create: `src/com/clinic/service/AppointmentService.java`
- Create: `src/com/clinic/service/ConsultationService.java`
- Create: `src/com/clinic/service/BillingService.java`
- Create: `src/com/clinic/service/ReportService.java`
- Create: `src/com/clinic/service/AuthService.java`

**Requirements Covered:**
- FR3, FR5, FR8, FR9, FR11, FR13, FR14, FR15, FR19, FR22, FR23, FR24, FR27, FR28, FR29, FR30.

**Key Specifications:**
1. `PatientService`: Validates mandatory fields (name, DOB, phone, emergency contact) (FR5); searches by ID, name, or phone (FR2); updates permitted info (FR3).
2. `DoctorService`: Manages availability toggles for specified dates (FR9); filters doctors by department and specialization (FR10).
3. `AppointmentService`:
   - Enforces **Double-Booking Collision Prevention (FR13)**: Query doctor's existing scheduled appointments for the given date and time slot. Throw `ValidationException` if slot is already occupied.
   - Computes sequential daily `tokenNumber` per doctor (FR12).
   - Handles rescheduling with collision check and cancellation (FR14).
4. `ConsultationService`:
   - Validates user role is Doctor (FR17).
   - Records symptoms, diagnosis, prescription (FR18, FR19, FR20, FR21).
   - Locks consultation and marks appointment as `Completed` (FR15, FR22, FR23).
   - Triggers bill creation via `BillingService` (FR24).
5. `BillingService`:
   - Bill calculation: `Doctor consultation fee + fixed clinic service charge` (e.g., $15.00 / 500 PKR) (FR24).
   - Records payment and updates status to `Paid` (FR25).
6. `ReportService`:
   - Aggregates daily and date-range appointment metrics (FR27).
   - Doctor-wise patient counts and consultations (FR28).
   - Patient visit history reports (FR29).
   - Daily and date-range revenue summaries (FR30).

**Verification:**
- Unit tests verifying:
  - Double booking fails with exception.
  - Completed consultation cannot be updated.
  - Bill calculation matches doctor fee + fixed charges.
- Commit: `feat: implement business logic services and appointment conflict engine`

---

### Task 4: Desktop Shell, Theme & Authentication Frame
**Goal:** Create the main application window with clean modern desktop styling, responsive sidebar navigation, user role switcher, and session management.

**Files:**
- Create: `src/com/clinic/ui/theme/UITheme.java`
- Create: `src/com/clinic/ui/LoginFrame.java`
- Create: `src/com/clinic/ui/MainApplicationFrame.java`
- Create: `src/com/clinic/ui/components/SidebarPanel.java`
- Create: `src/com/clinic/ui/components/HeaderPanel.java`
- Create: `src/com/clinic/Main.java`

**Requirements Covered:**
- Desktop Application Framework, Role-based interaction (Receptionist, Doctor, Administrator).

**Key Specifications:**
1. `UITheme`: Clean professional color palette (healthcare teal/blue `#0D9488`, slate `#1E293B`, subtle borders, modern font rendering).
2. `LoginFrame`: Role selection with pre-configured quick logins (`Receptionist`, `Dr. Sarah Jenkins (Cardiology)`, `Admin`).
3. `MainApplicationFrame`: `CardLayout` container switching views based on active sidebar tab, showing current user, active role, and date/time.

**Verification:**
- Run `Main.java`, launch GUI, switch between user roles, verify dynamic navigation updates according to permissions.
- Commit: `feat: implement desktop shell, modern UI theme, and role-based login frame`

---

### Task 5: Patient Registration & Records Management GUI
**Goal:** Build front-desk patient registration form, real-time search table, patient update dialog, and patient medical history viewer.

**Files:**
- Create: `src/com/clinic/ui/panels/PatientManagementPanel.java`
- Create: `src/com/clinic/ui/dialogs/PatientRegistrationDialog.java`
- Create: `src/com/clinic/ui/dialogs/PatientEditDialog.java`
- Create: `src/com/clinic/ui/dialogs/PatientHistoryDialog.java`

**Requirements Covered:**
- FR1, FR2, FR3, FR4, FR5.

**Key Specifications:**
1. Patient registration form with validation error indicators for empty fields or invalid contact formats (FR1, FR5).
2. Instant search bar filtering by Patient ID, Name, or Phone (FR2).
3. "Edit Patient" dialog for authorized staff to modify contact/emergency details (FR3).
4. "View History" modal displaying past visits, dates, attending doctors, diagnoses, and prescriptions (FR4).

**Verification:**
- Register a new patient, verify validation triggers on empty fields, search by name/phone, edit phone number, view history.
- Commit: `feat: implement patient registration, search, and history viewing GUI`

---

### Task 6: Doctor & Schedule Management GUI
**Goal:** Build administrator GUI for doctor registration, department assignment, working days and time slots scheduling, and daily availability toggles.

**Files:**
- Create: `src/com/clinic/ui/panels/DoctorManagementPanel.java`
- Create: `src/com/clinic/ui/dialogs/DoctorRegistrationDialog.java`
- Create: `src/com/clinic/ui/dialogs/DoctorScheduleDialog.java`
- Create: `src/com/clinic/ui/dialogs/DoctorAvailabilityDialog.java`

**Requirements Covered:**
- FR6, FR7, FR8, FR9, FR10.

**Key Specifications:**
1. Doctor registration form: ID, Name, Specialization, Qualification, Consultation Fee, Department dropdown (FR6, FR7).
2. Schedule manager: Checkboxes for Working Days (Monday–Sunday) and custom Consultation Time Slots (FR8).
3. Date-specific Availability Toggle: Mark doctor `Available` or `Unavailable` for a selected date (FR9).
4. Table filtering: Dropdowns to filter doctor list by Department and Specialization (FR10).

**Verification:**
- Register a doctor, assign to Cardiology, set working days and slots, toggle unavailability for a date, filter by department.
- Commit: `feat: implement doctor registration and schedule management GUI`

---

### Task 7: Appointment Booking & Conflict Management GUI
**Goal:** Build receptionist GUI for appointment booking with live conflict detection, token generation, rescheduling, cancellation, and daily schedule views.

**Files:**
- Create: `src/com/clinic/ui/panels/AppointmentManagementPanel.java`
- Create: `src/com/clinic/ui/dialogs/BookAppointmentDialog.java`
- Create: `src/com/clinic/ui/dialogs/RescheduleAppointmentDialog.java`

**Requirements Covered:**
- FR11, FR12, FR13, FR14, FR15, FR16.

**Key Specifications:**
1. Booking Dialog: Select registered patient, select doctor, pick date (respecting doctor working days & availability), pick available time slot (FR11).
2. Conflict Prevention Notification: Prevent double-booking with clear error dialog (FR13).
3. Automatic Token Number calculation and display (FR12).
4. Action buttons: Reschedule (with conflict check) and Cancel (FR14).
5. Status badge rendering: `Scheduled`, `Completed`, `Cancelled`, `No-Show` (FR15).
6. Daily Doctor Queue View: Select doctor + date -> displays sequential patient appointment list with tokens (FR16).

**Verification:**
- Book an appointment, verify token generated; attempt booking same doctor/date/time -> verify blocked; reschedule to free slot -> verify success.
- Commit: `feat: implement appointment booking, double-booking prevention, and token queue GUI`

---

### Task 8: Doctor Consultation Station & Electronic Prescription Builder GUI
**Goal:** Build dedicated Doctor clinical workstation allowing doctors to view scheduled patients, review prior medical history, record clinical notes, and build prescriptions.

**Files:**
- Create: `src/com/clinic/ui/panels/DoctorConsultationPanel.java`
- Create: `src/com/clinic/ui/components/PrescriptionItemTableModel.java`
- Create: `src/com/clinic/ui/dialogs/PrescriptionEditorDialog.java`

**Requirements Covered:**
- FR17, FR18, FR19, FR20, FR21, FR22, FR23.

**Key Specifications:**
1. Doctor's Active Queue: Shows scheduled patients for the logged-in doctor for today (FR16, FR17).
2. "Open Patient File": Displays patient demographics and historical visits (FR17).
3. Clinical Record Entry: Multi-line text areas for Symptoms, Diagnosis, and Consultation Notes (FR18).
4. Electronic Prescription Builder: Dynamic table to add medicine rows with Medicine Name, Dosage, Frequency, and Duration (FR19, FR20, FR21).
5. "Finalize & Complete Consultation" button: Locks the consultation record (immutability), updates appointment to `Completed`, stores consultation in patient history, and triggers bill creation (FR22, FR23, FR24). Modification disabled after completion (FR23).

**Verification:**
- Log in as Doctor, select scheduled patient, enter symptoms and diagnosis, add 2 medicines, click Finalize. Verify record is locked and appointment status changes to Completed.
- Commit: `feat: implement doctor consultation workstation and prescription builder GUI`

---

### Task 9: Billing & Printable Receipt GUI
**Goal:** Build front-desk billing panel displaying generated consultation bills, recording payments, and rendering printable receipts.

**Files:**
- Create: `src/com/clinic/ui/panels/BillingPanel.java`
- Create: `src/com/clinic/ui/dialogs/PaymentDialog.java`
- Create: `src/com/clinic/ui/dialogs/PrintableReceiptDialog.java`
- Create: `src/com/clinic/util/ReceiptPrinter.java`

**Requirements Covered:**
- FR24, FR25, FR26.

**Key Specifications:**
1. Bill Table: Lists bills generated from completed consultations with Doctor Fee, Clinic Service Charges, and Total (FR24).
2. Payment Dialog: Record payment with payment method (Cash, Card) and mark status as `Paid` (FR25).
3. Printable Receipt View: Professional clinic receipt format with Clinic Header, Patient Info, Doctor Info, Itemized breakdown, Total, and Print button using Java Printing API (`java.awt.print.Printable`) (FR26).

**Verification:**
- Complete a consultation, verify bill is generated with correct fees, record payment, open printable receipt dialog, verify layout and print trigger.
- Commit: `feat: implement billing, payment processing, and printable receipt GUI`

---

### Task 10: Analytical Reports & Statistics Engine GUI
**Goal:** Build comprehensive reporting dashboard supporting daily and date-range queries for appointments, doctor performance, patient histories, and clinic revenue.

**Files:**
- Create: `src/com/clinic/ui/panels/ReportsPanel.java`
- Create: `src/com/clinic/ui/components/ReportTableView.java`
- Create: `src/com/clinic/util/ReportExporter.java`

**Requirements Covered:**
- FR27, FR28, FR29, FR30.

**Key Specifications:**
1. Date-range filter toolbar: Start Date, End Date, Quick Presets (Today, Last 7 Days, This Month).
2. Sub-tab 1: Daily & Date-Range Appointment Report (totals, breakdown by Scheduled/Completed/Cancelled/No-Show) (FR27).
3. Sub-tab 2: Doctor-wise Patient & Consultation Report (patients seen, consultations completed, revenue share) (FR28).
4. Sub-tab 3: Patient Visit-History Report (patient search -> complete timeline of visits, doctors, and treatments) (FR29).
5. Sub-tab 4: Daily & Date-Range Revenue Report (total revenue, doctor fees, clinic charges, payment breakdown) (FR30).
6. Print and CSV/Text Export actions for generated reports.

**Verification:**
- Run reports for "Today" and date ranges; verify counts, doctor performance tables, and revenue sums match transactions.
- Commit: `feat: implement reporting dashboard for appointments, doctor performance, and revenue`

---

### Task 11: End-to-End Integration, Packaging & Final GitHub Push
**Goal:** Run end-to-end integration test across all 30 functional requirements, verify role-based permissions, compile clean binary distributions, and push all deliverables to GitHub repository.

**Steps:**
1. Verify compilation with `javac`.
2. Execute comprehensive test suite covering all services and conflict rules.
3. Test all GUI interactions via `com.clinic.Main`.
4. Create release build script (`run.bat` / `run.ps1` / `compile.bat`).
5. Update `README.md` with final screenshots and run instructions.
6. Commit and push all code and documentation to `https://github.com/usmanwajid09/Clinic-Reception-Patient-Record-Management-System.git`.

---

## 🚦 Execution Handoff & Approval

Plan complete and saved to `docs/plans/2026-11-09-clinic-desktop-app-implementation-plan.md`. Two execution options:

1. **Subagent-Driven (this session)** - I dispatch fresh subagents per task, review between tasks, fast iteration.
2. **Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints.
