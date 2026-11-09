# Doctor Clinical Workstation & Electronic Prescription Subsystem Guide

## Overview & Functional Scope
This panel provides a specialized clinical workspace designed exclusively for licensed medical practitioners:
- **FR17 (Record Access)**: Clinicians open confidential medical records strictly for patients scheduled with their session.
- **FR18 (Clinical Examination)**: Clinician records clinical symptoms, diagnostic findings (mandatory), and consultation notes.
- **FR19 (Electronic Prescription)**: Generates a single, official electronic prescription tied to the consultation.
- **FR20 (Multi-Medication Support)**: Prescription supports adding multiple pharmaceutical items.
- **FR21 (Structured Dosage Details)**: Captures medicine name, dosage, frequency, duration, and patient instructions.
- **FR22 (Medical History Archival)**: Completed consultation is permanently cataloged in the patient's lifetime history.
- **FR23 (Immutability Lock)**: Locks consultation records immediately upon completion. Modifications by unauthorized users or post-finalization edits are strictly blocked.

## Component & Architecture Map
- **UI View**: `DoctorConsultationPanel.java`
- **Dialogs**: `PrescriptionItemDialog.java` (Medication builder), `PatientHistoryDialog.java` (Historical records review)
- **Service**: `ConsultationService.java` (Implements `IConsultationService`)
- **Repository**: `InMemoryConsultationRepository.java`
- **Domain Entities**: `Consultation.java`, `Prescription.java`, `PrescriptionItem.java`

## Security & Medical Privacy Rules
- **Doctor**: Strictly isolated to the authenticated doctor's profile (e.g. Dr. Muhammad Bilal, DOC-101). Cannot access patients assigned to other clinicians.
- **Receptionist**: Inaccessible. Receptionists cannot view confidential diagnostic findings or prescribe medications.
- **Administrator**: Inaccessible for clinical actions (Non-physicians cannot diagnose or prescribe).

