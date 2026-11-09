package com.clinic.repository.inmemory;

import com.clinic.model.Patient;
import com.clinic.repository.IPatientRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of IPatientRepository.
 */
public class InMemoryPatientRepository implements IPatientRepository {
    private final Map<String, Patient> storage = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    @Override
    public Patient save(Patient patient) {
        Objects.requireNonNull(patient, "patient cannot be null");
        storage.put(patient.getPatientId().toUpperCase(), patient);
        return patient;
    }

    @Override
    public Optional<Patient> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id.trim().toUpperCase()));
    }

    @Override
    public List<Patient> findAll() {
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
    public List<Patient> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String cleanQuery = query.trim().toLowerCase();
        return storage.values().stream()
                .filter(p -> p.getPatientId().toLowerCase().contains(cleanQuery)
                        || p.getName().toLowerCase().contains(cleanQuery)
                        || p.getContactNumber().contains(cleanQuery))
                .sorted(Comparator.comparing(Patient::getName))
                .collect(Collectors.toList());
    }

    @Override
    public String generateNextPatientId() {
        return "PAT-" + sequence.incrementAndGet();
    }
}
