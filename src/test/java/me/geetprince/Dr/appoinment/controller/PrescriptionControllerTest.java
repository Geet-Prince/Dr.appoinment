package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.geetprince.Dr.appoinment.dto.PrescriptionRequest;
import me.geetprince.Dr.appoinment.dto.PrescriptionResponse;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionController prescriptionController;

    private PrescriptionResponse sampleResponse;
    private PrescriptionRequest sampleRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(prescriptionController)
                .setMessageConverters(converter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new PrescriptionResponse(1L, 100L, 1L, "Jane Doe",
                1L, "Dr. John Watson", "Cardiologist", "Cardiology",
                "Stage 1 Hypertension", "Amlodipine 5mg", "Low sodium diet",
                LocalDateTime.now());

        sampleRequest = new PrescriptionRequest(100L, "Stage 1 Hypertension",
                "Amlodipine 5mg", "Low sodium diet");
    }

    @Test
    @DisplayName("POST /api/prescriptions should return 201 Created on valid input")
    void createPrescription_Success() throws Exception {
        when(prescriptionService.createPrescription(any(PrescriptionRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.diagnosis").value("Stage 1 Hypertension"));
    }

    @Test
    @DisplayName("POST /api/prescriptions should return 400 Bad Request when appointment is not completed")
    void createPrescription_NotCompleted_Returns400() throws Exception {
        when(prescriptionService.createPrescription(any(PrescriptionRequest.class)))
                .thenThrow(new InvalidAppointmentException("Prescriptions can only be created for completed appointments"));

        mockMvc.perform(post("/api/prescriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Appointment"));
    }

    @Test
    @DisplayName("GET /api/prescriptions/{id} should return 200 OK when found")
    void getPrescriptionById_Success() throws Exception {
        when(prescriptionService.getPrescriptionById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/prescriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.doctorName").value("Dr. John Watson"));
    }

    @Test
    @DisplayName("GET /api/prescriptions/appointment/{appointmentId} should return 200 OK")
    void getPrescriptionByAppointmentId_Success() throws Exception {
        when(prescriptionService.getPrescriptionByAppointmentId(100L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/prescriptions/appointment/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(100));
    }

    @Test
    @DisplayName("GET /api/prescriptions/patient/{patientId} should return 200 OK and list")
    void getPrescriptionsByPatientId_Success() throws Exception {
        when(prescriptionService.getPrescriptionsByPatientId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/prescriptions/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientName").value("Jane Doe"));
    }

    @Test
    @DisplayName("GET /api/prescriptions/doctor/{doctorId} should return 200 OK and list")
    void getPrescriptionsByDoctorId_Success() throws Exception {
        when(prescriptionService.getPrescriptionsByDoctorId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/prescriptions/doctor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/prescriptions/{id} should return 200 OK on update")
    void updatePrescription_Success() throws Exception {
        when(prescriptionService.updatePrescription(eq(1L), any(PrescriptionRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/prescriptions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
