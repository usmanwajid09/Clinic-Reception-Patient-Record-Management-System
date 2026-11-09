package com.clinic.service;

import com.clinic.model.Consultation;
import com.clinic.model.Prescription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service contract for Clinical Consultations and Electronic Prescriptions (FR17 - FR23).
 */
public interface IConsultationService {
    Consultation startConsultation(String appointmentId);

    void recordClinicalFindings(String consultationId, String symptoms, String diagnosis, String notes);

    void attachPrescription(String consultationId, Prescription prescription);

    Consultation finalizeAndCompleteConsultation(String consultationId);

    Optional<Consultation> getConsultationById(String consultationId);

    Optional<Consultation> getConsultationByAppointmentId(String appointmentId);

    List<Consultation> getConsultationsForDoctor(String doctorId);

    List<Consultation> getConsultationsForPatient(String patientId);

    List<Consultation> getConsultationsByDateRange(LocalDate startDate, LocalDate endDate);
}
