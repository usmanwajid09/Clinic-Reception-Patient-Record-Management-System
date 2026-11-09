package com.clinic.validation;

import com.clinic.model.Appointment;
import com.clinic.model.Doctor;
import com.clinic.service.ValidationException;

import java.time.LocalDate;

/**
 * Validates appointment scheduling parameters.
 * Adheres to Single Responsibility Principle.
 */
public class AppointmentValidator {

    public void validate(Appointment appointment, Doctor doctor) throws ValidationException {
        if (appointment == null) {
            throw new ValidationException("Appointment cannot be null.");
        }
        if (appointment.getPatientId() == null || appointment.getPatientId().trim().isEmpty()) {
            throw new ValidationException("Patient selection is mandatory.");
        }
        if (appointment.getDoctorId() == null || appointment.getDoctorId().trim().isEmpty()) {
            throw new ValidationException("Doctor selection is mandatory.");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new ValidationException("Appointment Date is mandatory.");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Appointment Date cannot be in the past.");
        }
        if (appointment.getTimeSlot() == null) {
            throw new ValidationException("Consultation Time Slot is mandatory.");
        }
        if (doctor != null) {
            if (!doctor.isAvailableOn(appointment.getAppointmentDate())) {
                throw new ValidationException("Doctor " + doctor.getName() + " is not available on " + appointment.getAppointmentDate() + ".");
            }
        }
    }
}

// Validates date in future and availability
