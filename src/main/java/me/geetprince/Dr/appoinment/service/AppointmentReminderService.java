package me.geetprince.Dr.appoinment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geetprince.Dr.appoinment.models.Appointment;
import me.geetprince.Dr.appoinment.models.AppointmentStatus;
import me.geetprince.Dr.appoinment.repository.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentReminderService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Daily background cron job running at 08:00 AM every day.
     * Finds confirmed appointments scheduled for tomorrow and logs / dispatches reminders.
     */
    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * ?}")
    @Transactional(readOnly = true)
    public int sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Starting automated appointment reminders sweep for date: {}", tomorrow);

        List<Appointment> upcomingAppointments = appointmentRepository
                .findByAppointmentDateAndStatusWithDetails(tomorrow, AppointmentStatus.CONFIRMED);

        for (Appointment appointment : upcomingAppointments) {
            sendReminderNotification(appointment);
        }

        log.info("Completed automated reminder sweep. Dispatched {} reminders.", upcomingAppointments.size());
        return upcomingAppointments.size();
    }

    private void sendReminderNotification(Appointment appointment) {
        log.info("🔔 REMINDER SENT -> Patient: {} ({}), Doctor: {} ({}), Time: {}, Reason: '{}'",
                appointment.getPatient().getFullName(),
                appointment.getPatient().getEmail(),
                appointment.getDoctor().getFullName(),
                appointment.getDoctor().getSpecialization(),
                appointment.getStartTime(),
                appointment.getReason());
    }
}
