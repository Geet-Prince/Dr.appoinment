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

    private final PrescriptionRepository prescriptionRepository;
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Resetting existing records and seeding Indian hospital data...");

        // 1. Clean existing records in referential integrity order
        prescriptionRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        appointmentRepository.deleteAllInBatch();
        availabilityRepository.deleteAllInBatch();
        doctorRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 2. Seed Indian Departments
        Department generalMedicine = departmentRepository.save(new Department(null, "General Medicine", "Primary healthcare, viral fever, seasonal influenza, diabetes, and preventive medicine.", null));
        Department cardiology = departmentRepository.save(new Department(null, "Cardiology", "Heart disease, hypertension, ECG, chest pain evaluation, and cardiovascular care.", null));
        Department dermatology = departmentRepository.save(new Department(null, "Dermatology", "Skin, hair, nails, acne treatment, eczema, and allergy consultation.", null));
        Department pediatrics = departmentRepository.save(new Department(null, "Pediatrics", "Infant, child, and adolescent healthcare, growth monitoring, and immunization.", null));
        Department neurology = departmentRepository.save(new Department(null, "Neurology", "Brain and spinal care, migraines, vertigo, epilepsy, and neurological disorders.", null));
        Department orthopedics = departmentRepository.save(new Department(null, "Orthopedics", "Bone and joint health, arthritis, sports injuries, and fracture care.", null));
        Department gynecology = departmentRepository.save(new Department(null, "Gynecology & Obstetrics", "Women's wellness, maternity care, hormonal balance, and prenatal guidance.", null));
        Department ent = departmentRepository.save(new Department(null, "ENT", "Ear infections, nasal allergy, chronic sinusitis, throat pain, and hearing care.", null));

        // 3. Seed Indian Doctors
        Doctor doc1 = createDoctor(
                "Dr. Rajesh Sharma",
                "rajesh.sharma@hospital.in",
                "+91-98111-23456",
                "Male",
                "General Physician",
                "MBBS, MD (General Medicine) - AIIMS New Delhi",
                15,
                "MCI-2009-45123",
                new BigDecimal("500.00"),
                "Senior General Physician with over 15 years of experience managing viral fevers, seasonal infections, diabetes, and lifestyle diseases.",
                generalMedicine
        );

        Doctor doc2 = createDoctor(
                "Dr. Priya Nair",
                "priya.nair@hospital.in",
                "+91-98222-34567",
                "Female",
                "Cardiologist",
                "MBBS, MD, DM (Cardiology) - CMC Vellore",
                12,
                "MCI-2012-78901",
                new BigDecimal("1200.00"),
                "Expert Cardiologist specializing in preventive cardiology, coronary artery disease, hypertension, and heart rhythm management.",
                cardiology
        );

        Doctor doc3 = createDoctor(
                "Dr. Amit Verma",
                "amit.verma@hospital.in",
                "+91-98333-45678",
                "Male",
                "Dermatologist",
                "MBBS, MD (Dermatology) - PGIMER Chandigarh",
                10,
                "MCI-2014-34567",
                new BigDecimal("800.00"),
                "Renowned Dermatologist specializing in clinical dermatology, skin infections, hair loss treatment, and allergy therapies.",
                dermatology
        );

        Doctor doc4 = createDoctor(
                "Dr. Ananya Iyer",
                "ananya.iyer@hospital.in",
                "+91-98444-56789",
                "Female",
                "Pediatrician",
                "MBBS, MD (Pediatrics) - KEM Hospital Mumbai",
                14,
                "MCI-2010-89012",
                new BigDecimal("700.00"),
                "Caring pediatrician with deep expertise in neonatal health, childhood vaccinations, asthma, and developmental milestones.",
                pediatrics
        );

        Doctor doc5 = createDoctor(
                "Dr. Vikramaditya Rao",
                "vikram.rao@hospital.in",
                "+91-98555-67890",
                "Male",
                "Neurologist",
                "MBBS, MD, DM (Neurology) - NIMHANS Bengaluru",
                16,
                "MCI-2008-23456",
                new BigDecimal("1500.00"),
                "Distinguished Neurologist with extensive research in chronic migraines, stroke recovery, neuropathy, and vertigo management.",
                neurology
        );

        Doctor doc6 = createDoctor(
                "Dr. Suresh Kulkarni",
                "suresh.kulkarni@hospital.in",
                "+91-98666-78901",
                "Male",
                "Orthopedic Surgeon",
                "MBBS, MS (Orthopedics) - Grant Medical College Mumbai",
                18,
                "MCI-2006-12345",
                new BigDecimal("1000.00"),
                "Senior Orthopedic Surgeon focusing on knee and hip joint pain, arthritis management, ligament injuries, and spine care.",
                orthopedics
        );

        Doctor doc7 = createDoctor(
                "Dr. Sunita Deshmukh",
                "sunita.deshmukh@hospital.in",
                "+91-98777-89012",
                "Female",
                "Gynecologist",
                "MBBS, MS (Obstetrics & Gynecology) - JIPMER Puducherry",
                13,
                "MCI-2011-56789",
                new BigDecimal("900.00"),
                "Compassionate gynecologist dedicated to women's reproductive health, prenatal care, PCOS/PCOD management, and safe motherhood.",
                gynecology
        );

        Doctor doc8 = createDoctor(
                "Dr. Rohan Gupta",
                "rohan.gupta@hospital.in",
                "+91-98888-90123",
                "Male",
                "ENT Specialist",
                "MBBS, MS (ENT) - Maulana Azad Medical College Delhi",
                11,
                "MCI-2013-67890",
                new BigDecimal("600.00"),
                "ENT surgeon specializing in sinus ailments, chronic throat infections, tonsillitis, vertigo, and pediatric ear care.",
                ent
        );

        List<Doctor> doctors = List.of(doc1, doc2, doc3, doc4, doc5, doc6, doc7, doc8);

        // 4. Seed Weekly Availability (Monday to Saturday: 09:00 - 18:00, 30-min slots)
        List<DayOfWeek> workDays = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        );

        for (Doctor doc : doctors) {
            for (DayOfWeek day : workDays) {
                DoctorAvailability avail = new DoctorAvailability(
                        null,
                        doc,
                        day,
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0),
                        30,
                        true
                );
                availabilityRepository.save(avail);
            }
        }

        // 5. Seed Indian Patients
        Patient p1 = new Patient();
        p1.setFullName("Rahul Verma");
        p1.setEmail("rahul.verma@gmail.com");
        p1.setPhoneNumber("+91-99887-76655");
        p1.setAge(29);
        p1.setGender("Male");
        patientRepository.save(p1);

        Patient p2 = new Patient();
        p2.setFullName("Pooja Sharma");
        p2.setEmail("pooja.sharma@gmail.com");
        p2.setPhoneNumber("+91-98776-65544");
        p2.setAge(34);
        p2.setGender("Female");
        patientRepository.save(p2);

        Patient p3 = new Patient();
        p3.setFullName("Arjun Patel");
        p3.setEmail("arjun.patel@gmail.com");
        p3.setPhoneNumber("+91-97665-54433");
        p3.setAge(45);
        p3.setGender("Male");
        patientRepository.save(p3);

        // 6. Seed Application Users
        User admin = User.builder()
                .email("admin@hospital.in")
                .password(passwordEncoder.encode("Admin@123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        User doctorUser = User.builder()
                .email("rajesh.sharma@hospital.in")
                .password(passwordEncoder.encode("Doctor@123"))
                .role(UserRole.DOCTOR)
                .enabled(true)
                .build();
        userRepository.save(doctorUser);

        User patientUser = User.builder()
                .email("rahul.verma@gmail.com")
                .password(passwordEncoder.encode("Patient@123"))
                .role(UserRole.PATIENT)
                .enabled(true)
                .build();
        userRepository.save(patientUser);

        log.info("Successfully seeded 8 Indian departments, 8 doctors, 48 shifts, 3 patients, and system users.");
    }

    private Doctor createDoctor(String fullName, String email, String phoneNumber, String gender,
                                String specialization, String qualification, Integer experienceYears,
                                String licenseNumber, BigDecimal consultationFee, String about,
                                Department department) {
        Doctor doc = new Doctor();
        doc.setFullName(fullName);
        doc.setEmail(email);
        doc.setPhoneNumber(phoneNumber);
        doc.setGender(gender);
        doc.setSpecialization(specialization);
        doc.setQualification(qualification);
        doc.setExperienceYears(experienceYears);
        doc.setLicenseNumber(licenseNumber);
        doc.setConsultationFee(consultationFee);
        doc.setAbout(about);
        doc.setDepartment(department);
        return doctorRepository.save(doc);
    }
}
