package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DepartmentRequest;
import me.geetprince.Dr.appoinment.dto.DepartmentResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Department;
import me.geetprince.Dr.appoinment.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    private DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt()
        );
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String trimmedName = request.getName().trim();

        if (departmentRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException(
                    "Department with name '" + trimmedName + "' already exists"
            );
        }

        Department department = new Department();
        department.setName(trimmedName);
        department.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Department savedDepartment = departmentRepository.save(department);
        return mapToResponse(savedDepartment);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id
                ));
        return mapToResponse(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> searchDepartments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDepartments();
        }

        return departmentRepository.findByNameContainingIgnoreCase(keyword.trim())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id
                ));

        String trimmedName = request.getName().trim();

        // Check if another department already has this name
        if (!existingDepartment.getName().equalsIgnoreCase(trimmedName)
                && departmentRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException(
                    "Department with name '" + trimmedName + "' already exists"
            );
        }

        existingDepartment.setName(trimmedName);
        existingDepartment.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Department updated = departmentRepository.save(existingDepartment);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Department not found with ID: " + id
            );
        }
        departmentRepository.deleteById(id);
    }
}
