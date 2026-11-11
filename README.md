# Clinic Reception & Patient Record Management System

A modern Java Swing desktop application built for the **Software Design & Analysis (SDA)** Course Project (**Project #2**), tailored for outpatient clinic reception, clinician consultations, appointment scheduling, billing counters, and executive reporting at **Shifa Clinic & Patient Healthcare Center (Lahore, Pakistan)**.

---

## Project Scope & Submission Phases

| Phase | Scope | Status | Completion Date |
| :--- | :--- | :--- | :--- |
| **Phase 1 — Desktop GUI** | Complete interactive Java Swing Presentation Layer (`RoleSelectFrame`, `LoginFrame`, `MainApplicationFrame`, Role Dashboards, 6 Module Panels, 10 Modal Dialogs, 60 FPS Motion UI, Custom PNG Icons, Static UI Mock Data). **Strictly GUI-only: no backend service logic, database storage, or external APIs.** | **Completed** | **November 11, 2026** |
| **Phase 2+ — Service & Persistence** *(Upcoming)* | Business rule engines, scheduling conflict detection, automated billing calculations, persistent storage, and automated test suites. | *Planned (Future Phase)* | *TBD* |

---

## Complete System Vision (End-to-End Capabilities)

The overall **Clinic Reception & Patient Record Management System** spans six integrated clinical and administrative workflows across three specialized staff roles (**Receptionist**, **Doctor**, **Administrator**):

1. **Patient Intake & Visit History**: Onboarding walk-in patients, live directory search by Patient ID, name, or phone number, demographic updates, and viewing lifetime medical encounter histories.
2. **Clinician Directory & Shift Configuration**: Managing clinician profiles, departments (*Cardiology*, *Pediatrics*, *General Medicine*, *Orthopedics*, *Dermatology*), specializations, consultation fees (`Rs.`), weekly working days, consultation slots, and daily availability toggles.
3. **Appointment Desk & Daily Queue**: Issuing sequential consultation tokens, filtering queues by doctor/date/status (`SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`), booking new visits, rescheduling, and handling cancellations.
4. **Doctor Clinical Workstation & Electronic Prescriptions**: Split-pane clinician station for reviewing today’s assigned patient queue, inspecting patient visit history, recording symptoms, diagnosis, clinical notes, composing multi-medicine electronic prescriptions, and locking finalized encounters.
5. **Front-Desk Cashier Billing & Receipts**: Tracking consultation invoices (`PAID` / `UNPAID`), viewing fee breakdowns (Doctor Fee + Clinic Service Charge), recording payment methods (`Cash`, `Card`, `Online Transfer`), and previewing formatted official receipts.
6. **Analytics & Executive Reporting**: Tabbed multi-view reporting for appointments, doctor consultation productivity, patient visit logs, and clinic billing/revenue summaries.

---

## Phase 1 — What Was Built & How (Completed: Nov 11, 2026)

In **Phase 1**, the entire visual workspace and navigation architecture was built purely inside the **Java Swing presentation layer** (`src/com/clinic/ui/`), using flat static mock views ([`MockClinicData.java`](src/com/clinic/ui/mock/MockClinicData.java)) with **zero** backend services, **zero** database/file persistence, and **zero** API calls.

### 1. Multi-Step Entry Flow & Collapsible Desktop Shell
- **Visual Role Selector ([`RoleSelectFrame.java`](src/com/clinic/ui/RoleSelectFrame.java))**: Application launches via [`Main.java`](src/com/clinic/Main.java) into a 3-card portal for **Receptionist**, **Doctor / Clinician**, and **Administrator** with 60 FPS animated card elevation and 32px icons.
- **Role-Scoped Login ([`LoginFrame.java`](src/com/clinic/ui/LoginFrame.java))**: Displays a `"← Back to Role Selection"` navigation button, color-coded role portal badge, pre-filled staff username/password UI inputs, and a quick demo profile selector filtered to the selected role.
- **Main Shell ([`MainApplicationFrame.java`](src/com/clinic/ui/MainApplicationFrame.java))**:
  - **Collapsible Dark Slate Sidebar (`☰`)**: Toggles smoothly between a full 250px menu and a 76px icon-only rail. Dynamically filters menu buttons based on the logged-in role.
  - **Top Branding & Profile Header**: Displays clinic logo/branding on the left and an interactive user profile dropdown (`Dashboard Home`, `Switch Role`, `Sign Out`) on the right.
- **Role Dashboard Hub ([`DashboardHomePanel.java`](src/com/clinic/ui/panels/DashboardHomePanel.java))**: Greets each role with tailored summary `StatCard`s and clickable quick-launch module cards linking directly to authorized workspace panels.

### 2. Interactive Panels & Modal Form Dialogs

| Workspace Module | Panel Class | Connected UI Form Dialogs | Role Visibility |
| :--- | :--- | :--- | :--- |
| **Role Overview Hub** | `DashboardHomePanel.java` | — | Receptionist, Doctor, Administrator |
| **Patient Records** | `PatientManagementPanel.java` | `PatientDialog`, `PatientHistoryDialog` | Receptionist (Full UI), Administrator (Read-only UI) |
| **Doctor Directory** | `DoctorManagementPanel.java` | `DoctorDialog`, `DoctorScheduleDialog`, `DoctorAvailabilityDialog` | Administrator (Full UI), Receptionist (Directory & Duty Status) |
| **Appointment Desk** | `AppointmentManagementPanel.java` | `BookAppointmentDialog`, `RescheduleAppointmentDialog` | Receptionist (Full UI), Doctor (Own Queue), Administrator (Read-only UI) |
| **Clinical Workstation** | `DoctorConsultationPanel.java` | `PrescriptionItemDialog`, `PatientHistoryDialog` | **Doctor Only** (Locked to clinician profile) |
| **Billing & Receipts** | `BillingManagementPanel.java` | `PaymentDialog`, `PrintableReceiptDialog` | **Receptionist Only** (Cashier terminal) |
| **Analytics & Reports** | `ReportsPanel.java` | — | **Administrator Only** (4 report tabs) |

### 3. Modern Styling, 60 FPS Motion Engine & Bespoke Icons
- **FlatLaf 3.6 & High-DPI Theme ([`ThemeManager.java`](src/com/clinic/ui/theme/ThemeManager.java), [`ThemeColors.java`](src/com/clinic/ui/theme/ThemeColors.java), [`UIStyle.java`](src/com/clinic/ui/theme/UIStyle.java), [`UIBranding.java`](src/com/clinic/ui/theme/UIBranding.java))**:
  - Initializes `FlatLightLaf` (`lib/flatlaf-3.6.jar`) with sub-pixel font anti-aliasing (`Segoe UI`), rounded corners (`Button.arc=10`, `Component.arc=8`, `ScrollBar.thumbArc=999`), 40px styled tables, and a teal/slate healthcare color palette (`#0D9488`, `#0F172A`, `#F8FAFC`).
- **60 FPS Animation Utilities (`src/com/clinic/ui/animation/`)**:
  - `Animator.java`: Frame-accurate `javax.swing.Timer` supporting `EASE_OUT_QUAD`, `EASE_IN_OUT_CUBIC`, `EASE_OUT_CUBIC`, and `EASE_OUT_BACK`.
  - `ColorInterpolator.java`: Smooth RGB/Alpha hover color transitions for buttons, sidebar navigation pills, and cards.
  - `ToastNotification.java`: Floating slide-down toast alerts rendered on the window’s `JLayeredPane`.
- **Reusable Visual Primitives (`src/com/clinic/ui/components/`)**:
  - `ModernButton.java`: Rounded primary, secondary, success, and danger buttons with hover color interpolation.
  - `StatCard.java`: Elevated KPI cards with dynamic hover drop shadows and accent ribbons.
  - `StatusBadge.java`: Color-coded status pills (`AVAILABLE`, `UNAVAILABLE`, `SCHEDULED`, `COMPLETED`, `PAID`, `UNPAID`, `NO_SHOW`, `CANCELLED`).
- **Custom Transparent PNG Icon Suite ([`IconFactory.java`](src/com/clinic/ui/theme/IconFactory.java))**:
  - 15 bespoke transparent PNG icons at `24x24`, `32x32`, and `64x64` under `src/com/clinic/ui/icons/` (`workstation`, `patients`, `doctors`, `appointments`, `billing`, `reports`, `clinic_logo`), packaged into `bin/` during compilation with Java2D procedural fallback support.

### 4. Static Preview Data (`MockClinicData`)
- [`MockClinicData.java`](src/com/clinic/ui/mock/MockClinicData.java) supplies lightweight in-GUI display records populated with realistic Pakistani healthcare data (e.g., `Dr. Muhammad Bilal`, `Dr. Ayesha Tariq`, `Dr. Usman Farooq`, Lahore addresses, `03xx-...` contact formats, `Rs. ` consultation fees, and sample prescriptions) so every search filter, table, dropdown, form dialog, and receipt preview is immediately interactive during UI walkthroughs.

---

## Project Directory Structure (Phase 1 GUI)

```text
SDA Project/
├── .gitignore
├── compile.bat
├── run.bat
├── README.md
├── lib/
│   └── flatlaf-3.6.jar
└── src/
    └── com/
        └── clinic/
            ├── Main.java
            └── ui/
                ├── LoginFrame.java
                ├── MainApplicationFrame.java
                ├── RoleSelectFrame.java
                ├── animation/
                │   ├── Animator.java
                │   ├── ColorInterpolator.java
                │   └── ToastNotification.java
                ├── components/
                │   ├── ModernButton.java
                │   ├── StatCard.java
                │   └── StatusBadge.java
                ├── dialogs/
                │   ├── BookAppointmentDialog.java
                │   ├── DoctorAvailabilityDialog.java
                │   ├── DoctorDialog.java
                │   ├── DoctorScheduleDialog.java
                │   ├── PatientDialog.java
                │   ├── PatientHistoryDialog.java
                │   ├── PaymentDialog.java
                │   ├── PrescriptionItemDialog.java
                │   ├── PrintableReceiptDialog.java
                │   └── RescheduleAppointmentDialog.java
                ├── icons/
                │   ├── appointments_24.png / _32.png
                │   ├── billing_24.png / _32.png
                │   ├── clinic_logo_24.png / _32.png / _64.png
                │   ├── doctors_24.png / _32.png
                │   ├── patients_24.png / _32.png
                │   ├── reports_24.png / _32.png
                │   └── workstation_24.png / _32.png
                ├── mock/
                │   └── MockClinicData.java
                ├── panels/
                │   ├── AppointmentManagementPanel.java
                │   ├── BillingManagementPanel.java
                │   ├── DashboardHomePanel.java
                │   ├── DoctorConsultationPanel.java
                │   ├── DoctorManagementPanel.java
                │   ├── PatientManagementPanel.java
                │   └── ReportsPanel.java
                └── theme/
                    ├── IconFactory.java
                    ├── ThemeColors.java
                    ├── ThemeManager.java
                    ├── UIBranding.java
                    └── UIStyle.java
```

---

## How to Build & Run

### Prerequisites
- **Java Development Kit (JDK) 17 or JDK 21** (compiled with UTF-8 encoding).

### Quick Launch (Windows)
1. **Compile sources and copy PNG icons:**
   ```cmd
   compile.bat
   ```
2. **Launch the desktop GUI:**
   ```cmd
   run.bat
   ```

### Manual CLI Execution
```powershell
# Compile all UI sources
javac -encoding UTF-8 -cp "lib/*;bin" -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName

# Copy PNG assets to classpath output
Copy-Item src\com\clinic\ui\icons\*.png bin\com\clinic\ui\icons\ -Force

# Launch Application
java -cp "lib/*;bin" com.clinic.Main
```

---

## Git Branch Architecture

- `main`: Unified integration branch containing all Phase 1 GUI modules, frames, dialogs, icons, and styling.
- Dedicated panel & subsystem feature branches merged via reviewed Pull Requests:
  - `feature/patient-management-panel`
  - `feature/doctor-management-panel`
  - `feature/appointment-management-panel`
  - `feature/doctor-consultation-panel`
  - `feature/billing-management-panel`
  - `feature/reports-panel`
  - `feature/motion-animation-ui`
  - `feature/auth-rbac-security`
  - `feature/medical-iconography`
  - `feature/gui-only-architecture`
