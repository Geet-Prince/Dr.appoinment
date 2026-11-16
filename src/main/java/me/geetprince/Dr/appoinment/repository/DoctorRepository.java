package me.geetprince.Dr.appoinment.repository;

import me.geetprince.Dr.appoinment.models.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByDepartmentId(Long departmentId);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.department WHERE d.id = :id")
    Optional<Doctor> findByIdWithDepartment(@Param("id") Long id);

    List<Doctor> findByDepartmentId(Long departmentId);

    @Query(value = "SELECT d FROM Doctor d JOIN FETCH d.department dep WHERE " +
            "(CAST(:name AS string) IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
            "(CAST(:specialization AS string) IS NULL OR LOWER(d.specialization) = LOWER(CAST(:specialization AS string))) AND " +
            "(:departmentId IS NULL OR dep.id = :departmentId) AND " +
            "(:minExperience IS NULL OR d.experienceYears >= :minExperience) AND " +
            "(:maxFee IS NULL OR d.consultationFee <= :maxFee)",
            countQuery = "SELECT count(d) FROM Doctor d WHERE " +
            "(CAST(:name AS string) IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
            "(CAST(:specialization AS string) IS NULL OR LOWER(d.specialization) = LOWER(CAST(:specialization AS string))) AND " +
            "(:departmentId IS NULL OR d.department.id = :departmentId) AND " +
            "(:minExperience IS NULL OR d.experienceYears >= :minExperience) AND " +
            "(:maxFee IS NULL OR d.consultationFee <= :maxFee)")
    Page<Doctor> searchDoctors(
            @Param("name") String name,
            @Param("specialization") String specialization,
            @Param("departmentId") Long departmentId,
            @Param("minExperience") Integer minExperience,
            @Param("maxFee") BigDecimal maxFee,
            Pageable pageable
    );
}
