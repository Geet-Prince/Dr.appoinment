
package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.PatientRequest;
import me.geetprince.Dr.appoinment.dto.PatientResponse;
import me.geetprince.Dr.appoinment.models.Patient;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // Convert Patient entity into PatientResponse
    private PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getAge(),
                patient.getGender()
        );
    }

    // Register patient
    public PatientResponse registerPatient(PatientRequest request) {

        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        Patient patient = new Patient();

        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());

        Patient savedPatient = patientRepository.save(patient);

        return mapToResponse(savedPatient);
    }

    // Get all patients
    public List<PatientResponse> getAllPatients() {

        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get patient by ID
    public PatientResponse getPatientById(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with ID: " + id
                        ));

        return mapToResponse(patient);
    }

    // Update patient
    public PatientResponse updatePatient(Long id, PatientRequest request) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with ID: " + id
                        ));

        // Check whether another patient already uses this email
        if (!patient.getEmail().equals(request.getEmail())
                && patientRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());

        Patient updatedPatient = patientRepository.save(patient);

        return mapToResponse(updatedPatient);
    }

    // Delete patient
    public void deletePatient(Long id) {

        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Patient not found with ID: " + id
            );
        }

        patientRepository.deleteById(id);
    }
}
