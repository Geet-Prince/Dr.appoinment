package me.geetprince.Dr.appoinment.controller;

import jakarta.validation.Valid;
import me.geetprince.Dr.appoinment.dto.PrescriptionRequest;
import me.geetprince.Dr.appoinment.dto.PrescriptionResponse;
import me.geetprince.Dr.appoinment.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request) {
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByAppointmentId(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByAppointmentId(appointmentId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByPatientId(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatientId(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByDoctorId(
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByDoctorId(doctorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, request));
    }
}
