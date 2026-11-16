package me.geetprince.Dr.appoinment.repository;

import me.geetprince.Dr.appoinment.models.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsAvailableTrue(Long doctorId, DayOfWeek dayOfWeek);

    @Query("SELECT CASE WHEN count(a) > 0 THEN true ELSE false END FROM DoctorAvailability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.dayOfWeek = :dayOfWeek " +
            "AND (:excludeId IS NULL OR a.id != :excludeId) " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean hasOverlappingShift(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
