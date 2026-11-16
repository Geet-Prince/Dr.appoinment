package me.geetprince.Dr.appoinment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geetprince.Dr.appoinment.dto.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HospitalAiTools {

    private final DoctorService doctorService;
    private final DoctorAvailabilityService doctorAvailabilityService;
    private final AppointmentService appointmentService;

    @Tool(description = "Search for hospital doctors by name or specialization (e.g. Cardiologist, Dermatologist, Neurologist, Pediatrician). Returns doctors with their ID, name, specialization, and consultation fee.")
    public List<DoctorResponse> searchDoctors(
            @ToolParam(description = "Doctor name or general query (optional, can be empty)") String query,
            @ToolParam(description = "Medical specialization, e.g. Cardiologist (optional, can be empty)") String specialization) {

        log.info("AI Tool executed: searchDoctors(query='{}', specialization='{}')", query, specialization);
        String q = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String s = (specialization != null && !specialization.trim().isEmpty()) ? specialization.trim() : null;

        Page<DoctorResponse> page = doctorService.searchDoctors(q, s, null, null, null, PageRequest.of(0, 5));
        return page.getContent();
    }

    @Tool(description = "Get detailed profile information for a specific doctor by their ID.")
    public DoctorResponse getDoctorProfile(
            @ToolParam(description = "The unique numeric ID of the doctor") Long doctorId) {

        log.info("AI Tool executed: getDoctorProfile(doctorId={})", doctorId);
        return doctorService.getDoctorById(doctorId);
    }

    @Tool(description = "Check available consultation time slots for a doctor on a specific date (format: YYYY-MM-DD, e.g. 2026-11-25). Returns open slots with start and end times.")
    public List<TimeSlotResponse> checkDoctorAvailability(
            @ToolParam(description = "The unique numeric ID of the doctor") Long doctorId,
            @ToolParam(description = "Target consultation date in YYYY-MM-DD format (must not be in the past)") String date) {

        log.info("AI Tool executed: checkDoctorAvailability(doctorId={}, date='{}')", doctorId, date);
        LocalDate targetDate = LocalDate.parse(date.trim());
        return doctorAvailabilityService.getAvailableSlotsForDate(doctorId, targetDate);
    }

    @Tool(description = "Book a medical appointment for a patient. Requires patient ID, doctor ID, date (YYYY-MM-DD), start time (HH:mm:ss, e.g. 10:00:00), and reason for visit.")
    public AppointmentResponse bookAppointment(
            @ToolParam(description = "Numeric ID of the patient") Long patientId,
            @ToolParam(description = "Numeric ID of the doctor") Long doctorId,
            @ToolParam(description = "Appointment date in YYYY-MM-DD format") String appointmentDate,
            @ToolParam(description = "Appointment start time in HH:mm:ss format, e.g. 10:00:00") String startTime,
            @ToolParam(description = "Reason for consultation or symptoms description") String reason) {

        log.info("AI Tool executed: bookAppointment(patientId={}, doctorId={}, date='{}', time='{}')",
                patientId, doctorId, appointmentDate, startTime);

        LocalDate date = LocalDate.parse(appointmentDate.trim());
        LocalTime time;
        if (startTime.trim().length() == 5) {
            time = LocalTime.parse(startTime.trim() + ":00");
        } else {
            time = LocalTime.parse(startTime.trim());
        }

        AppointmentBookingRequest request = new AppointmentBookingRequest(
                patientId, doctorId, date, time, reason, null);

        return appointmentService.bookAppointment(request);
    }

    @Tool(description = "Retrieve appointments history for a patient by their patient ID.")
    public List<AppointmentResponse> getPatientAppointments(
            @ToolParam(description = "Numeric ID of the patient") Long patientId) {

        log.info("AI Tool executed: getPatientAppointments(patientId={})", patientId);
        Page<AppointmentResponse> page = appointmentService.getAppointmentsByPatient(patientId, PageRequest.of(0, 5));
        return page.getContent();
    }
}
