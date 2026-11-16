package me.geetprince.Dr.appoinment.repository;

import me.geetprince.Dr.appoinment.models.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT p FROM Prescription p " +
            "JOIN FETCH p.appointment a " +
            "JOIN FETCH a.patient " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.department " +
            "WHERE p.id = :id")
    Optional<Prescription> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Prescription p " +
            "JOIN FETCH p.appointment a " +
            "JOIN FETCH a.patient " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.department " +
            "WHERE a.id = :appointmentId")
    Optional<Prescription> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT p FROM Prescription p " +
            "JOIN FETCH p.appointment a " +
            "JOIN FETCH a.patient pt " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.department " +
            "WHERE pt.id = :patientId " +
            "ORDER BY p.prescribedAt DESC")
    List<Prescription> findPrescriptionsByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p " +
            "JOIN FETCH p.appointment a " +
            "JOIN FETCH a.patient pt " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.department " +
            "WHERE d.id = :doctorId " +
            "ORDER BY p.prescribedAt DESC")
    List<Prescription> findPrescriptionsByDoctorId(@Param("doctorId") Long doctorId);
}
