package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geetprince.Dr.appoinment.dto.DoctorRequest;
import me.geetprince.Dr.appoinment.dto.DoctorResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.service.DoctorService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private DoctorResponse sampleResponse;
    private DoctorRequest sampleRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(doctorController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new DoctorResponse(1L, "Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD Cardiology", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"),
                "Cardiology expert", "https://images.com/watson.jpg",
                1L, "Cardiology", LocalDateTime.now(), LocalDateTime.now());

        sampleRequest = new DoctorRequest("Dr. John Watson", "dr.watson@hospital.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD Cardiology", 12,
                "MED-LIC-1001", new BigDecimal("1500.00"),
                "Cardiology expert", "https://images.com/watson.jpg",
                1L);
    }

    @Test
    @DisplayName("POST /api/doctors should return 201 Created on valid request")
    void registerDoctor_Success() throws Exception {
        when(doctorService.registerDoctor(any(DoctorRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Dr. John Watson"))
                .andExpect(jsonPath("$.licenseNumber").value("MED-LIC-1001"));
    }

    @Test
    @DisplayName("POST /api/doctors should return 409 Conflict when license number or email is duplicate")
    void registerDoctor_Duplicate_ReturnsConflict() throws Exception {
        when(doctorService.registerDoctor(any(DoctorRequest.class)))
                .thenThrow(new DuplicateResourceException("Doctor with license number already exists"));

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("GET /api/doctors should return 200 OK and page of doctors")
    void getAllDoctors_ReturnsPage() throws Exception {
        when(doctorService.getAllDoctors(any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Dr. John Watson"));
    }

    @Test
    @DisplayName("GET /api/doctors/{id} should return 200 OK when found")
    void getDoctorById_Success() throws Exception {
        when(doctorService.getDoctorById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Dr. John Watson"));
    }

    @Test
    @DisplayName("GET /api/doctors/{id} should return 404 Not Found when non-existent")
    void getDoctorById_NotFound() throws Exception {
        when(doctorService.getDoctorById(99L))
                .thenThrow(new ResourceNotFoundException("Doctor not found with ID: 99"));

        mockMvc.perform(get("/api/doctors/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/doctors/department/{id} should return list of department doctors")
    void getDoctorsByDepartment_Success() throws Exception {
        when(doctorService.getDoctorsByDepartment(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/doctors/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].departmentName").value("Cardiology"));
    }

    @Test
    @DisplayName("GET /api/doctors/search should return matching doctors page")
    void searchDoctors_Success() throws Exception {
        when(doctorService.searchDoctors(eq("Watson"), eq("Cardiologist"), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/doctors/search")
                        .param("name", "Watson")
                        .param("specialization", "Cardiologist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Dr. John Watson"));
    }

    @Test
    @DisplayName("PUT /api/doctors/{id} should return 200 OK on update")
    void updateDoctor_Success() throws Exception {
        when(doctorService.updateDoctor(eq(1L), any(DoctorRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/doctors/{id} should return 204 No Content")
    void deleteDoctor_Success() throws Exception {
        doNothing().when(doctorService).deleteDoctor(1L);

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNoContent());

        verify(doctorService).deleteDoctor(1L);
    }
}
