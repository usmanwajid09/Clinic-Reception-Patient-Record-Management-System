# Patient Management & Intake Subsystem Guide

## Overview & Functional Scope
This panel fulfills all patient-related requirements specified in Project #2:
- **FR1 (Registration)**: Record Patient ID, Full Name, DOB, Gender, Address, Contact Number, and Emergency Contact.
- **FR2 (Search Engine)**: Search registered patients dynamically by Patient ID, Name, or Phone Number.
- **FR3 (Demographics Update)**: Update permitted contact, address, and emergency contact details.
- **FR4 (Visit History)**: Maintain and review a patient's historical consultation logs, visiting clinicians, and prescribed medications.
- **FR5 (Validation)**: Mandatory pre-save validation ensuring required fields are non-empty, contact numbers follow Pakistani telecommunications formats (`0300-...`), and dates of birth are strictly in the past.

## Component & Architecture Map
- **UI View**: `PatientManagementPanel.java`
- **Dialogs**: `PatientDialog.java` (Intake & update form), `PatientHistoryDialog.java` (Chronological record inspection)
- **Validator**: `PatientValidator.java` (Implements `IValidator<Patient>`)
- **Service**: `PatientService.java` (Implements `IPatientService`)
- **Repository**: `InMemoryPatientRepository.java` (Thread-safe `ConcurrentHashMap`)
- **Domain Entity**: `Patient.java`

## Security & Privacy Rules
- **Receptionist**: Full operational authority (Register new walk-in patients, update demographics, search directory).
- **Administrator**: Read-only audit mode (Can search directory and view registration dates for compliance).
- **Doctor**: Inaccessible directly from front desk; clinicians only access assigned patient records through the clinical workstation.
