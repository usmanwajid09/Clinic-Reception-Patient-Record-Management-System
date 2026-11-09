# Clinical Analytics & Executive Reporting Subsystem Guide

## Overview & Functional Scope
This panel provides multi-dimensional clinical, operational, and financial analytics for clinic administrators:
- **FR27 (Appointment Reports)**: Aggregates daily and date-range appointment statistics, completion counts, cancellation reasons, and no-show rates.
- **FR28 (Doctor Productivity Reports)**: Doctor-wise breakdown of completed consultations, unique patient volume, and consultation fee aggregates.
- **FR29 (Patient Visit History Audit)**: Chronological visit-history audit for any selected patient, listing past diagnoses and prescribed medications.
- **FR30 (Revenue Reports)**: Daily and date-range financial reporting detailing total invoiced amounts, collected revenues, and pending balances in Pakistani Rupees (`Rs. `).

## Component & Architecture Map
- **UI View**: `ReportsPanel.java` (Tabbed reporting container)
- **Service**: `ReportService.java` (Implements `IReportService`)
- **Data Transfer Records**:
  - `AppointmentReportData`
  - `DoctorReportData`
  - `PatientHistoryReportData`
  - `RevenueReportData`

## Security & Access Control Rules
- **Administrator**: Exclusive access. Full access to executive reporting and clinic revenue analytics.
- **Receptionist**: Inaccessible. Receptionists do not view macro-financial clinic reports.
- **Doctor**: Inaccessible. Clinicians do not inspect clinic-wide revenue figures.
