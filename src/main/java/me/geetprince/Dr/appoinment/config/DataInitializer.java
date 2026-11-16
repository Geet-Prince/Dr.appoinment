package me.geetprince.Dr.appoinment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geetprince.Dr.appoinment.models.*;
import me.geetprince.Dr.appoinment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartmentsAndDoctors();
        seedPatients();
        seedUsers();
    }

    private void seedDepartmentsAndDoctors() {
        if (departmentRepository.count() > 0) {
            log.info("Database already seeded with departments and doctors. Skipping.");
            return;
        }

        log.info("Seeding initial hospital departments, doctors, and schedules...");

        // 1. Departments
        Department generalMedicine = departmentRepository.save(new Department(null, "General Medicine", "Primary care, internal medicine, fever, and routine health checks.", null));
        Department cardiology = departmentRepository.save(new Department(null, "Cardiology", "Cardiovascular care, heart disease, blood pressure, and ecg diagnosis.", null));
        Department dermatology = departmentRepository.save(new Department(null, "Dermatology", "Skin, hair, nails, acne, and rash treatments.", null));
        Department pediatrics = departmentRepository.save(new Department(null, "Pediatrics", "Infant, child, and adolescent healthcare.", null));
        Department neurology = departmentRepository.save(new Department(null, "Neurology", "Brain, spine, migraine, and nervous system disorders.", null));
        Department orthopedics = departmentRepository.save(new Department(null, "Orthopedics", "Bone, joint, fracture, and musculoskeletal medicine.", null));

        // 2. Doctors
        Doctor doc1 = new Doctor();
        doc1.setFullName("Dr. Sarah Jenkins");
        doc1.setEmail("sarah.jenkins@hospital.com");
        doc1.setPhoneNumber("+1-555-1011");
        doc1.setGender("Female");
        doc1.setSpecialization("General Physician");
        doc1.setQualification("MBBS, MD (Internal Medicine)");
        doc1.setExperienceYears(12);
        doc1.setLicenseNumber("MED-LIC-001");
        doc1.setConsultationFee(new BigDecimal("50.00"));
        doc1.setAbout("Experienced General Practitioner specializing in fever, viral infections, diabetes, and preventive medicine.");
        doc1.setDepartment(generalMedicine);
        doc1 = doctorRepository.save(doc1);

        Doctor doc2 = new Doctor();
        doc2.setFullName("Dr. Robert Chen");
        doc2.setEmail("robert.chen@hospital.com");
        doc2.setPhoneNumber("+1-555-1012");
        doc2.setGender("Male");
        doc2.setSpecialization("Cardiologist");
        doc2.setQualification("MD, DM (Cardiology), FACC");
        doc2.setExperienceYears(15);
        doc2.setLicenseNumber("MED-LIC-002");
        doc2.setConsultationFee(new BigDecimal("120.00"));
        doc2.setAbout("Board-certified cardiologist focused on coronary artery disease, hypertension, and preventive cardiology.");
        doc2.setDepartment(cardiology);
        doc2 = doctorRepository.save(doc2);

        Doctor doc3 = new Doctor();
        doc3.setFullName("Dr. Emily Davis");
        doc3.setEmail("emily.davis@hospital.com");
        doc3.setPhoneNumber("+1-555-1013");
        doc3.setGender("Female");
        doc3.setSpecialization("Dermatologist");
        doc3.setQualification("MD (Dermatology)");
        doc3.setExperienceYears(9);
        doc3.setLicenseNumber("MED-LIC-003");
        doc3.setConsultationFee(new BigDecimal("80.00"));
        doc3.setAbout("Specialist in clinical dermatology, skin infections, allergy management, and acne therapies.");
        doc3.setDepartment(dermatology);
        doc3 = doctorRepository.save(doc3);

        Doctor doc4 = new Doctor();
        doc4.setFullName("Dr. James Wilson");
        doc4.setEmail("james.wilson@hospital.com");
        doc4.setPhoneNumber("+1-555-1014");
        doc4.setGender("Male");
        doc4.setSpecialization("Pediatrician");
        doc4.setQualification("MD (Pediatrics), FAAP");
        doc4.setExperienceYears(14);
        doc4.setLicenseNumber("MED-LIC-004");
        doc4.setConsultationFee(new BigDecimal("70.00"));
        doc4.setAbout("Dedicated pediatrician caring for newborns, toddlers, and adolescents with vaccination and developmental care.");
        doc4.setDepartment(pediatrics);
        doc4 = doctorRepository.save(doc4);

        Doctor doc5 = new Doctor();
        doc5.setFullName("Dr. Lisa Patel");
        doc5.setEmail("lisa.patel@hospital.com");
        doc5.setPhoneNumber("+1-555-1015");
        doc5.setGender("Female");
        doc5.setSpecialization("Neurologist");
        doc5.setQualification("MD, DM (Neurology)");
        doc5.setExperienceYears(11);
        doc5.setLicenseNumber("MED-LIC-005");
        doc5.setConsultationFee(new BigDecimal("150.00"));
        doc5.setAbout("Expert in neurological disorders, severe headaches, migraines, stroke prevention, and neuropathy.");
        doc5.setDepartment(neurology);
        doc5 = doctorRepository.save(doc5);

        Doctor doc6 = new Doctor();
        doc6.setFullName("Dr. Michael Brown");
        doc6.setEmail("michael.brown@hospital.com");
        doc6.setPhoneNumber("+1-555-1016");
        doc6.setGender("Male");
        doc6.setSpecialization("Orthopedic Surgeon");
        doc6.setQualification("MS (Orthopedics)");
        doc6.setExperienceYears(16);
        doc6.setLicenseNumber("MED-LIC-006");
        doc6.setConsultationFee(new BigDecimal("110.00"));
        doc6.setAbout("Specialist in sports injuries, joint replacements, arthritis, and bone fracture treatments.");
        doc6.setDepartment(orthopedics);
        doc6 = doctorRepository.save(doc6);

        // 3. Weekly Availability for all doctors (Mon-Fri 09:00 - 17:00, 30 min slots)
        List<Doctor> doctors = List.of(doc1, doc2, doc3, doc4, doc5, doc6);
        List<DayOfWeek> workDays = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

        for (Doctor doc : doctors) {
            for (DayOfWeek day : workDays) {
                DoctorAvailability avail = new DoctorAvailability(
                        null,
                        doc,
                        day,
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0),
                        30,
                        true
                );
                availabilityRepository.save(avail);
            }
        }

        log.info("Successfully seeded 6 departments, 6 doctors, and 30 availability shifts.");
    }

    private void seedPatients() {
        if (patientRepository.count() > 0) {
            return;
        }

        Patient patient = new Patient();
        patient.setFullName("John Doe");
        patient.setEmail("john.doe@example.com");
        patient.setPhoneNumber("+1-555-0199");
        patient.setAge(32);
        patient.setGender("Male");
        patientRepository.save(patient);

        log.info("Successfully seeded demo patient: John Doe (ID: 1).");
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = User.builder()
                .email("admin@hospital.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.info("Successfully seeded admin user: admin@hospital.com.");
    }
}
