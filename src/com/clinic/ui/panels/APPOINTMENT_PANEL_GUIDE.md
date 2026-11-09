# Appointment Scheduling & Queue Management Subsystem Guide

## Overview & Functional Scope
This panel coordinates all front-desk patient bookings, sequential token assignments, and strict collision detection:
- **FR11 (Appointment Booking)**: Front-desk receptionist creates an appointment for any registered patient with an available doctor.
- **FR12 (Audit & Token Generation)**: Records appointment date, time, sequential token number (e.g. `Token #1`), and status.
- **FR13 (Conflict Prevention Engine)**: Strictly prevents double-booking. Intercepts any attempt to assign two appointments to the same doctor on the same date and time slot.
- **FR14 (Rescheduling & Cancellation)**: Enables rescheduling to an alternate available slot (instantly freeing the previous slot) or cancellation with recorded audit reason.
- **FR15 (Lifecycle Status Tracking)**: Maintains appointment status across `Scheduled`, `Completed`, `Cancelled`, and `No-Show`.
- **FR16 (Daily Doctor Queues)**: Displays appointment queues for any selected doctor and calendar date.

## Component & Architecture Map
- **UI View**: `AppointmentManagementPanel.java`
- **Dialogs**: `BookAppointmentDialog.java` (Intake and slot selection), `RescheduleAppointmentDialog.java` (Slot shifting)
- **Validator**: `AppointmentValidator.java` (Implements `IValidator<Appointment>`)
- **Service**: `AppointmentService.java` (Implements `IAppointmentService`)
- **Repository**: `InMemoryAppointmentRepository.java`
- **Domain Entities**: `Appointment.java`, `TimeSlot.java`, `AppointmentStatus.java`

## Security & Privacy Rules
- **Receptionist**: Full booking, rescheduling, and cancellation authority across all clinic doctors.
- **Doctor**: Strictly isolated to their own scheduled appointment queue. Booking and cancellation controls are disabled. Can mark patient as No-Show.
- **Administrator**: Read-only oversight of all clinic queues and booking statistics.

