package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityRequest;
import me.geetprince.Dr.appoinment.dto.DoctorAvailabilityResponse;
import me.geetprince.Dr.appoinment.dto.TimeSlotResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Doctor;
import me.geetprince.Dr.appoinment.models.DoctorAvailability;
import me.geetprince.Dr.appoinment.repository.DoctorAvailabilityRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;

    public DoctorAvailabilityService(DoctorAvailabilityRepository availabilityRepository,
                                     DoctorRepository doctorRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
    }

    private DoctorAvailabilityResponse mapToResponse(DoctorAvailability availability) {
        return new DoctorAvailabilityResponse(
                availability.getId(),
                availability.getDoctor().getId(),
                availability.getDoctor().getFullName(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getSlotDurationMinutes(),
                availability.getIsAvailable()
        );
    }

    @Transactional
    public DoctorAvailabilityResponse addAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (availabilityRepository.hasOverlappingShift(doctorId, request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(), null)) {
            throw new DuplicateResourceException(
                    "Doctor already has an overlapping shift scheduled for " + request.getDayOfWeek()
            );
        }

        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctor(doctor);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(request.getSlotDurationMinutes());
        availability.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        DoctorAvailability saved = availabilityRepository.save(availability);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getDoctorAvailability(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return availabilityRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlotsForDate(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> activeShifts = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndIsAvailableTrue(doctorId, dayOfWeek);

        if (activeShifts.isEmpty()) {
            return Collections.emptyList();
        }

        List<TimeSlotResponse> slots = new ArrayList<>();
        boolean isToday = date.equals(LocalDate.now());
        LocalTime now = LocalTime.now();

        for (DoctorAvailability shift : activeShifts) {
            LocalTime slotStart = shift.getStartTime();
            int duration = shift.getSlotDurationMinutes();

            while (!slotStart.plusMinutes(duration).isAfter(shift.getEndTime())) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);
                boolean isPastTime = isToday && slotStart.isBefore(now);

                slots.add(new TimeSlotResponse(slotStart, slotEnd, !isPastTime));
                slotStart = slotEnd;
            }
        }

        return slots;
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailability(Long doctorId, Long availabilityId,
                                                         DoctorAvailabilityRequest request) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }

        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability schedule not found with ID: " + availabilityId));

        if (!availability.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException("Availability schedule with ID " + availabilityId + " does not belong to Doctor " + doctorId);
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (availabilityRepository.hasOverlappingShift(doctorId, request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(), availabilityId)) {
            throw new DuplicateResourceException(
                    "Doctor already has an overlapping shift scheduled for " + request.getDayOfWeek()
            );
        }

        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(request.getSlotDurationMinutes());
        availability.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        DoctorAvailability updated = availabilityRepository.save(availability);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAvailability(Long doctorId, Long availabilityId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }

        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability schedule not found with ID: " + availabilityId));

        if (!availability.getDoctor().getId().equals(doctorId)) {
            throw new ResourceNotFoundException("Availability schedule with ID " + availabilityId + " does not belong to Doctor " + doctorId);
        }

        availabilityRepository.delete(availability);
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Shift start time (" + startTime + ") must be before end time (" + endTime + ")");
        }
    }
}
