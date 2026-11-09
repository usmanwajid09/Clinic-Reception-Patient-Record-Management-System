package com.clinic.service;

import com.clinic.model.Department;
import com.clinic.model.Doctor;
import com.clinic.model.TimeSlot;
import com.clinic.repository.IDoctorRepository;
import com.clinic.validation.DoctorValidator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete DoctorService adhering to SRP and DIP.
 * Manages doctor profiles (FR6), department associations (FR7),
 * schedule definitions (FR8), day-specific availability (FR9),
 * and filtered catalog views (FR10).
 */
public class DoctorService implements IDoctorService {
    private final IDoctorRepository doctorRepository;
    private final DoctorValidator validator;

    public DoctorService(IDoctorRepository doctorRepository) {
        this.doctorRepository = Objects.requireNonNull(doctorRepository, "doctorRepository required");
        this.validator = new DoctorValidator();
    }

    @Override
    public Doctor registerDoctor(String name, String specialization, String qualification,
                                 double consultationFee, Department department) {
        String doctorId = doctorRepository.generateNextDoctorId();
        Doctor doctor = new Doctor(doctorId, name, specialization, qualification, consultationFee, department);

        validator.validate(doctor);
        return doctorRepository.save(doctor);
    }

    @Override
    public void setDoctorSchedule(String doctorId, Collection<DayOfWeek> workingDays, Collection<TimeSlot> slots) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found with ID: " + doctorId));

        if (workingDays == null || workingDays.isEmpty()) {
            throw new ValidationException("Doctor must have at least one designated working day.");
        }
        if (slots == null || slots.isEmpty()) {
            throw new ValidationException("Doctor must have at least one consultation time slot configured.");
        }

        doctor.setWorkingDays(workingDays);
        doctor.setConsultationSlots(slots);
        doctorRepository.save(doctor);
    }

    @Override
    public void setDoctorAvailabilityForDate(String doctorId, LocalDate date, boolean available) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found with ID: " + doctorId));

        if (date == null) {
            throw new ValidationException("Date cannot be null.");
        }

        doctor.setAvailabilityForDate(date, available);
        doctorRepository.save(doctor);
    }

    @Override
    public boolean isDoctorAvailable(String doctorId, LocalDate date) {
        return doctorRepository.findById(doctorId)
                .map(doc -> doc.isAvailableOn(date))
                .orElse(false);
    }

    @Override
    public List<Doctor> filterDoctors(String departmentName, String specialization) {
        return doctorRepository.findByDepartmentAndSpecialization(departmentName, specialization);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Optional<Doctor> getDoctorById(String doctorId) {
        return doctorRepository.findById(doctorId);
    }

    @Override
    public List<Department> getAllDepartments() {
        return doctorRepository.findAllDepartments();
    }

    @Override
    public Department registerDepartment(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Department name cannot be empty.");
        }
        String depId = "DEP-0" + (doctorRepository.findAllDepartments().size() + 1);
        Department dep = new Department(depId, name.trim(), description);
        return doctorRepository.saveDepartment(dep);
    }
}
