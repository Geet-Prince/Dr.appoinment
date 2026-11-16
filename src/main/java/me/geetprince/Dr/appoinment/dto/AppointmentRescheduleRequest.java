package me.geetprince.Dr.appoinment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRescheduleRequest {

    @NotNull(message = "New appointment date is required")
    @FutureOrPresent(message = "New appointment date cannot be in the past")
    private LocalDate appointmentDate;

    @NotNull(message = "New start time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public AppointmentRescheduleRequest() {
    }

    public AppointmentRescheduleRequest(LocalDate appointmentDate, LocalTime startTime, String reason) {
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.reason = reason;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
