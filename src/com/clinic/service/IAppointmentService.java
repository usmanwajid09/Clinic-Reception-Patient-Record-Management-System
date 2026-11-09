package com.clinic.service;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service contract for Appointment Management & Conflict Engine (FR11, FR12, FR13, FR14, FR15, FR16).
 */
public interface IAppointmentService {
    Appointment createAppointment(String patientId, String doctorId, LocalDate date, TimeSlot timeSlot, String notes);

    Appointment rescheduleAppointment(String appointmentId, LocalDate newDate, TimeSlot newTimeSlot);

    void cancelAppointment(String appointmentId, String cancellationReason);

    void updateAppointmentStatus(String appointmentId, AppointmentStatus newStatus);

    List<Appointment> getDoctorAppointmentsForDate(String doctorId, LocalDate date);

    List<Appointment> getPatientAppointments(String patientId);

    List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate);

    List<Appointment> getAllAppointments();

    Optional<Appointment> getAppointmentById(String appointmentId);
}

