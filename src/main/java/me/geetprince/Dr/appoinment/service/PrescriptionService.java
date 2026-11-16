package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.PrescriptionRequest;
import me.geetprince.Dr.appoinment.dto.PrescriptionResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.InvalidAppointmentException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Appointment;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import me.geetprince.Dr.appoinment.models.Prescription;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import me.geetprince.Dr.appoinment.repository.PatientRepository;
import me.geetprince.Dr.appoinment.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    private PrescriptionResponse mapToResponse(Prescription prescription) {
        Appointment a = prescription.getAppointment();
        return new PrescriptionResponse(
                prescription.getId(),
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getDoctor().getId(),
                a.getDoctor().getFullName(),
                a.getDoctor().getSpecialization(),
                a.getDoctor().getDepartment() != null ? a.getDoctor().getDepartment().getName() : null,
                prescription.getDiagnosis(),
                prescription.getMedicines(),
                prescription.getInstructions(),
                prescription.getPrescribedAt()
        );
    }

    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + request.getAppointmentId()));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Prescriptions can only be created for completed appointments (current status: " + appointment.getStatus() + ")");
        }

        if (prescriptionRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new DuplicateResourceException("A prescription has already been created for appointment ID: " + request.getAppointmentId());
        }

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setDiagnosis(request.getDiagnosis().trim());
        prescription.setMedicines(request.getMedicines().trim());
        prescription.setInstructions(request.getInstructions() != null ? request.getInstructions().trim() : null);

        Prescription saved = prescriptionRepository.save(prescription);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByAppointmentId(Long appointmentId) {
        Prescription prescription = prescriptionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No prescription found for appointment ID: " + appointmentId));
        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return prescriptionRepository.findPrescriptionsByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByDoctorId(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return prescriptionRepository.findPrescriptionsByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PrescriptionResponse updatePrescription(Long id, PrescriptionRequest request) {
        Prescription existing = prescriptionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));

        existing.setDiagnosis(request.getDiagnosis().trim());
        existing.setMedicines(request.getMedicines().trim());
        existing.setInstructions(request.getInstructions() != null ? request.getInstructions().trim() : null);

        Prescription updated = prescriptionRepository.save(existing);
        return mapToResponse(updated);
    }
}
