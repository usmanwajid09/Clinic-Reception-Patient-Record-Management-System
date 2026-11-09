package com.clinic.service;

import com.clinic.model.Consultation;
import com.clinic.model.Patient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service contract for Patient Registration & Record Management (FR1, FR2, FR3, FR4, FR5).
 */
public interface IPatientService {
    Patient registerPatient(String name, LocalDate dateOfBirth, String gender,
                            String address, String contactNumber, String emergencyContact);

    Patient updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender,
                          String address, String contactNumber, String emergencyContact);

    Optional<Patient> getPatientById(String patientId);

    List<Patient> searchPatients(String query);

    List<Patient> getAllPatients();

    List<Consultation> getPatientMedicalHistory(String patientId);
}
