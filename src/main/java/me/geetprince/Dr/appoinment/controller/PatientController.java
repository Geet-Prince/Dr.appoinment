
package me.geetprince.Dr.appoinment.controller;

import me.geetprince.Dr.appoinment.dto.PatientRequest;
import me.geetprince.Dr.appoinment.dto.PatientResponse;
import me.geetprince.Dr.appoinment.service.PatientService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // Register patient
    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(
            @Valid @RequestBody PatientRequest request) {

        PatientResponse response = patientService.registerPatient(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all patients
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {

        return ResponseEntity.ok(patientService.getAllPatients());
    }

    // Get patient by ID
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id) {

        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // Update patient
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {

        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    // Delete patient
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {

        patientService.deletePatient(id);

        return ResponseEntity.noContent().build();
    }
}
