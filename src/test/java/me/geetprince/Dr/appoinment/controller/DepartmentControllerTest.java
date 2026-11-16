package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geetprince.Dr.appoinment.dto.DepartmentRequest;
import me.geetprince.Dr.appoinment.dto.DepartmentResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
class DepartmentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private DepartmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(departmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new DepartmentResponse(1L, "Cardiology", "Heart care", LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/departments should return 201 Created on valid input")
    void createDepartment_Success() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Cardiology", "Heart care");
        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cardiology"));
    }

    @Test
    @DisplayName("POST /api/departments should return 409 Conflict when name already exists")
    void createDepartment_Duplicate_ReturnsConflict() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Cardiology", "Heart care");
        when(departmentService.createDepartment(any(DepartmentRequest.class)))
                .thenThrow(new DuplicateResourceException("Department with name 'Cardiology' already exists"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Department with name 'Cardiology' already exists"));
    }

    @Test
    @DisplayName("GET /api/departments should return 200 OK and list of departments")
    void getAllDepartments_ReturnsList() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cardiology"));
    }

    @Test
    @DisplayName("GET /api/departments/{id} should return 200 OK when found")
    void getDepartmentById_Success() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cardiology"));
    }

    @Test
    @DisplayName("GET /api/departments/{id} should return 404 Not Found when non-existent")
    void getDepartmentById_NotFound() throws Exception {
        when(departmentService.getDepartmentById(99L))
                .thenThrow(new ResourceNotFoundException("Department not found with ID: 99"));

        mockMvc.perform(get("/api/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Department not found with ID: 99"));
    }

    @Test
    @DisplayName("GET /api/departments/search should return matching departments")
    void searchDepartments_Success() throws Exception {
        when(departmentService.searchDepartments("cardio")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/departments/search").param("name", "cardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cardiology"));
    }

    @Test
    @DisplayName("PUT /api/departments/{id} should return 200 OK on update")
    void updateDepartment_Success() throws Exception {
        DepartmentRequest updateRequest = new DepartmentRequest("Cardiology Center", "Updated description");
        DepartmentResponse updatedResponse = new DepartmentResponse(1L, "Cardiology Center", "Updated description", LocalDateTime.now());
        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cardiology Center"));
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} should return 204 No Content")
    void deleteDepartment_Success() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNoContent());

        verify(departmentService).deleteDepartment(1L);
    }
}
