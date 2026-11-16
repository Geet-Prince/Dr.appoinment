package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DoctorRequest;
import me.geetprince.Dr.appoinment.dto.DoctorResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Department;
import me.geetprince.Dr.appoinment.models.Doctor;
import me.geetprince.Dr.appoinment.repository.DepartmentRepository;
import me.geetprince.Dr.appoinment.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorService(DoctorRepository doctorRepository, DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getEmail(),
                doctor.getPhoneNumber(),
                doctor.getGender(),
                doctor.getSpecialization(),
                doctor.getQualification(),
                doctor.getExperienceYears(),
                doctor.getLicenseNumber(),
                doctor.getConsultationFee(),
                doctor.getAbout(),
                doctor.getProfileImageUrl(),
                doctor.getDepartment() != null ? doctor.getDepartment().getId() : null,
                doctor.getDepartment() != null ? doctor.getDepartment().getName() : null,
                doctor.getCreatedAt(),
                doctor.getUpdatedAt()
        );
    }

    @Transactional
    public DoctorResponse registerDoctor(DoctorRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String license = request.getLicenseNumber().trim();

        if (doctorRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Doctor with email '" + email + "' is already registered");
        }

        if (doctorRepository.existsByLicenseNumber(license)) {
            throw new DuplicateResourceException("Doctor with license number '" + license + "' is already registered");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Doctor doctor = new Doctor();
        doctor.setFullName(request.getFullName().trim());
        doctor.setEmail(email);
        doctor.setPhoneNumber(request.getPhoneNumber().trim());
        doctor.setGender(request.getGender().trim());
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setQualification(request.getQualification().trim());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setLicenseNumber(license);
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAbout(request.getAbout() != null ? request.getAbout().trim() : null);
        doctor.setProfileImageUrl(request.getProfileImageUrl() != null ? request.getProfileImageUrl().trim() : null);
        doctor.setDepartment(department);

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToResponse(savedDoctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));
        return mapToResponse(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }
        return doctorRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> searchDoctors(String name, String specialization, Long departmentId,
                                              Integer minExperience, BigDecimal maxFee, Pageable pageable) {
        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanSpec = (specialization != null && !specialization.trim().isEmpty()) ? specialization.trim() : null;

        return doctorRepository.searchDoctors(cleanName, cleanSpec, departmentId, minExperience, maxFee, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));

        String email = request.getEmail().trim().toLowerCase();
        String license = request.getLicenseNumber().trim();

        if (!existingDoctor.getEmail().equalsIgnoreCase(email) && doctorRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Doctor with email '" + email + "' is already registered");
        }

        if (!existingDoctor.getLicenseNumber().equalsIgnoreCase(license) && doctorRepository.existsByLicenseNumber(license)) {
            throw new DuplicateResourceException("Doctor with license number '" + license + "' is already registered");
        }

        if (!existingDoctor.getDepartment().getId().equals(request.getDepartmentId())) {
            Department newDepartment = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
            existingDoctor.setDepartment(newDepartment);
        }

        existingDoctor.setFullName(request.getFullName().trim());
        existingDoctor.setEmail(email);
        existingDoctor.setPhoneNumber(request.getPhoneNumber().trim());
        existingDoctor.setGender(request.getGender().trim());
        existingDoctor.setSpecialization(request.getSpecialization().trim());
        existingDoctor.setQualification(request.getQualification().trim());
        existingDoctor.setExperienceYears(request.getExperienceYears());
        existingDoctor.setLicenseNumber(license);
        existingDoctor.setConsultationFee(request.getConsultationFee());
        existingDoctor.setAbout(request.getAbout() != null ? request.getAbout().trim() : null);
        existingDoctor.setProfileImageUrl(request.getProfileImageUrl() != null ? request.getProfileImageUrl().trim() : null);

        Doctor updated = doctorRepository.save(existingDoctor);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + id);
        }
        doctorRepository.deleteById(id);
    }
}
