package com.clinic.model;

/**
 * Concrete Doctor user model.
 * Associates authenticated user with their clinician profile.
 * Adheres to LSP.
 */
public class DoctorUser extends User {
    private final String doctorProfileId;

    public DoctorUser(String userId, String username, String fullName, String doctorProfileId) {
        super(userId, username, fullName, Role.DOCTOR);
        this.doctorProfileId = doctorProfileId;
    }

    public String getDoctorProfileId() {
        return doctorProfileId;
    }

    @Override
    public boolean hasAdministrativeAccess() {
        return false;
    }

    @Override
    public boolean canConductConsultations() {
        return true;
    }

    @Override
    public boolean canManageFrontDesk() {
        return false;
    }
}
