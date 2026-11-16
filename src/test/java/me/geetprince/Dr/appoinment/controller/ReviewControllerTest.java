package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.geetprince.Dr.appoinment.dto.DoctorRatingSummaryResponse;
import me.geetprince.Dr.appoinment.dto.ReviewRequest;
import me.geetprince.Dr.appoinment.dto.ReviewResponse;
import me.geetprince.Dr.appoinment.dto.ReviewUpdateRequest;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper;
    private ReviewResponse testResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        testResponse = ReviewResponse.builder()
                .id(1L)
                .appointmentId(10L)
                .patientId(1L)
                .patientName("Jane Doe")
                .doctorId(1L)
                .doctorName("Dr. John Watson")
                .doctorSpecialization("Cardiologist")
                .rating(5)
                .comment("Compassionate and skilled doctor.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createReview_Success_Returns201() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .appointmentId(10L)
                .rating(5)
                .comment("Compassionate and skilled doctor.")
                .build();

        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.doctorName").value("Dr. John Watson"));
    }

    @Test
    void createReview_InvalidRating_Returns400() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
                .appointmentId(10L)
                .rating(6) // Exceeds max 5
                .comment("Invalid rating test")
                .build();

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void getReviewById_Success_Returns200() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.patientName").value("Jane Doe"));
    }

    @Test
    void getReviewByAppointmentId_Success_Returns200() throws Exception {
        when(reviewService.getReviewByAppointmentId(10L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/reviews/appointment/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(10L));
    }

    @Test
    void getReviewsByDoctorId_Success_Returns200() throws Exception {
        when(reviewService.getReviewsByDoctorId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(testResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/reviews/doctor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rating").value(5));
    }

    @Test
    void getDoctorRatingSummary_Success_Returns200() throws Exception {
        DoctorRatingSummaryResponse summary = DoctorRatingSummaryResponse.builder()
                .doctorId(1L)
                .doctorName("Dr. John Watson")
                .doctorSpecialization("Cardiologist")
                .averageRating(4.85)
                .totalReviews(12L)
                .build();

        when(reviewService.getDoctorRatingSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/reviews/doctor/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(1L))
                .andExpect(jsonPath("$.averageRating").value(4.85))
                .andExpect(jsonPath("$.totalReviews").value(12L));
    }

    @Test
    void updateReview_Success_Returns200() throws Exception {
        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
                .rating(4)
                .comment("Updated comment")
                .build();

        when(reviewService.updateReview(eq(1L), any(ReviewUpdateRequest.class))).thenReturn(testResponse);

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteReview_Success_Returns204() throws Exception {
        doNothing().when(reviewService).deleteReview(1L);

        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(1L);
    }
}
