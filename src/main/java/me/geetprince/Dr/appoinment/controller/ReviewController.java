package me.geetprince.Dr.appoinment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.geetprince.Dr.appoinment.dto.DoctorRatingSummaryResponse;
import me.geetprince.Dr.appoinment.dto.ReviewRequest;
import me.geetprince.Dr.appoinment.dto.ReviewResponse;
import me.geetprince.Dr.appoinment.dto.ReviewUpdateRequest;
import me.geetprince.Dr.appoinment.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        ReviewResponse response = reviewService.getReviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ReviewResponse> getReviewByAppointmentId(@PathVariable Long appointmentId) {
        ReviewResponse response = reviewService.getReviewByAppointmentId(appointmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByDoctorId(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getReviewsByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}/summary")
    public ResponseEntity<DoctorRatingSummaryResponse> getDoctorRatingSummary(@PathVariable Long doctorId) {
        DoctorRatingSummaryResponse response = reviewService.getDoctorRatingSummary(doctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByPatientId(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getReviewsByPatientId(patientId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewResponse response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
