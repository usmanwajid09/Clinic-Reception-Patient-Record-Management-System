package com.clinic.di;

import com.clinic.repository.*;
import com.clinic.repository.inmemory.*;
import com.clinic.service.*;

/**
 * Dependency Injection & Service Locator Container.
 * Adheres to Dependency Inversion Principle (DIP): decouples GUI panels
 * from concrete repository and service instantiations.
 */
public class ServiceContainer {
    private static ServiceContainer instance;

    // Repositories
    private final IPatientRepository patientRepository;
    private final IDoctorRepository doctorRepository;
    private final IAppointmentRepository appointmentRepository;
    private final IConsultationRepository consultationRepository;
    private final IBillRepository billRepository;

    // Services
    private final IAuthService authService;
    private final IPatientService patientService;
    private final IDoctorService doctorService;
    private final IAppointmentService appointmentService;
    private final IBillingService billingService;
    private final IConsultationService consultationService;
    private final IReportService reportService;

    private ServiceContainer() {
        // Instantiate Repositories
        this.patientRepository = new InMemoryPatientRepository();
        this.doctorRepository = new InMemoryDoctorRepository();
        this.appointmentRepository = new InMemoryAppointmentRepository();
        this.consultationRepository = new InMemoryConsultationRepository();
        this.billRepository = new InMemoryBillRepository();

        // Instantiate Services with injected repositories
        this.authService = new AuthService();
        this.billingService = new BillingService(billRepository, doctorRepository);
        this.consultationService = new ConsultationService(consultationRepository, appointmentRepository, patientRepository, billingService);
        this.patientService = new PatientService(patientRepository, consultationRepository);
        this.doctorService = new DoctorService(doctorRepository);
        this.appointmentService = new AppointmentService(appointmentRepository, doctorRepository, patientRepository);
        this.reportService = new ReportService(appointmentRepository, doctorRepository, patientRepository, consultationRepository, billRepository);

        // Seed realistic data for instant clinic operation
        DataSeeder seeder = new DataSeeder(patientRepository, doctorRepository, appointmentRepository, consultationRepository, billRepository);
        seeder.seed();
    }

    public static synchronized ServiceContainer getInstance() {
        if (instance == null) {
            instance = new ServiceContainer();
        }
        return instance;
    }

    // Service Accessors
    public IAuthService getAuthService() { return authService; }
    public IPatientService getPatientService() { return patientService; }
    public IDoctorService getDoctorService() { return doctorService; }
    public IAppointmentService getAppointmentService() { return appointmentService; }
    public IConsultationService getConsultationService() { return consultationService; }
    public IBillingService getBillingService() { return billingService; }
    public IReportService getReportService() { return reportService; }

    // Repository Accessors
    public IPatientRepository getPatientRepository() { return patientRepository; }
    public IDoctorRepository getDoctorRepository() { return doctorRepository; }
    public IAppointmentRepository getAppointmentRepository() { return appointmentRepository; }
    public IConsultationRepository getConsultationRepository() { return consultationRepository; }
    public IBillRepository getBillRepository() { return billRepository; }
}

