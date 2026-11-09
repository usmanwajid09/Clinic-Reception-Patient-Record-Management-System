package com.clinic.repository.inmemory;

import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.TimeSlot;
import com.clinic.repository.IAppointmentRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of IAppointmentRepository.
 */
public class InMemoryAppointmentRepository implements IAppointmentRepository {
    private final Map<String, Appointment> storage = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(5000);

    @Override
    public Appointment save(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        storage.put(appointment.getAppointmentId().toUpperCase(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id.trim().toUpperCase()));
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        return storage.containsKey(id.trim().toUpperCase());
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            storage.remove(id.trim().toUpperCase());
        }
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public boolean hasDoctorConflict(String doctorId, LocalDate date, TimeSlot timeSlot, String excludeAppointmentId) {
        if (doctorId == null || date == null || timeSlot == null) return false;

        return storage.values().stream()
                .filter(app -> app.getDoctorId().equalsIgnoreCase(doctorId))
                .filter(app -> app.getAppointmentDate().equals(date))
                .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
                .filter(app -> excludeAppointmentId == null || !app.getAppointmentId().equalsIgnoreCase(excludeAppointmentId))
                .anyMatch(app -> app.getTimeSlot().overlaps(timeSlot));
    }

    @Override
    public List<Appointment> findByDoctorAndDate(String doctorId, LocalDate date) {
        if (doctorId == null || date == null) return Collections.emptyList();

        return storage.values().stream()
                .filter(app -> app.getDoctorId().equalsIgnoreCase(doctorId))
                .filter(app -> app.getAppointmentDate().equals(date))
                .sorted(Comparator.comparingInt(Appointment::getTokenNumber))
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByPatientId(String patientId) {
        if (patientId == null) return Collections.emptyList();

        return storage.values().stream()
                .filter(app -> app.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        return storage.values().stream()
                .filter(app -> !app.getAppointmentDate().isBefore(start) && !app.getAppointmentDate().isAfter(end))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparingInt(Appointment::getTokenNumber))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized int getNextTokenNumber(String doctorId, LocalDate date) {
        int maxToken = storage.values().stream()
                .filter(app -> app.getDoctorId().equalsIgnoreCase(doctorId))
                .filter(app -> app.getAppointmentDate().equals(date))
                .mapToInt(Appointment::getTokenNumber)
                .max()
                .orElse(0);
        return maxToken + 1;
    }

    @Override
    public String generateNextAppointmentId() {
        return "APT-" + sequence.incrementAndGet();
    }
}

// Strict stream filtering for doctor schedule collision detection
