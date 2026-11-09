package com.clinic.model;

/**
 * Concrete Receptionist user model.
 * Adheres to LSP: substitutable for base User.
 */
public class ReceptionistUser extends User {
    private final String frontDeskDeskNumber;

    public ReceptionistUser(String userId, String username, String fullName, String frontDeskDeskNumber) {
        super(userId, username, fullName, Role.RECEPTIONIST);
        this.frontDeskDeskNumber = frontDeskDeskNumber;
    }

    public String getFrontDeskDeskNumber() {
        return frontDeskDeskNumber;
    }

    @Override
    public boolean hasAdministrativeAccess() {
        return false;
    }

    @Override
    public boolean canConductConsultations() {
        return false;
    }

    @Override
    public boolean canManageFrontDesk() {
        return true;
    }
}

