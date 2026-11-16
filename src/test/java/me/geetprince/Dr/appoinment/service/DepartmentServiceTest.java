package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.DepartmentRequest;
import me.geetprince.Dr.appoinment.dto.DepartmentResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.Department;
import me.geetprince.Dr.appoinment.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private DepartmentRequest request;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "Cardiology", "Heart and vascular care", LocalDateTime.now());
        request = new DepartmentRequest("Cardiology", "Heart and vascular care");
    }

    @Test
    @DisplayName("Should successfully create a department when name is unique")
    void createDepartment_Success() {
        when(departmentRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentResponse response = departmentService.createDepartment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Cardiology");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating department with existing name")
    void createDepartment_DuplicateName_ThrowsException() {
        when(departmentRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Should return all departments")
    void getAllDepartments_Success() {
        Department dep2 = new Department(2L, "Neurology", "Brain and nervous system", LocalDateTime.now());
        when(departmentRepository.findAll()).thenReturn(List.of(department, dep2));

        List<DepartmentResponse> list = departmentService.getAllDepartments();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getName()).isEqualTo("Cardiology");
        assertThat(list.get(1).getName()).isEqualTo("Neurology");
    }

    @Test
    @DisplayName("Should return department by ID when found")
    void getDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        DepartmentResponse response = departmentService.getDepartmentById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department ID does not exist")
    void getDepartmentById_NotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with ID: 99");
    }

    @Test
    @DisplayName("Should search departments by keyword")
    void searchDepartments_Success() {
        when(departmentRepository.findByNameContainingIgnoreCase("cardio"))
                .thenReturn(List.of(department));

        List<DepartmentResponse> results = departmentService.searchDepartments("cardio");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("Should return all departments when search keyword is empty")
    void searchDepartments_EmptyKeyword_ReturnsAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<DepartmentResponse> results = departmentService.searchDepartments("");

        assertThat(results).hasSize(1);
        verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Should successfully update existing department")
    void updateDepartment_Success() {
        DepartmentRequest updateRequest = new DepartmentRequest("Advanced Cardiology", "Updated description");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCase("Advanced Cardiology")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponse response = departmentService.updateDepartment(1L, updateRequest);

        assertThat(response.getName()).isEqualTo("Advanced Cardiology");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        verify(departmentRepository).save(department);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating with another department's name")
    void updateDepartment_DuplicateName() {
        DepartmentRequest updateRequest = new DepartmentRequest("Neurology", "Duplicate name test");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCase("Neurology")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Should delete department when ID exists")
    void deleteDepartment_Success() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(departmentRepository).deleteById(1L);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent department")
    void deleteDepartment_NotFound() {
        when(departmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.deleteDepartment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with ID: 99");

        verify(departmentRepository, never()).deleteById(anyLong());
    }
}
