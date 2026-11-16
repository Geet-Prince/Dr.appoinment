package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityRequest;
import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityResponse;
import me.geetprince.Dr.appoinment.dto.TimeSlotResponse;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.service.DoctorAvailabilityService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private DoctorAvailabilityService availabilityService;

    @InjectMocks
    private DoctorAvailabilityController availabilityController;

    private DoctorAvailabilityResponse sampleResponse;
    private DoctorAvailabilityRequest sampleRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(availabilityController)
                .setMessageConverters(converter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new DoctorAvailabilityResponse(10L, 1L, "Dr. John Watson",
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true);

        sampleRequest = new DoctorAvailabilityRequest(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true);
    }

    @Test
    @DisplayName("POST /api/doctors/{id}/availability should return 201 Created on valid request")
    void addAvailability_Success() throws Exception {
        when(availabilityService.addAvailability(eq(1L), any(DoctorAvailabilityRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/doctors/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }

    @Test
    @DisplayName("GET /api/doctors/{id}/availability should return 200 OK and list of schedules")
    void getDoctorAvailability_Success() throws Exception {
        when(availabilityService.getDoctorAvailability(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/doctors/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. John Watson"));
    }

    @Test
    @DisplayName("GET /api/doctors/{id}/slots should return 200 OK and calculated time slots")
    void getAvailableSlots_Success() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 23); // A Monday
        TimeSlotResponse slot = new TimeSlotResponse(LocalTime.of(9, 0), LocalTime.of(9, 30), true);
        when(availabilityService.getAvailableSlotsForDate(1L, date)).thenReturn(List.of(slot));

        mockMvc.perform(get("/api/doctors/1/slots").param("date", "2026-11-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].isAvailable").value(true));
    }

    @Test
    @DisplayName("PUT /api/doctors/{id}/availability/{id} should return 200 OK on update")
    void updateAvailability_Success() throws Exception {
        when(availabilityService.updateAvailability(eq(1L), eq(10L), any(DoctorAvailabilityRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/doctors/1/availability/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("DELETE /api/doctors/{id}/availability/{id} should return 204 No Content")
    void deleteAvailability_Success() throws Exception {
        doNothing().when(availabilityService).deleteAvailability(1L, 10L);

        mockMvc.perform(delete("/api/doctors/1/availability/10"))
                .andExpect(status().isNoContent());

        verify(availabilityService).deleteAvailability(1L, 10L);
    }

    @Test
    @DisplayName("GET /api/doctors/{id}/availability should return 404 Not Found when doctor does not exist")
    void getDoctorAvailability_DoctorNotFound() throws Exception {
        when(availabilityService.getDoctorAvailability(99L))
                .thenThrow(new ResourceNotFoundException("Doctor not found with ID: 99"));

        mockMvc.perform(get("/api/doctors/99/availability"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
