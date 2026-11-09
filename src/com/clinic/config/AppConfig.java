package com.clinic.config;

/**
 * System-wide constants and clinic configuration parameters.
 */
public final class AppConfig {
    private AppConfig() {}

    public static final String CLINIC_NAME = "Shifa Clinic & Patient Healthcare Center";
    public static final String CLINIC_TAGLINE = "Quality Healthcare & Patient Record Management";
    public static final String CLINIC_ADDRESS = "Plot 42-B, Main Commercial Avenue, Phase 5 DHA, Lahore, Pakistan";
    public static final String CLINIC_CONTACT = "+92 (42) 3578-9012 | contact@shifaclinic.pk";

    // Fixed clinic administrative/facility service charge applied to consultations (FR24)
    public static final double FIXED_CLINIC_SERVICE_CHARGE = 500.00;

    // Currency representation
    public static final String CURRENCY_SYMBOL = "Rs. ";

    // Application window defaults
    public static final int APP_WIDTH = 1200;
    public static final int APP_HEIGHT = 800;
}
