package com.clinic.service;

import com.clinic.model.Department;
import com.clinic.model.Doctor;
import com.clinic.model.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service contract for Doctor & Schedule Management (FR6, FR7, FR8, FR9, FR10).
 */
public interface IDoctorService {
    Doctor registerDoctor(String name, String specialization, String qualification,
                          double consultationFee, Department department);

    void setDoctorSchedule(String doctorId, Collection<DayOfWeek> workingDays, Collection<TimeSlot> slots);

    void setDoctorAvailabilityForDate(String doctorId, LocalDate date, boolean available);

    boolean isDoctorAvailable(String doctorId, LocalDate date);

    List<Doctor> filterDoctors(String departmentName, String specialization);

    List<Doctor> getAllDoctors();

    Optional<Doctor> getDoctorById(String doctorId);

    List<Department> getAllDepartments();

    Department registerDepartment(String name, String description);
}

