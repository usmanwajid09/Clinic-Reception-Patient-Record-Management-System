package com.clinic.repository;

import com.clinic.model.Department;
import com.clinic.model.Doctor;
import java.util.List;

/**
 * Repository interface for Doctor entity (FR6, FR7, FR8, FR9, FR10).
 */
public interface IDoctorRepository extends IRepository<Doctor, String> {
    /**
     * Filters doctors by department and/or specialization (FR10).
     */
    List<Doctor> findByDepartmentAndSpecialization(String departmentName, String specialization);

    /**
     * Retrieves all registered departments (FR7).
     */
    List<Department> findAllDepartments();

    /**
     * Saves a department.
     */
    Department saveDepartment(Department department);

    /**
     * Generates next available doctor ID.
     */
    String generateNextDoctorId();
}
