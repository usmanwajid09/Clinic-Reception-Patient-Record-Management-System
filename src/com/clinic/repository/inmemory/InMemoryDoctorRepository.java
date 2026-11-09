package com.clinic.repository.inmemory;

import com.clinic.model.Department;
import com.clinic.model.Doctor;
import com.clinic.repository.IDoctorRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of IDoctorRepository.
 */
public class InMemoryDoctorRepository implements IDoctorRepository {
    private final Map<String, Doctor> doctorStorage = new ConcurrentHashMap<>();
    private final Map<String, Department> departmentStorage = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(100);

    @Override
    public Doctor save(Doctor doctor) {
        Objects.requireNonNull(doctor, "doctor cannot be null");
        doctorStorage.put(doctor.getDoctorId().toUpperCase(), doctor);
        saveDepartment(doctor.getDepartment());
        return doctor;
    }

    @Override
    public Optional<Doctor> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(doctorStorage.get(id.trim().toUpperCase()));
    }

    @Override
    public List<Doctor> findAll() {
        return new ArrayList<>(doctorStorage.values());
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        return doctorStorage.containsKey(id.trim().toUpperCase());
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            doctorStorage.remove(id.trim().toUpperCase());
        }
    }

    @Override
    public long count() {
        return doctorStorage.size();
    }

    @Override
    public List<Doctor> findByDepartmentAndSpecialization(String departmentName, String specialization) {
        return doctorStorage.values().stream()
                .filter(doc -> {
                    boolean matchDept = (departmentName == null || departmentName.trim().isEmpty() || "All Departments".equalsIgnoreCase(departmentName.trim())
                            || doc.getDepartment().getName().equalsIgnoreCase(departmentName.trim()));
                    boolean matchSpec = (specialization == null || specialization.trim().isEmpty() || "All Specializations".equalsIgnoreCase(specialization.trim())
                            || doc.getSpecialization().equalsIgnoreCase(specialization.trim()));
                    return matchDept && matchSpec;
                })
                .sorted(Comparator.comparing(Doctor::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Department> findAllDepartments() {
        return new ArrayList<>(departmentStorage.values());
    }

    @Override
    public Department saveDepartment(Department department) {
        if (department != null) {
            departmentStorage.put(department.getDepartmentId().toUpperCase(), department);
        }
        return department;
    }

    @Override
    public String generateNextDoctorId() {
        return "DOC-" + sequence.incrementAndGet();
    }
}
