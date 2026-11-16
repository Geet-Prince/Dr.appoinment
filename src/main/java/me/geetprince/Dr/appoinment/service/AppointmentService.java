package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.AppointmentBookingRequest;
import me.geetprince.Dr.appoinment.dto.AppointmentRescheduleRequest;
import me.geetprince.Dr.appoinment.dto.AppointmentResponse;
import me.geetprince.Dr.appoinment.dto.AppointmentStatusUpdateRequest;
import me.geetprince.Dr.appoinment.exception.AppointmentConflictException;
import me.geetprince.Dr.appoinment.exception.DoctorUnavailableException;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.*;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorAvailabilityRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              DoctorAvailabilityRepository availabilityRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFullName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhoneNumber(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getFullName(),
                appointment.getDoctor().getSpecialization(),
                appointment.getDoctor().getDepartment() != null ? appointment.getDoctor().getDepartment().getName() : null,
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getConsultationFee(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentBookingRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Doctor doctor = doctorRepository.findByIdWithDepartment(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        validateAppointmentDateTime(request.getAppointmentDate(), request.getStartTime());

        DoctorAvailability matchingShift = findMatchingShift(doctor.getId(), request.getAppointmentDate(), request.getStartTime());
        LocalTime endTime = request.getStartTime().plusMinutes(matchingShift.getSlotDurationMinutes());

        if (endTime.isAfter(matchingShift.getEndTime())) {
            throw new InvalidAppointmentException("Appointment slot exceeds the doctor's scheduled shift end time (" + matchingShift.getEndTime() + ")");
        }

        checkConflicts(doctor.getId(), patient.getId(), request.getAppointmentDate(), request.getStartTime(), endTime, null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setReason(request.getReason().trim());
        appointment.setConsultationFee(doctor.getConsultationFee());
        appointment.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));
        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsByPatient(Long patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return appointmentRepository.findByPatientId(patientId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsByDoctor(Long doctorId, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Cannot cancel an appointment that has already been completed");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            String updatedNotes = (appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") +
                    "[Cancellation Reason]: " + reason.trim();
            appointment.setNotes(updatedNotes);
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Long id, AppointmentRescheduleRequest request) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentException("Cannot reschedule an appointment with status: " + appointment.getStatus());
        }

        validateAppointmentDateTime(request.getAppointmentDate(), request.getStartTime());

        DoctorAvailability matchingShift = findMatchingShift(appointment.getDoctor().getId(), request.getAppointmentDate(), request.getStartTime());
        LocalTime newEndTime = request.getStartTime().plusMinutes(matchingShift.getSlotDurationMinutes());

        if (newEndTime.isAfter(matchingShift.getEndTime())) {
            throw new InvalidAppointmentException("Rescheduled slot exceeds the doctor's shift end time (" + matchingShift.getEndTime() + ")");
        }

        checkConflicts(appointment.getDoctor().getId(), appointment.getPatient().getId(),
                request.getAppointmentDate(), request.getStartTime(), newEndTime, appointment.getId());

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(newEndTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
            String updatedNotes = (appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") +
                    "[Rescheduled Reason]: " + request.getReason().trim();
            appointment.setNotes(updatedNotes);
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        appointment.setStatus(request.getStatus());
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            appointment.setNotes(request.getNotes().trim());
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    private void validateAppointmentDateTime(LocalDate date, LocalTime startTime) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new InvalidAppointmentException("Appointment date cannot be in the past (" + date + ")");
        }

        if (date.equals(today) && startTime.isBefore(LocalTime.now())) {
            throw new InvalidAppointmentException("Appointment start time cannot be in the past (" + startTime + ")");
        }
    }

    private DoctorAvailability findMatchingShift(Long doctorId, LocalDate date, LocalTime startTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> activeShifts = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndIsAvailableTrue(doctorId, dayOfWeek);

        if (activeShifts.isEmpty()) {
            throw new DoctorUnavailableException("Doctor is not scheduled to work on " + dayOfWeek);
        }

        return activeShifts.stream()
                .filter(shift -> !startTime.isBefore(shift.getStartTime()) && startTime.isBefore(shift.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new DoctorUnavailableException(
                        "Doctor has no consultation shift covering " + startTime + " on " + dayOfWeek
                ));
    }

    private void checkConflicts(Long doctorId, Long patientId, LocalDate date,
                                LocalTime startTime, LocalTime endTime, Long excludeId) {
        if (appointmentRepository.hasDoctorConflict(doctorId, date, startTime, endTime, excludeId)) {
            throw new AppointmentConflictException("Doctor already has an appointment scheduled at this time");
        }

        if (appointmentRepository.hasPatientConflict(patientId, date, startTime, endTime, excludeId)) {
            throw new AppointmentConflictException("Patient already has an active appointment scheduled at this time");
        }
    }
}
