package com.clinic.repository;

import com.clinic.model.Patient;
import java.util.List;

/**
 * Repository interface for Patient entity (FR1, FR2, FR3, FR4).
 */
public interface IPatientRepository extends IRepository<Patient, String> {
    /**
     * Search patients by ID, Name, or Contact Number (FR2).
     */
    List<Patient> search(String query);

    /**
     * Finds next available sequential patient ID.
     */
    String generateNextPatientId();
}

// ISP: Segregated patient repository operations
