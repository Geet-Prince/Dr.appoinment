package me.geetprince.Dr.appoinment.controller;

import jakarta.validation.Valid;
import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityRequest;
import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityResponse;
import me.geetprince.Dr.appoinment.dto.TimeSlotResponse;
import me.geetprince.Dr.appoinment.service.DoctorAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/availability")
    public ResponseEntity<DoctorAvailabilityResponse> addAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailabilityResponse response = availabilityService.addAvailability(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<DoctorAvailabilityResponse>> getDoctorAvailability(
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(availabilityService.getDoctorAvailability(doctorId));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailableSlotsForDate(doctorId, date));
    }

    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<DoctorAvailabilityResponse> updateAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long availabilityId,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.updateAvailability(doctorId, availabilityId, request));
    }

    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long availabilityId) {
        availabilityService.deleteAvailability(doctorId, availabilityId);
        return ResponseEntity.noContent().build();
    }
}
