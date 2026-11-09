package com.clinic.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Represents a registered doctor in the clinic management system (FR6, FR7, FR8, FR9, FR10).
 */
public class Doctor {
    private final String doctorId;
    private String name;
    private String specialization;
    private String qualification;
    private double consultationFee;
    private Department department; // Associated with one department (FR7)

    // Working days of the week (FR8)
    private final Set<DayOfWeek> workingDays;

    // Available consultation time slots for working days (FR8)
    private final List<TimeSlot> consultationSlots;

    // Dates where the doctor is explicitly marked Unavailable (FR9)
    private final Set<LocalDate> unavailableDates;

    public Doctor(String doctorId, String name, String specialization, String qualification,
                  double consultationFee, Department department) {
        this.doctorId = Objects.requireNonNull(doctorId, "doctorId required");
        this.name = Objects.requireNonNull(name, "name required");
        this.specialization = Objects.requireNonNull(specialization, "specialization required");
        this.qualification = Objects.requireNonNull(qualification, "qualification required");
        this.consultationFee = consultationFee;
        this.department = Objects.requireNonNull(department, "department required");
        this.workingDays = new HashSet<>();
        this.consultationSlots = new ArrayList<>();
        this.unavailableDates = new HashSet<>();
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = Objects.requireNonNull(department, "department required");
    }

    public Set<DayOfWeek> getWorkingDays() {
        return Collections.unmodifiableSet(workingDays);
    }

    public synchronized void setWorkingDays(Collection<DayOfWeek> days) {
        this.workingDays.clear();
        if (days != null) {
            this.workingDays.addAll(days);
        }
    }

    public List<TimeSlot> getConsultationSlots() {
        return Collections.unmodifiableList(consultationSlots);
    }

    public synchronized void setConsultationSlots(Collection<TimeSlot> slots) {
        this.consultationSlots.clear();
        if (slots != null) {
            this.consultationSlots.addAll(slots);
            Collections.sort(this.consultationSlots);
        }
    }

    public synchronized void addTimeSlot(TimeSlot slot) {
        if (slot != null && !consultationSlots.contains(slot)) {
            consultationSlots.add(slot);
            Collections.sort(consultationSlots);
        }
    }

    public Set<LocalDate> getUnavailableDates() {
        return Collections.unmodifiableSet(unavailableDates);
    }

    /**
     * Marks doctor Available or Unavailable for a specified day (FR9).
     */
    public synchronized void setAvailabilityForDate(LocalDate date, boolean available) {
        if (date == null) return;
        if (available) {
            unavailableDates.remove(date);
        } else {
            unavailableDates.add(date);
        }
    }

    /**
     * Determines whether the doctor is available for consultations on a specific date (FR9).
     */
    public boolean isAvailableOn(LocalDate date) {
        if (date == null) return false;
        // Check if explicitly marked unavailable for this date
        if (unavailableDates.contains(date)) {
            return false;
        }
        // Check if day of week is one of doctor's working days
        return workingDays.contains(date.getDayOfWeek());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor doctor)) return false;
        return doctorId.equalsIgnoreCase(doctor.doctorId);
    }

    @Override
    public int hashCode() {
        return doctorId.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + specialization + " - " + department.getName() + ")";
    }
}
