package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityRequest;
import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityResponse;
import me.geetprince.Dr.appoinment.dto.TimeSlotResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Department;
import me.geetprince.Dr.appoinment.models.Doctor;
import me.geetprince.Dr.appoinment.models.DoctorAvailability;
import me.geetprince.Dr.appoinment.repository.DoctorAvailabilityRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorAvailabilityService availabilityService;

    private Doctor doctor;
    private DoctorAvailability availability;
    private DoctorAvailabilityRequest request;

    @BeforeEach
    void setUp() {
        Department department = new Department(1L, "Cardiology", "Heart care", LocalDateTime.now());
        doctor = new Doctor(1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"), "About", null,
                department, LocalDateTime.now(), LocalDateTime.now());

        availability = new DoctorAvailability(10L, doctor, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true);

        request = new DoctorAvailabilityRequest(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true);
    }

    @Test
    @DisplayName("Should successfully add doctor availability schedule")
    void addAvailability_Success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.hasOverlappingShift(1L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), null))
                .thenReturn(false);
        when(availabilityRepository.save(any(DoctorAvailability.class))).thenReturn(availability);

        DoctorAvailabilityResponse response = availabilityService.addAvailability(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getDoctorName()).isEqualTo("Dr. John Watson");
        assertThat(response.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        verify(availabilityRepository).save(any(DoctorAvailability.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when doctor does not exist")
    void addAvailability_DoctorNotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.addAvailability(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with ID: 99");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when startTime is after or equal to endTime")
    void addAvailability_InvalidTimeRange() {
        DoctorAvailabilityRequest invalid = new DoctorAvailabilityRequest(DayOfWeek.MONDAY,
                LocalTime.of(15, 0), LocalTime.of(10, 0), 30, true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> availabilityService.addAvailability(1L, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be before end time");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when new shift overlaps with existing shift")
    void addAvailability_OverlappingShift() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.hasOverlappingShift(1L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), null))
                .thenReturn(true);

        assertThatThrownBy(() -> availabilityService.addAvailability(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("overlapping shift");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return list of availability schedules for doctor")
    void getDoctorAvailability_Success() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findByDoctorId(1L)).thenReturn(List.of(availability));

        List<DoctorAvailabilityResponse> list = availabilityService.getDoctorAvailability(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("Should dynamically generate time slots for a given date")
    void getAvailableSlotsForDate_Success() {
        // Assume next Monday date
        LocalDate nextMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.MONDAY))
                .thenReturn(List.of(availability)); // 9:00 to 12:00 in 30 min slots = 6 slots

        List<TimeSlotResponse> slots = availabilityService.getAvailableSlotsForDate(1L, nextMonday);

        assertThat(slots).hasSize(6);
        assertThat(slots.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(0).getEndTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(slots.get(5).getStartTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(slots.get(5).getEndTime()).isEqualTo(LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("Should return empty list when doctor has no active shifts on date")
    void getAvailableSlotsForDate_NoShifts() {
        LocalDate sunday = LocalDate.now().plusWeeks(1).with(DayOfWeek.SUNDAY);
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findByDoctorIdAndDayOfWeekAndIsAvailableTrue(1L, DayOfWeek.SUNDAY))
                .thenReturn(List.of());

        List<TimeSlotResponse> slots = availabilityService.getAvailableSlotsForDate(1L, sunday);

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Should successfully update an availability shift")
    void updateAvailability_Success() {
        DoctorAvailabilityRequest updateRequest = new DoctorAvailabilityRequest(DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(13, 0), 30, true);

        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findById(10L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.hasOverlappingShift(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0), 10L))
                .thenReturn(false);
        when(availabilityRepository.save(any(DoctorAvailability.class))).thenAnswer(i -> i.getArgument(0));

        DoctorAvailabilityResponse response = availabilityService.updateAvailability(1L, 10L, updateRequest);

        assertThat(response.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.getEndTime()).isEqualTo(LocalTime.of(13, 0));
        verify(availabilityRepository).save(availability);
    }

    @Test
    @DisplayName("Should successfully delete availability shift")
    void deleteAvailability_Success() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findById(10L)).thenReturn(Optional.of(availability));
        doNothing().when(availabilityRepository).delete(availability);

        availabilityService.deleteAvailability(1L, 10L);

        verify(availabilityRepository).delete(availability);
    }
}
