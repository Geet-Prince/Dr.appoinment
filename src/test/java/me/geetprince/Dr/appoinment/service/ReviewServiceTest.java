package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DoctorRatingSummaryResponse;
import me.geetprince.Dr.appoinment.dto.ReviewRequest;
import me.geetprince.Dr.appoinment.dto.ReviewResponse;
import me.geetprince.Dr.appoinment.dto.ReviewUpdateRequest;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.*;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import me.geetprince.Dr.appoinment.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFullName("Jane Doe");
        testPatient.setEmail("jane.doe@example.com");
        testPatient.setPhoneNumber("9876543210");
        testPatient.setAge(30);
        testPatient.setGender("Female");

        Department department = new Department(1L, "Cardiology", "Heart care", LocalDateTime.now());

        testDoctor = new Doctor(1L, "Dr. John Watson", "dr.john@example.com", "9876543210",
                "Male", "Cardiologist", "MBBS, MD", 12,
                "MED-LIC-1001", BigDecimal.valueOf(1500.00), "About doctor", null,
                department, LocalDateTime.now(), LocalDateTime.now());

        testAppointment = new Appointment(10L, testPatient, testDoctor, LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.COMPLETED,
                "Cardiovascular checkup", BigDecimal.valueOf(1500.00), "All good",
                LocalDateTime.now(), LocalDateTime.now());

        testReview = Review.builder()
                .id(100L)
                .patient(testPatient)
                .doctor(testDoctor)
                .appointment(testAppointment)
                .rating(5)
                .comment("Excellent doctor, very attentive.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createReview_Success() {
        ReviewRequest request = ReviewRequest.builder()
                .appointmentId(10L)
                .rating(5)
                .comment("Excellent doctor, very attentive.")
                .build();

        when(appointmentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(testAppointment));
        when(reviewRepository.existsByAppointmentId(10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(5, response.getRating());
        assertEquals("Excellent doctor, very attentive.", response.getComment());
        assertEquals("Jane Doe", response.getPatientName());
        assertEquals("Dr. John Watson", response.getDoctorName());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_AppointmentNotFound_ThrowsResourceNotFoundException() {
        ReviewRequest request = ReviewRequest.builder()
                .appointmentId(99L)
                .rating(5)
                .build();

        when(appointmentRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_AppointmentNotCompleted_ThrowsInvalidAppointmentException() {
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        ReviewRequest request = ReviewRequest.builder()
                .appointmentId(10L)
                .rating(5)
                .build();

        when(appointmentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(testAppointment));

        InvalidAppointmentException ex = assertThrows(InvalidAppointmentException.class,
                () -> reviewService.createReview(request));
        assertTrue(ex.getMessage().contains("COMPLETED"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_DuplicateReview_ThrowsDuplicateResourceException() {
        ReviewRequest request = ReviewRequest.builder()
                .appointmentId(10L)
                .rating(5)
                .build();

        when(appointmentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(testAppointment));
        when(reviewRepository.existsByAppointmentId(10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> reviewService.createReview(request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewById_Success() {
        when(reviewRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(testReview));

        ReviewResponse response = reviewService.getReviewById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(5, response.getRating());
    }

    @Test
    void getReviewById_NotFound_ThrowsResourceNotFoundException() {
        when(reviewRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(999L));
    }

    @Test
    void getReviewByAppointmentId_Success() {
        when(appointmentRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByAppointmentIdWithDetails(10L)).thenReturn(Optional.of(testReview));

        ReviewResponse response = reviewService.getReviewByAppointmentId(10L);

        assertNotNull(response);
        assertEquals(10L, response.getAppointmentId());
    }

    @Test
    void getReviewsByDoctorId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> page = new PageImpl<>(List.of(testReview), pageable, 1);

        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByDoctorIdWithDetails(1L, pageable)).thenReturn(page);

        Page<ReviewResponse> response = reviewService.getReviewsByDoctorId(1L, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(5, response.getContent().get(0).getRating());
    }

    @Test
    void getReviewsByDoctorId_DoctorNotFound_ThrowsResourceNotFoundException() {
        when(doctorRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.getReviewsByDoctorId(99L, PageRequest.of(0, 10)));
    }

    @Test
    void getDoctorRatingSummary_WithReviews_Success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.findAverageRatingByDoctorId(1L)).thenReturn(4.6666);
        when(reviewRepository.countByDoctorId(1L)).thenReturn(3L);

        DoctorRatingSummaryResponse summary = reviewService.getDoctorRatingSummary(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getDoctorId());
        assertEquals("Dr. John Watson", summary.getDoctorName());
        assertEquals(4.67, summary.getAverageRating());
        assertEquals(3L, summary.getTotalReviews());
    }

    @Test
    void getDoctorRatingSummary_NoReviews_ReturnsZero() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(reviewRepository.findAverageRatingByDoctorId(1L)).thenReturn(null);
        when(reviewRepository.countByDoctorId(1L)).thenReturn(0L);

        DoctorRatingSummaryResponse summary = reviewService.getDoctorRatingSummary(1L);

        assertNotNull(summary);
        assertEquals(0.0, summary.getAverageRating());
        assertEquals(0L, summary.getTotalReviews());
    }

    @Test
    void updateReview_Success() {
        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
                .rating(4)
                .comment("Updated: Good consultation.")
                .build();

        when(reviewRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.updateReview(100L, updateRequest);

        assertNotNull(response);
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    void deleteReview_Success() {
        when(reviewRepository.existsById(100L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(100L);

        assertDoesNotThrow(() -> reviewService.deleteReview(100L));
        verify(reviewRepository, times(1)).deleteById(100L);
    }

    @Test
    void deleteReview_NotFound_ThrowsResourceNotFoundException() {
        when(reviewRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(999L));
        verify(reviewRepository, never()).deleteById(anyLong());
    }
}
