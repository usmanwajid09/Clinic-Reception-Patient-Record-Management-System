package com.clinic.validation;

import com.clinic.model.Patient;
import com.clinic.service.ValidationException;

import java.time.LocalDate;

/**
 * Validates patient registration and profile update information (FR5).
 * Adheres to Single Responsibility Principle.
 */
public class PatientValidator implements IValidator<Patient> {

    @Override
    public void validate(Patient patient) throws ValidationException {
        if (patient == null) {
            throw new ValidationException("Patient record cannot be null.");
        }
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new ValidationException("Patient Name is mandatory and cannot be empty.");
        }
        if (patient.getDateOfBirth() == null) {
            throw new ValidationException("Date of Birth is mandatory.");
        }
        if (patient.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of Birth cannot be in the future.");
        }
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            throw new ValidationException("Gender is mandatory.");
        }
        if (patient.getAddress() == null || patient.getAddress().trim().isEmpty()) {
            throw new ValidationException("Residential Address is mandatory.");
        }
        if (patient.getContactNumber() == null || patient.getContactNumber().trim().isEmpty()) {
            throw new ValidationException("Contact Number is mandatory.");
        }
        if (patient.getEmergencyContact() == null || patient.getEmergencyContact().trim().isEmpty()) {
            throw new ValidationException("Emergency Contact Information is mandatory.");
        }
    }
}

// FR5: Validates mandatory patient fields
