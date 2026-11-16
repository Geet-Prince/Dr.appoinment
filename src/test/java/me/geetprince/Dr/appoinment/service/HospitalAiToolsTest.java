package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.*;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalAiToolsTest {

    @Mock
    private DoctorService doctorService;

    @Mock
    private DoctorAvailabilityService doctorAvailabilityService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private HospitalAiTools hospitalAiTools;

    private DoctorResponse testDoctorResponse;
    private AppointmentResponse testAppointmentResponse;

    @BeforeEach
    void setUp() {
        testDoctorResponse = new DoctorResponse(
                1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD", 12, "MED-LIC-1001",
                BigDecimal.valueOf(1500.00), "Cardiologist specialist", null,
                1L, "Cardiology", null, null);

        testAppointmentResponse = new AppointmentResponse(
                10L, 1L, "Jane Doe", "jane@example.com", "9876543210",
                1L, "Dr. John Watson", "Cardiologist", "Cardiology",
                LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "Chest pain evaluation", BigDecimal.valueOf(1500.00),
                null, null, null);
    }

    @Test
    void searchDoctors_Success() {
        when(doctorService.searchDoctors(eq("Watson"), eq("Cardiologist"), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(testDoctorResponse), PageRequest.of(0, 5), 1));

        List<DoctorResponse> result = hospitalAiTools.searchDoctors("Watson", "Cardiologist");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. John Watson", result.get(0).getFullName());
    }

    @Test
    void getDoctorProfile_Success() {
        when(doctorService.getDoctorById(1L)).thenReturn(testDoctorResponse);

        DoctorResponse result = hospitalAiTools.getDoctorProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cardiologist", result.getSpecialization());
    }

    @Test
    void checkDoctorAvailability_Success() {
        TimeSlotResponse slot = new TimeSlotResponse(LocalTime.of(10, 0), LocalTime.of(10, 30), true);
        LocalDate targetDate = LocalDate.now().plusDays(3);

        when(doctorAvailabilityService.getAvailableSlotsForDate(1L, targetDate))
                .thenReturn(List.of(slot));

        List<TimeSlotResponse> slots = hospitalAiTools.checkDoctorAvailability(1L, targetDate.toString());

        assertNotNull(slots);
        assertEquals(1, slots.size());
        assertEquals(LocalTime.of(10, 0), slots.get(0).getStartTime());
    }

    @Test
    void bookAppointment_Success() {
        LocalDate date = LocalDate.now().plusDays(2);
        when(appointmentService.bookAppointment(any(AppointmentBookingRequest.class)))
                .thenReturn(testAppointmentResponse);

        AppointmentResponse result = hospitalAiTools.bookAppointment(
                1L, 1L, date.toString(), "10:00:00", "Chest pain evaluation");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        verify(appointmentService, times(1)).bookAppointment(any(AppointmentBookingRequest.class));
    }

    @Test
    void getPatientAppointments_Success() {
        when(appointmentService.getAppointmentsByPatient(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(testAppointmentResponse), PageRequest.of(0, 5), 1));

        List<AppointmentResponse> result = hospitalAiTools.getPatientAppointments(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }
}
