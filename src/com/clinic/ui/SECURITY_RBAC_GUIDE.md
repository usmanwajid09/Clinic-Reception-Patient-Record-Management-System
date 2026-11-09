# Role-Based Access Control (RBAC) & Medical Privacy Architecture Guide

## Overview & Security Policy
The desktop application adheres to the Principle of Least Privilege and strict medical record confidentiality. System interfaces dynamically adapt according to the authenticated user's role:
- **Doctor Role (Clinician)**:
  - Restricted to `🩺 Clinical Workstation` and `📅 My Appointments Queue`.
  - Clinician identity is permanently tied to the authenticated user's profile ID (`DOC-101`, `DOC-102`).
  - Strict queue isolation: A doctor cannot access appointments, diagnostic findings, or patients assigned to other clinicians.
  - Front-desk booking, cashier billing, and system configuration are hidden and inaccessible.
- **Receptionist Role (Front Desk Operations)**:
  - Manages `📋 Patient Records`, `👨‍⚕️ Doctor Directory & Status`, `📅 Appointment Desk`, and `💳 Billing & Receipts`.
  - Strict clinical privacy: Receptionists cannot inspect confidential medical diagnoses or write prescriptions.
  - Doctor registration and consultation fee modifications are disabled (Admin only).
- **Administrator Role (Governance & Audits)**:
  - Manages `👨‍⚕️ Doctor Management`, `📊 Analytics & Revenue`, `📋 Patient Directory (Audit)`, and `📅 All Appointments (Audit)`.
  - Clinical examination and cashier counter billing are locked/hidden (administrators do not diagnose or collect cash).

## Polymorphic Architecture (LSP Compliance)
- Base Class: `User.java`
- Concrete Subclasses: `DoctorUser.java`, `ReceptionistUser.java`, `AdminUser.java`
- Session Management: `IAuthService.java` and `AuthService.java`
- Complete formal authority matrix detailed in `ROLES_AND_AUTHORITY.txt`.

