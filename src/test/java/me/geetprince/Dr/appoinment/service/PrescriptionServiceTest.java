package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.PrescriptionRequest;
import me.geetprince.Dr.appoinment.dto.PrescriptionResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.*;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import me.geetprince.Dr.appoinment.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Patient patient;
    private Doctor doctor;
    private Department department;
    private Appointment completedAppointment;
    private Prescription prescription;
    private PrescriptionRequest request;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFullName("Jane Doe");
        patient.setEmail("jane@example.com");

        department = new Department(1L, "Cardiology", "Heart care", LocalDateTime.now());

        doctor = new Doctor(1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"), "About", null,
                department, LocalDateTime.now(), LocalDateTime.now());

        completedAppointment = new Appointment(100L, patient, doctor, LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.COMPLETED,
                "Hypertension follow-up", new BigDecimal("1500.00"), "All good",
                LocalDateTime.now(), LocalDateTime.now());

        prescription = new Prescription(1L, completedAppointment,
                "Stage 1 Essential Hypertension",
                "Amlodipine 5mg once daily in morning for 30 days",
                "Reduce dietary sodium and monitor blood pressure weekly",
                LocalDateTime.now());

        request = new PrescriptionRequest(100L,
                "Stage 1 Essential Hypertension",
                "Amlodipine 5mg once daily in morning for 30 days",
                "Reduce dietary sodium and monitor blood pressure weekly");
    }

    @Test
    @DisplayName("Should successfully create a prescription for a completed appointment")
    void createPrescription_Success() {
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(completedAppointment));
        when(prescriptionRepository.existsByAppointmentId(100L)).thenReturn(false);
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        PrescriptionResponse response = prescriptionService.createPrescription(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDiagnosis()).isEqualTo("Stage 1 Essential Hypertension");
        assertThat(response.getPatientName()).isEqualTo("Jane Doe");
        assertThat(response.getDoctorName()).isEqualTo("Dr. John Watson");
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should throw InvalidAppointmentException when creating prescription for uncompleted appointment")
    void createPrescription_UncompletedAppointment() {
        completedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(completedAppointment));

        assertThatThrownBy(() -> prescriptionService.createPrescription(request))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("only be created for completed appointments");

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when prescription already exists for appointment")
    void createPrescription_AlreadyExists() {
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(completedAppointment));
        when(prescriptionRepository.existsByAppointmentId(100L)).thenReturn(true);

        assertThatThrownBy(() -> prescriptionService.createPrescription(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already been created");

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return prescription by ID when found")
    void getPrescriptionById_Success() {
        when(prescriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(prescription));

        PrescriptionResponse response = prescriptionService.getPrescriptionById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDiagnosis()).isEqualTo("Stage 1 Essential Hypertension");
    }

    @Test
    @DisplayName("Should return prescription by appointment ID when found")
    void getPrescriptionByAppointmentId_Success() {
        when(prescriptionRepository.findByAppointmentId(100L)).thenReturn(Optional.of(prescription));

        PrescriptionResponse response = prescriptionService.getPrescriptionByAppointmentId(100L);

        assertThat(response.getAppointmentId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should return list of prescriptions for a patient")
    void getPrescriptionsByPatientId_Success() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(prescriptionRepository.findPrescriptionsByPatientId(1L)).thenReturn(List.of(prescription));

        List<PrescriptionResponse> list = prescriptionService.getPrescriptionsByPatientId(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPatientName()).isEqualTo("Jane Doe");
    }

    @Test
    @DisplayName("Should return list of prescriptions issued by a doctor")
    void getPrescriptionsByDoctorId_Success() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(prescriptionRepository.findPrescriptionsByDoctorId(1L)).thenReturn(List.of(prescription));

        List<PrescriptionResponse> list = prescriptionService.getPrescriptionsByDoctorId(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getDoctorName()).isEqualTo("Dr. John Watson");
    }

    @Test
    @DisplayName("Should update prescription details successfully")
    void updatePrescription_Success() {
        PrescriptionRequest updateRequest = new PrescriptionRequest(100L,
                "Stage 2 Hypertension",
                "Amlodipine 10mg daily",
                "Low sodium diet");

        when(prescriptionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(i -> i.getArgument(0));

        PrescriptionResponse response = prescriptionService.updatePrescription(1L, updateRequest);

        assertThat(response.getDiagnosis()).isEqualTo("Stage 2 Hypertension");
        assertThat(response.getMedicines()).isEqualTo("Amlodipine 10mg daily");
        verify(prescriptionRepository).save(prescription);
    }
}
