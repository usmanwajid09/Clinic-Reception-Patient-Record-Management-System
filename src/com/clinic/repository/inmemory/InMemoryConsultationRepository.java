package com.clinic.repository.inmemory;

import com.clinic.model.Consultation;
import com.clinic.repository.IConsultationRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of IConsultationRepository.
 */
public class InMemoryConsultationRepository implements IConsultationRepository {
    private final Map<String, Consultation> storage = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(7000);

    @Override
    public Consultation save(Consultation consultation) {
        Objects.requireNonNull(consultation, "consultation cannot be null");
        storage.put(consultation.getConsultationId().toUpperCase(), consultation);
        return consultation;
    }

    @Override
    public Optional<Consultation> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id.trim().toUpperCase()));
    }

    @Override
    public List<Consultation> findAll() {
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
    public Optional<Consultation> findByAppointmentId(String appointmentId) {
        if (appointmentId == null) return Optional.empty();

        return storage.values().stream()
                .filter(c -> c.getAppointmentId().equalsIgnoreCase(appointmentId.trim()))
                .findFirst();
    }

    @Override
    public List<Consultation> findByPatientId(String patientId) {
        if (patientId == null) return Collections.emptyList();

        return storage.values().stream()
                .filter(c -> c.getPatientId().equalsIgnoreCase(patientId.trim()))
                .sorted(Comparator.comparing(Consultation::getConsultationTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Consultation> findByDoctorId(String doctorId) {
        if (doctorId == null) return Collections.emptyList();

        return storage.values().stream()
                .filter(c -> c.getDoctorId().equalsIgnoreCase(doctorId.trim()))
                .sorted(Comparator.comparing(Consultation::getConsultationTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Consultation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        return storage.values().stream()
                .filter(c -> {
                    LocalDate consultDate = c.getConsultationTime().toLocalDate();
                    return !consultDate.isBefore(start) && !consultDate.isAfter(end);
                })
                .sorted(Comparator.comparing(Consultation::getConsultationTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String generateNextConsultationId() {
        return "CON-" + sequence.incrementAndGet();
    }
}
