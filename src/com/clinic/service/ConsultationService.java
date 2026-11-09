package com.clinic.service;

import com.clinic.model.*;
import com.clinic.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete ConsultationService adhering to SRP and DIP.
 * Enforces consultation immutability (FR23), historical linking (FR22),
 * and triggers billing integration (FR24).
 */
public class ConsultationService implements IConsultationService {
    private final IConsultationRepository consultationRepository;
    private final IAppointmentRepository appointmentRepository;
    private final IPatientRepository patientRepository;
    private final IBillingService billingService;

    public ConsultationService(IConsultationRepository consultationRepository,
                               IAppointmentRepository appointmentRepository,
                               IPatientRepository patientRepository,
                               IBillingService billingService) {
        this.consultationRepository = Objects.requireNonNull(consultationRepository, "consultationRepository required");
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "appointmentRepository required");
        this.patientRepository = Objects.requireNonNull(patientRepository, "patientRepository required");
        this.billingService = Objects.requireNonNull(billingService, "billingService required");
    }

    @Override
    public synchronized Consultation startConsultation(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found: " + appointmentId));

        // Check if consultation already exists for this appointment
        Optional<Consultation> existing = consultationRepository.findByAppointmentId(appointmentId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String consultId = consultationRepository.generateNextConsultationId();
        Consultation consultation = new Consultation(consultId, appointmentId, appointment.getPatientId(), appointment.getDoctorId());
        return consultationRepository.save(consultation);
    }

    @Override
    public synchronized void recordClinicalFindings(String consultationId, String symptoms, String diagnosis, String notes) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ValidationException("Consultation not found: " + consultationId));

        if (consultation.isCompleted()) {
            throw new ValidationException("Modification Forbidden (FR23): Consultation is completed and locked.");
        }

        consultation.setSymptoms(symptoms);
        consultation.setDiagnosis(diagnosis);
        consultation.setNotes(notes);
        consultationRepository.save(consultation);
    }

    @Override
    public synchronized void attachPrescription(String consultationId, Prescription prescription) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ValidationException("Consultation not found: " + consultationId));

        if (consultation.isCompleted()) {
            throw new ValidationException("Modification Forbidden (FR23): Consultation is completed and locked.");
        }

        consultation.setPrescription(prescription);
        consultationRepository.save(consultation);
    }

    @Override
    public synchronized Consultation finalizeAndCompleteConsultation(String consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ValidationException("Consultation not found: " + consultationId));

        if (consultation.isCompleted()) {
            return consultation;
        }

        if (consultation.getDiagnosis() == null || consultation.getDiagnosis().trim().isEmpty()) {
            throw new ValidationException("Clinical Diagnosis is required to complete consultation.");
        }

        // 1. Lock the consultation record permanently (FR23)
        consultation.complete();
        consultationRepository.save(consultation);

        // 2. Update appointment status to COMPLETED (FR15)
        Appointment appointment = appointmentRepository.findById(consultation.getAppointmentId()).orElse(null);
        if (appointment != null) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        // 3. Store consultation in patient's permanent medical history (FR22)
        patientRepository.findById(consultation.getPatientId()).ifPresent(patient -> {
            patient.addConsultationRecord(consultation.getConsultationId());
            patientRepository.save(patient);
        });

        // 4. Automatically generate clinical bill for completed consultation (FR24)
        billingService.generateBillForConsultation(consultation);

        return consultation;
    }

    @Override
    public Optional<Consultation> getConsultationById(String consultationId) {
        return consultationRepository.findById(consultationId);
    }

    @Override
    public Optional<Consultation> getConsultationByAppointmentId(String appointmentId) {
        return consultationRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public List<Consultation> getConsultationsForDoctor(String doctorId) {
        return consultationRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Consultation> getConsultationsForPatient(String patientId) {
        return consultationRepository.findByPatientId(patientId);
    }

    @Override
    public List<Consultation> getConsultationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return consultationRepository.findByDateRange(startDate, endDate);
    }
}

// FR23: Locks consultation against subsequent unauthorized modifications
