package com.clinic.repository;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Appointment management (FR11, FR12, FR13, FR14, FR15, FR16).
 */
public interface IAppointmentRepository extends IRepository<Appointment, String> {
    /**
     * Checks if doctor already has an active (non-cancelled) appointment at the specified date and time slot (FR13).
     */
    boolean hasDoctorConflict(String doctorId, LocalDate date, TimeSlot timeSlot, String excludeAppointmentId);

    /**
     * Finds all appointments scheduled for a specific doctor on a selected date (FR16).
     */
    List<Appointment> findByDoctorAndDate(String doctorId, LocalDate date);

    /**
     * Finds all appointments scheduled for a specific patient.
     */
    List<Appointment> findByPatientId(String patientId);

    /**
     * Finds appointments within a specified date range (FR27).
     */
    List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Calculates the next sequential token number for a doctor on a specific date (FR12).
     */
    int getNextTokenNumber(String doctorId, LocalDate date);

    /**
     * Generates next available unique appointment ID.
     */
    String generateNextAppointmentId();
}
