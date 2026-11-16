package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.models.Appointment;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import me.geetprince.Dr.appoinment.models.Doctor;
import me.geetprince.Dr.appoinment.models.Patient;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentReminderService reminderService;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFullName("Jane Doe");
        patient.setEmail("jane@example.com");

        Doctor doctor = new Doctor(1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD", 12, "MED-LIC-1001",
                BigDecimal.valueOf(1500.00), "Cardiologist specialist", null,
                null, LocalDateTime.now(), LocalDateTime.now());

        testAppointment = new Appointment(100L, patient, doctor, LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.CONFIRMED,
                "Cardiovascular checkup", BigDecimal.valueOf(1500.00), null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void sendDailyReminders_WithUpcomingAppointments_DispatchesReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(appointmentRepository.findByAppointmentDateAndStatusWithDetails(eq(tomorrow), eq(AppointmentStatus.CONFIRMED)))
                .thenReturn(List.of(testAppointment));

        int remindersSent = reminderService.sendDailyReminders();

        assertEquals(1, remindersSent);
        verify(appointmentRepository, times(1))
                .findByAppointmentDateAndStatusWithDetails(eq(tomorrow), eq(AppointmentStatus.CONFIRMED));
    }

    @Test
    void sendDailyReminders_NoAppointments_DispatchesZero() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(appointmentRepository.findByAppointmentDateAndStatusWithDetails(eq(tomorrow), eq(AppointmentStatus.CONFIRMED)))
                .thenReturn(Collections.emptyList());

        int remindersSent = reminderService.sendDailyReminders();

        assertEquals(0, remindersSent);
        verify(appointmentRepository, times(1))
                .findByAppointmentDateAndStatusWithDetails(eq(tomorrow), eq(AppointmentStatus.CONFIRMED));
    }
}
