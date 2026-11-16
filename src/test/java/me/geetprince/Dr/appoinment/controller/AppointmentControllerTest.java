package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.geetprince.Dr.appoinment.dto.AppointmentBookingRequest;
import me.geetprince.Dr.appoinment.dto.AppointmentRescheduleRequest;
import me.geetprince.Dr.appoinment.dto.AppointmentResponse;
import me.geetprince.Dr.appoinment.dto.AppointmentStatusUpdateRequest;
import me.geetprince.Dr.appoinment.exception.AppointmentConflictException;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import me.geetprince.Dr.appoinment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentResponse sampleResponse;
    private AppointmentBookingRequest sampleBookingRequest;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(appointmentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        futureDate = LocalDate.now().plusWeeks(1);

        sampleResponse = new AppointmentResponse(100L, 1L, "Jane Doe", "jane@example.com", "9876543210",
                1L, "Dr. John Watson", "Cardiologist", "Cardiology",
                futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "Chest pain", new BigDecimal("1500.00"), null,
                LocalDateTime.now(), LocalDateTime.now());

        sampleBookingRequest = new AppointmentBookingRequest(
                1L, 1L, futureDate, LocalTime.of(10, 0), "Chest pain", null);
    }

    @Test
    @DisplayName("POST /api/appointments should return 201 Created on valid booking")
    void bookAppointment_Success() throws Exception {
        when(appointmentService.bookAppointment(any(AppointmentBookingRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.doctorName").value("Dr. John Watson"));
    }

    @Test
    @DisplayName("POST /api/appointments should return 409 Conflict when slot is double-booked")
    void bookAppointment_Conflict_Returns409() throws Exception {
        when(appointmentService.bookAppointment(any(AppointmentBookingRequest.class)))
                .thenThrow(new AppointmentConflictException("Doctor already has an appointment scheduled at this time"));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookingRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Appointment Conflict"));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} should return 200 OK when found")
    void getAppointmentById_Success() throws Exception {
        when(appointmentService.getAppointmentById(100L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/appointments/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.patientName").value("Jane Doe"));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} should return 404 Not Found when non-existent")
    void getAppointmentById_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(999L))
                .thenThrow(new ResourceNotFoundException("Appointment not found with ID: 999"));

        mockMvc.perform(get("/api/appointments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/appointments/patient/{patientId} should return paginated list")
    void getAppointmentsByPatient_Success() throws Exception {
        when(appointmentService.getAppointmentsByPatient(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/appointments/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].patientName").value("Jane Doe"));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/cancel should return 200 OK on successful cancellation")
    void cancelAppointment_Success() throws Exception {
        AppointmentResponse cancelled = new AppointmentResponse(100L, 1L, "Jane Doe", "jane@example.com", "9876543210",
                1L, "Dr. John Watson", "Cardiologist", "Cardiology",
                futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CANCELLED, "Chest pain", new BigDecimal("1500.00"), "[Cancellation Reason]: Personal emergency",
                LocalDateTime.now(), LocalDateTime.now());

        when(appointmentService.cancelAppointment(100L, "Personal emergency")).thenReturn(cancelled);

        mockMvc.perform(patch("/api/appointments/100/cancel")
                        .param("reason", "Personal emergency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/reschedule should return 200 OK on reschedule")
    void rescheduleAppointment_Success() throws Exception {
        AppointmentRescheduleRequest rescheduleRequest = new AppointmentRescheduleRequest(
                futureDate.plusDays(1), LocalTime.of(11, 0), "Reschedule to Tuesday");

        when(appointmentService.rescheduleAppointment(eq(100L), any(AppointmentRescheduleRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/appointments/100/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rescheduleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/status should return 200 OK on status update")
    void updateStatus_Success() throws Exception {
        AppointmentStatusUpdateRequest statusRequest = new AppointmentStatusUpdateRequest(
                AppointmentStatus.COMPLETED, "Completed successfully");

        when(appointmentService.updateStatus(eq(100L), any(AppointmentStatusUpdateRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/appointments/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }
}
