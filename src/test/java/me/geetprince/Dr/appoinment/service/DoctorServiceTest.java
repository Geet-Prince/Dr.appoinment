package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DoctorRequest;
import me.geetprince.Dr.appoinment.dto.DoctorResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Department;
import me.geetprince.Dr.appoinment.models.Doctor;
import me.geetprince.Dr.appoinment.repository.DepartmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Department department;
    private Doctor doctor;
    private DoctorRequest request;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "Cardiology", "Heart care", LocalDateTime.now());

        doctor = new Doctor(1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD Cardiology", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"),
                "Expert in interventional cardiology", "https://images.com/watson.jpg",
                department, LocalDateTime.now(), LocalDateTime.now());

        request = new DoctorRequest("Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD Cardiology", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"),
                "Expert in interventional cardiology", "https://images.com/watson.jpg",
                1L);
    }

    @Test
    @DisplayName("Should successfully register a doctor when email, license, and department are valid")
    void registerDoctor_Success() {
        when(doctorRepository.existsByEmail("dr.watson@hospital.com")).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("MED-LIC-1001")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorResponse response = doctorService.registerDoctor(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Dr. John Watson");
        assertThat(response.getDepartmentName()).isEqualTo("Cardiology");
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when doctor email already exists")
    void registerDoctor_DuplicateEmail() {
        when(doctorRepository.existsByEmail("dr.watson@hospital.com")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.registerDoctor(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when doctor license number already exists")
    void registerDoctor_DuplicateLicense() {
        when(doctorRepository.existsByEmail("dr.watson@hospital.com")).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("MED-LIC-1001")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.registerDoctor(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("license number");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when assigned department does not exist")
    void registerDoctor_DepartmentNotFound() {
        when(doctorRepository.existsByEmail("dr.watson@hospital.com")).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("MED-LIC-1001")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.registerDoctor(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with ID: 1");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    @DisplayName("Should return paginated doctors")
    void getAllDoctors_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(doctor)));

        Page<DoctorResponse> page = doctorService.getAllDoctors(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getFullName()).isEqualTo("Dr. John Watson");
    }

    @Test
    @DisplayName("Should return doctor by ID when exists")
    void getDoctorById_Success() {
        when(doctorRepository.findByIdWithDepartment(1L)).thenReturn(Optional.of(doctor));

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSpecialization()).isEqualTo("Cardiologist");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when doctor ID does not exist")
    void getDoctorById_NotFound() {
        when(doctorRepository.findByIdWithDepartment(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with ID: 99");
    }

    @Test
    @DisplayName("Should return doctors belonging to a department")
    void getDoctorsByDepartment_Success() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(doctorRepository.findByDepartmentId(1L)).thenReturn(List.of(doctor));

        List<DoctorResponse> list = doctorService.getDoctorsByDepartment(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getDepartmentName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("Should search doctors with dynamic criteria")
    void searchDoctors_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.searchDoctors("Watson", "Cardiologist", 1L, 10, new BigDecimal("2000.00"), pageable))
                .thenReturn(new PageImpl<>(List.of(doctor)));

        Page<DoctorResponse> result = doctorService.searchDoctors("Watson", "Cardiologist", 1L, 10, new BigDecimal("2000.00"), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLicenseNumber()).isEqualTo("MED-LIC-1001");
    }

    @Test
    @DisplayName("Should update doctor details successfully")
    void updateDoctor_Success() {
        DoctorRequest updateRequest = new DoctorRequest("Dr. John Watson Jr.", "dr.watson@hospital.com", "9876543210",
                "Male", "Senior Cardiologist", "MBBS, MD, FACC", 15,
                "MED-LIC-1001", new BigDecimal("1800.00"),
                "Chief of interventional cardiology", "https://images.com/watson.jpg",
                1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorResponse response = doctorService.updateDoctor(1L, updateRequest);

        assertThat(response.getFullName()).isEqualTo("Dr. John Watson Jr.");
        assertThat(response.getSpecialization()).isEqualTo("Senior Cardiologist");
        assertThat(response.getExperienceYears()).isEqualTo(15);
        verify(doctorRepository).save(doctor);
    }

    @Test
    @DisplayName("Should delete doctor when ID exists")
    void deleteDoctor_Success() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(doctorRepository).deleteById(1L);

        doctorService.deleteDoctor(1L);

        verify(doctorRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent doctor")
    void deleteDoctor_NotFound() {
        when(doctorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with ID: 99");

        verify(doctorRepository, never()).deleteById(anyLong());
    }
}
