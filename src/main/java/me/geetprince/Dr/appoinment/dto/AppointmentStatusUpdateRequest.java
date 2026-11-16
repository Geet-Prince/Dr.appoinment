package me.geetprince.Dr.appoinment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;

public class AppointmentStatusUpdateRequest {

    @NotNull(message = "Appointment status is required")
    private AppointmentStatus status;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    public AppointmentStatusUpdateRequest() {
    }

    public AppointmentStatusUpdateRequest(AppointmentStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
