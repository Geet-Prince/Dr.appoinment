package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.AppointmentBookingRequest;
import me.geetprince.Dr.appoinment.dto.AppointmentRescheduleRequest;
import me.geetprince.Dr.appoinment.dto.AppointmentResponse;
import me.geetprince.Dr.appoinment.dto.AppointmentStatusUpdateRequest;
import me.geetprince.Dr.appoinment.exception.AppointmentConflictException;
import me.geetprince.Dr.appoinment.exception.DoctorUnavailableException;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.*;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorAvailabilityRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Department department;
    private DoctorAvailability availability;
    private Appointment appointment;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFullName("Jane Doe");
        patient.setEmail("jane@example.com");
        patient.setPhoneNumber("9876543210");
        patient.setAge(30);
        patient.setGender("Female");

        department = new Department(1L, "Cardiology", "Heart care", LocalDateTime.now());

        doctor = new Doctor(1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"), "About", null,
                department, LocalDateTime.now(), LocalDateTime.now());

        futureDate = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);

        availability = new DoctorAvailability(10L, doctor, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(13, 0), 30, true);

        appointment = new Appointment(100L, patient, doctor, futureDate,
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.CONFIRMED,
                "Chest pain consultation", new BigDecimal("1500.00"), null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully book an appointment when doctor and patient have no conflicts")
    void bookAppointment_Success() {
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 1L, futureDate, LocalTime.of(10, 0), "Chest pain consultation", null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(appointmentRepository.hasDoctorConflict(eq(1L), eq(futureDate), eq(LocalTime.of(10, 0)), eq(LocalTime.of(10, 30)), isNull()))
                .thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), eq(futureDate), eq(LocalTime.of(10, 0)), eq(LocalTime.of(10, 30)), isNull()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponse response = appointmentService.bookAppointment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getPatientName()).isEqualTo("Jane Doe");
        assertThat(response.getDoctorName()).isEqualTo("Dr. John Watson");
        assertThat(response.getConsultationFee()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw AppointmentConflictException when doctor has an overlapping appointment")
    void bookAppointment_DoctorConflict() {
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 1L, futureDate, LocalTime.of(10, 0), "Chest pain consultation", null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(appointmentRepository.hasDoctorConflict(eq(1L), eq(futureDate), eq(LocalTime.of(10, 0)), eq(LocalTime.of(10, 30)), isNull()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("Doctor already has an appointment scheduled");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AppointmentConflictException when patient has an overlapping appointment")
    void bookAppointment_PatientConflict() {
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 1L, futureDate, LocalTime.of(10, 0), "Chest pain consultation", null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(appointmentRepository.hasDoctorConflict(eq(1L), eq(futureDate), eq(LocalTime.of(10, 0)), eq(LocalTime.of(10, 30)), isNull()))
                .thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), eq(futureDate), eq(LocalTime.of(10, 0)), eq(LocalTime.of(10, 30)), isNull()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("Patient already has an active appointment");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DoctorUnavailableException when doctor is not working on that day")
    void bookAppointment_DoctorUnavailableDay() {
        LocalDate sunday = LocalDate.now().plusWeeks(1).with(DayOfWeek.SUNDAY);
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 1L, sunday, LocalTime.of(10, 0), "Sunday consult", null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.SUNDAY))
                .thenReturn(List.of());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(DoctorUnavailableException.class)
                .hasMessageContaining("Doctor is not scheduled to work");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidAppointmentException when booking date is in the past")
    void bookAppointment_PastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 1L, pastDate, LocalTime.of(10, 0), "Past appointment", null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("cannot be in the past");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return appointment by ID when found")
    void getAppointmentById_Success() {
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = appointmentService.getAppointmentById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should return paginated appointments by patient")
    void getAppointmentsByPatient_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(appointmentRepository.findByPatientId(1L, pageable)).thenReturn(new PageImpl<>(List.of(appointment)));

        Page<AppointmentResponse> page = appointmentService.getAppointmentsByPatient(1L, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPatientName()).isEqualTo("Jane Doe");
    }

    @Test
    @DisplayName("Should successfully cancel an active appointment")
    void cancelAppointment_Success() {
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse response = appointmentService.cancelAppointment(100L, "Patient feeling better");

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Should throw InvalidAppointmentException when cancelling an already completed appointment")
    void cancelAppointment_AlreadyCompleted() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(100L, "Reason"))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("already been completed");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully reschedule an appointment")
    void rescheduleAppointment_Success() {
        LocalDate newDate = futureDate.plusWeeks(1);
        AppointmentRescheduleRequest rescheduleRequest = new AppointmentRescheduleRequest(
                newDate, LocalTime.of(11, 0), "Need later slot");

        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(appointment));
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability));
        when(appointmentRepository.hasDoctorConflict(eq(1L), eq(newDate), eq(LocalTime.of(11, 0)), eq(LocalTime.of(11, 30)), eq(100L)))
                .thenReturn(false);
        when(appointmentRepository.hasPatientConflict(eq(1L), eq(newDate), eq(LocalTime.of(11, 0)), eq(LocalTime.of(11, 30)), eq(100L)))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse response = appointmentService.rescheduleAppointment(100L, rescheduleRequest);

        assertThat(response.getAppointmentDate()).isEqualTo(newDate);
        assertThat(response.getStartTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(response.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Should successfully update appointment status to COMPLETED")
    void updateStatus_Success() {
        AppointmentStatusUpdateRequest updateRequest = new AppointmentStatusUpdateRequest(
                AppointmentStatus.COMPLETED, "Consultation finished normally");

        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse response = appointmentService.updateStatus(100L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(response.getNotes()).isEqualTo("Consultation finished normally");
        verify(appointmentRepository).save(appointment);
    }
}
