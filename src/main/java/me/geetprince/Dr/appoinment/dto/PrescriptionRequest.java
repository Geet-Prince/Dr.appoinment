package me.geetprince.Dr.appoinment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrescriptionRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 2000, message = "Diagnosis cannot exceed 2000 characters")
    private String diagnosis;

    @NotBlank(message = "Medicines prescription is required")
    @Size(max = 5000, message = "Medicines text cannot exceed 5000 characters")
    private String medicines;

    @Size(max = 2000, message = "Instructions cannot exceed 2000 characters")
    private String instructions;

    public PrescriptionRequest() {
    }

    public PrescriptionRequest(Long appointmentId, String diagnosis, String medicines, String instructions) {
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.medicines = medicines;
        this.instructions = instructions;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMedicines() {
        return medicines;
    }

    public void setMedicines(String medicines) {
        this.medicines = medicines;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
