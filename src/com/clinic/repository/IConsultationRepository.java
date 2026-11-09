package com.clinic.repository;

import com.clinic.model.Consultation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for clinical consultations (FR17 - FR23, FR28, FR29).
 */
public interface IConsultationRepository extends IRepository<Consultation, String> {
    /**
     * Finds consultation by appointment ID.
     */
    Optional<Consultation> findByAppointmentId(String appointmentId);

    /**
     * Finds all consultations recorded for a given patient (FR4, FR22, FR29).
     */
    List<Consultation> findByPatientId(String patientId);

    /**
     * Finds all consultations recorded for a given doctor (FR28).
     */
    List<Consultation> findByDoctorId(String doctorId);

    /**
     * Finds all consultations completed within a date range (FR28).
     */
    List<Consultation> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Generates next available unique consultation ID.
     */
    String generateNextConsultationId();
}
