package me.geetprince.Dr.appoinment.repository;

import me.geetprince.Dr.appoinment.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Optional<Review> findByAppointmentId(Long appointmentId);

    @Query("SELECT r FROM Review r JOIN FETCH r.patient JOIN FETCH r.doctor JOIN FETCH r.appointment WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Review r JOIN FETCH r.patient JOIN FETCH r.doctor JOIN FETCH r.appointment WHERE r.appointment.id = :appointmentId")
    Optional<Review> findByAppointmentIdWithDetails(@Param("appointmentId") Long appointmentId);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.patient JOIN FETCH r.doctor JOIN FETCH r.appointment WHERE r.doctor.id = :doctorId",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.doctor.id = :doctorId")
    Page<Review> findByDoctorIdWithDetails(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.patient JOIN FETCH r.doctor JOIN FETCH r.appointment WHERE r.patient.id = :patientId",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.patient.id = :patientId")
    Page<Review> findByPatientIdWithDetails(@Param("patientId") Long patientId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    long countByDoctorId(Long doctorId);
}
