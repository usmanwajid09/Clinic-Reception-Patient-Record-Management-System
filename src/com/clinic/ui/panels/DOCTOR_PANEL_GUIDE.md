# Doctor Management & Scheduling Subsystem Guide

## Overview & Functional Scope
This panel fulfills all clinician administration, department mapping, and duty availability requirements:
- **FR6 (Clinician Registration)**: Administrator registers doctors with Doctor ID, Full Name, Specialization, Qualifications, and Consultation Fee.
- **FR7 (Department Association)**: Associates each doctor with exactly one clinical department (Cardiology, Pediatrics, Orthopedics, Dermatology, General Medicine).
- **FR8 (Working Days & Slots)**: Administrator defines weekly working days (Monday - Sunday) and consultation time slot intervals for each doctor.
- **FR9 (Daily Duty Status)**: Authorized staff marks a doctor as Available or Unavailable for any specific calendar date with reason tracking.
- **FR10 (Directory Filtering)**: Displays the doctor directory with live filtering by clinical Department and medical Specialization.

## Component & Architecture Map
- **UI View**: `DoctorManagementPanel.java`
- **Dialogs**: `DoctorDialog.java` (Onboarding form), `DoctorScheduleDialog.java` (Working days and slots), `DoctorAvailabilityDialog.java` (Daily leave/duty toggle)
- **Validator**: `DoctorValidator.java` (Implements `IValidator<Doctor>`)
- **Service**: `DoctorService.java` (Implements `IDoctorService`)
- **Repository**: `InMemoryDoctorRepository.java`
- **Domain Entities**: `Doctor.java`, `Department.java`, `TimeSlot.java`

## Security & Privacy Rules
- **Administrator**: Full administrative control (Register new clinicians, configure consultation fees, and define working days/slots).
- **Receptionist**: Restricted directory viewing and daily duty availability toggling (FR9). Button controls for clinician registration and scheduling are hidden.
- **Doctor**: Read-only colleague directory view.
