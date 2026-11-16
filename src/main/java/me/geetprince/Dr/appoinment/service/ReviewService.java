package me.geetprince.Dr.appoinment.service;

import lombok.RequiredArgsConstructor;
import me.geetprince.Dr.appoinment.dto.*;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Appointment;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import me.geetprince.Dr.appoinment.models.Doctor;
import me.geetprince.Dr.appoinment.models.Patient;
import me.geetprince.Dr.appoinment.models.Review;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import me.geetprince.Dr.appoinment.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + request.getAppointmentId()));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Review can only be submitted for COMPLETED appointments. Current status: " + appointment.getStatus());
        }

        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateResourceException("A review has already been submitted for appointment ID: " + appointment.getId());
        }

        Review review = Review.builder()
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .appointment(appointment)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));
        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByAppointmentId(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Appointment not found with ID: " + appointmentId);
        }
        Review review = reviewRepository.findByAppointmentIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No review found for appointment ID: " + appointmentId));
        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByDoctorId(Long doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return reviewRepository.findByDoctorIdWithDetails(doctorId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByPatientId(Long patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return reviewRepository.findByPatientIdWithDetails(patientId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DoctorRatingSummaryResponse getDoctorRatingSummary(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        Double rawAverage = reviewRepository.findAverageRatingByDoctorId(doctorId);
        long totalCount = reviewRepository.countByDoctorId(doctorId);

        Double averageRating = (rawAverage != null)
                ? BigDecimal.valueOf(rawAverage).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return DoctorRatingSummaryResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .doctorSpecialization(doctor.getSpecialization())
                .averageRating(averageRating)
                .totalReviews(totalCount)
                .build();
    }

    @Transactional
    public ReviewResponse updateReview(Long id, ReviewUpdateRequest request) {
        Review review = reviewRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        return mapToResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with ID: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .appointmentId(review.getAppointment().getId())
                .patientId(review.getPatient().getId())
                .patientName(review.getPatient().getFullName())
                .doctorId(review.getDoctor().getId())
                .doctorName(review.getDoctor().getFullName())
                .doctorSpecialization(review.getDoctor().getSpecialization())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
