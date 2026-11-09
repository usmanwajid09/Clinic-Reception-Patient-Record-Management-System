package com.clinic.validation;

import com.clinic.model.Doctor;
import com.clinic.service.ValidationException;

/**
 * Validates doctor registration and profile data (FR6, FR7, FR8).
 * Adheres to Single Responsibility Principle.
 */
public class DoctorValidator implements IValidator<Doctor> {

    @Override
    public void validate(Doctor doctor) throws ValidationException {
        if (doctor == null) {
            throw new ValidationException("Doctor record cannot be null.");
        }
        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            throw new ValidationException("Doctor Name is mandatory.");
        }
        if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
            throw new ValidationException("Specialization is mandatory.");
        }
        if (doctor.getQualification() == null || doctor.getQualification().trim().isEmpty()) {
            throw new ValidationException("Medical Qualification is mandatory.");
        }
        if (doctor.getConsultationFee() < 0) {
            throw new ValidationException("Consultation Fee cannot be negative.");
        }
        if (doctor.getDepartment() == null) {
            throw new ValidationException("Doctor must be associated with a Department (FR7).");
        }
    }
}
