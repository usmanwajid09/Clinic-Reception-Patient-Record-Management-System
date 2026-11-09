package com.clinic.service;

import com.clinic.model.Consultation;
import com.clinic.model.Patient;
import com.clinic.repository.IConsultationRepository;
import com.clinic.repository.IPatientRepository;
import com.clinic.validation.PatientValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete PatientService adhering to SRP and DIP.
 * Manages patient registration, validation (FR5), searching (FR2),
 * information updating (FR3), and medical history retrieval (FR4).
 */
public class PatientService implements IPatientService {
    private final IPatientRepository patientRepository;
    private final IConsultationRepository consultationRepository;
    private final PatientValidator validator;

    public PatientService(IPatientRepository patientRepository,
                          IConsultationRepository consultationRepository) {
        this.patientRepository = Objects.requireNonNull(patientRepository, "patientRepository required");
        this.consultationRepository = Objects.requireNonNull(consultationRepository, "consultationRepository required");
        this.validator = new PatientValidator();
    }

    @Override
    public Patient registerPatient(String name, LocalDate dateOfBirth, String gender,
                                   String address, String contactNumber, String emergencyContact) {
        String patientId = patientRepository.generateNextPatientId();
        Patient patient = new Patient(patientId, name, dateOfBirth, gender, address, contactNumber, emergencyContact);

        // Enforce mandatory information validation (FR5)
        validator.validate(patient);

        return patientRepository.save(patient);
    }

    @Override
    public Patient updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender,
                                 String address, String contactNumber, String emergencyContact) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ValidationException("Patient not found with ID: " + patientId));

        patient.setName(name);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setAddress(address);
        patient.setContactNumber(contactNumber);
        patient.setEmergencyContact(emergencyContact);

        // Enforce mandatory validation on update (FR5)
        validator.validate(patient);

        return patientRepository.save(patient);
    }

    @Override
    public Optional<Patient> getPatientById(String patientId) {
        return patientRepository.findById(patientId);
    }

    @Override
    public List<Patient> searchPatients(String query) {
        return patientRepository.search(query);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public List<Consultation> getPatientMedicalHistory(String patientId) {
        return consultationRepository.findByPatientId(patientId);
    }
}
