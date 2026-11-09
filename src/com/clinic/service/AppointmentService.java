package com.clinic.service;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Doctor;
import com.clinic.model.TimeSlot;
import com.clinic.repository.IAppointmentRepository;
import com.clinic.repository.IDoctorRepository;
import com.clinic.repository.IPatientRepository;
import com.clinic.validation.AppointmentValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete AppointmentService adhering to SRP and DIP.
 * Enforces strict double-booking collision prevention (FR13),
 * token generation (FR12), and appointment lifecycle transitions (FR14, FR15).
 */
public class AppointmentService implements IAppointmentService {
    private final IAppointmentRepository appointmentRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientRepository patientRepository;
    private final AppointmentValidator validator;

    public AppointmentService(IAppointmentRepository appointmentRepository,
                              IDoctorRepository doctorRepository,
                              IPatientRepository patientRepository) {
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "appointmentRepository required");
        this.doctorRepository = Objects.requireNonNull(doctorRepository, "doctorRepository required");
        this.patientRepository = Objects.requireNonNull(patientRepository, "patientRepository required");
        this.validator = new AppointmentValidator();
    }

    @Override
    public synchronized Appointment createAppointment(String patientId, String doctorId,
                                                      LocalDate date, TimeSlot timeSlot, String notes) {
        // 1. Verify Patient exists
        if (!patientRepository.existsById(patientId)) {
            throw new ValidationException("Patient not found with ID: " + patientId);
        }

        // 2. Verify Doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found with ID: " + doctorId));

        // 3. Verify Doctor is Available on that date (FR9, FR11)
        if (!doctor.isAvailableOn(date)) {
            throw new ValidationException("Doctor " + doctor.getName() + " is not available on " + date + ".");
        }

        // 4. Check for double-booking conflict (FR13)
        if (appointmentRepository.hasDoctorConflict(doctorId, date, timeSlot, null)) {
            throw new ValidationException("Double-Booking Prevented (FR13): Doctor " + doctor.getName()
                    + " already has an appointment scheduled at " + timeSlot.getFormatted() + " on " + date + ".");
        }

        // 5. Generate sequential token number for this doctor on this day (FR12)
        int tokenNumber = appointmentRepository.getNextTokenNumber(doctorId, date);
        String appointmentId = appointmentRepository.generateNextAppointmentId();

        Appointment appointment = new Appointment(appointmentId, patientId, doctorId, date, timeSlot, tokenNumber);
        appointment.setNotes(notes);

        validator.validate(appointment, doctor);

        return appointmentRepository.save(appointment);
    }

    @Override
    public synchronized Appointment rescheduleAppointment(String appointmentId, LocalDate newDate, TimeSlot newTimeSlot) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Cannot reschedule an already completed appointment.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("Cannot reschedule a cancelled appointment.");
        }

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new ValidationException("Assigned doctor not found."));

        if (!doctor.isAvailableOn(newDate)) {
            throw new ValidationException("Doctor " + doctor.getName() + " is not available on " + newDate + ".");
        }

        // Check for double-booking conflict excluding current appointment (FR13)
        if (appointmentRepository.hasDoctorConflict(appointment.getDoctorId(), newDate, newTimeSlot, appointmentId)) {
            throw new ValidationException("Conflict Detected (FR13): Doctor already booked at "
                    + newTimeSlot.getFormatted() + " on " + newDate + ".");
        }

        // Assign new token if date changed
        if (!appointment.getAppointmentDate().equals(newDate)) {
            int newToken = appointmentRepository.getNextTokenNumber(appointment.getDoctorId(), newDate);
            appointment.setTokenNumber(newToken);
        }

        appointment.setAppointmentDate(newDate);
        appointment.setTimeSlot(newTimeSlot);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    @Override
    public synchronized void cancelAppointment(String appointmentId, String cancellationReason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel an already completed appointment.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (cancellationReason != null && !cancellationReason.trim().isEmpty()) {
            appointment.setNotes((appointment.getNotes() != null ? appointment.getNotes() + " | " : "") + "Cancelled: " + cancellationReason.trim());
        }
        appointmentRepository.save(appointment);
    }

    @Override
    public synchronized void updateAppointmentStatus(String appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found: " + appointmentId));

        appointment.setStatus(Objects.requireNonNull(newStatus, "newStatus required"));
        appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getDoctorAppointmentsForDate(String doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorAndDate(doctorId, date);
    }

    @Override
    public List<Appointment> getPatientAppointments(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Optional<Appointment> getAppointmentById(String appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }
}
