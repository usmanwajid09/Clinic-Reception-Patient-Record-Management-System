package com.clinic.config;

/**
 * System-wide constants and clinic configuration parameters.
 */
public final class AppConfig {
    private AppConfig() {}

    public static final String CLINIC_NAME = "Apex Health & Wellness Clinic";
    public static final String CLINIC_TAGLINE = "Excellence in Patient Care & Clinical Management";
    public static final String CLINIC_ADDRESS = "Suite 400, Medical Arts Complex, Health Ave";
    public static final String CLINIC_CONTACT = "+1 (555) 019-2834 | contact@apexclinic.health";

    // Fixed clinic service charge applied to consultations (FR24)
    public static final double FIXED_CLINIC_SERVICE_CHARGE = 15.00;

    // Currency representation
    public static final String CURRENCY_SYMBOL = "$";

    // Application window defaults
    public static final int APP_WIDTH = 1200;
    public static final int APP_HEIGHT = 800;
}
