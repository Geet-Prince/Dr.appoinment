package me.geetprince.Dr.appoinment.repository;

import me.geetprince.Dr.appoinment.models.Appointment;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient " +
            "JOIN FETCH a.doctor d " +
            "JOIN FETCH d.department " +
            "WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status IN ('PENDING', 'CONFIRMED')")
    List<Appointment> findActiveAppointmentsByDoctorAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date
    );

    @Query("SELECT CASE WHEN count(a) > 0 THEN true ELSE false END FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status IN ('PENDING', 'CONFIRMED') " +
            "AND (:excludeId IS NULL OR a.id != :excludeId) " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean hasDoctorConflict(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT CASE WHEN count(a) > 0 THEN true ELSE false END FROM Appointment a " +
            "WHERE a.patient.id = :patientId " +
            "AND a.appointmentDate = :date " +
            "AND a.status IN ('PENDING', 'CONFIRMED') " +
            "AND (:excludeId IS NULL OR a.id != :excludeId) " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean hasPatientConflict(
            @Param("patientId") Long patientId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient " +
            "JOIN FETCH a.doctor " +
            "WHERE a.appointmentDate = :date " +
            "AND a.status = :status")
    List<Appointment> findByAppointmentDateAndStatusWithDetails(
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status
    );
}
