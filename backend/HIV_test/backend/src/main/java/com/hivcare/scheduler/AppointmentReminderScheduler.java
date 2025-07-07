
package com.hivcare.scheduler;

import com.hivcare.entity.Appointment;
import com.hivcare.service.AppointmentService;
import com.hivcare.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    public void sendAppointmentReminders() {
        logger.info("Starting appointment reminder job");
        
        try {
            List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointmentsForReminder();
            
            for (Appointment appointment : upcomingAppointments) {
                if (!appointment.isReminderSent()) {
                    notificationService.sendAppointmentReminder(appointment);
                    
                    // Mark as reminder sent
                    appointment.setReminderSent(true);
                    appointmentService.updateAppointment(appointment.getId(), appointment);
                    
                    logger.info("Sent reminder for appointment: {}", appointment.getAppointmentCode());
                }
            }
            
            logger.info("Completed appointment reminder job. Sent {} reminders", upcomingAppointments.size());
            
        } catch (Exception e) {
            logger.error("Error in appointment reminder job", e);
        }
    }

    @Scheduled(cron = "0 0 18 * * ?") // Run daily at 6 PM
    public void checkMissedAppointments() {
        logger.info("Starting missed appointment check");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            
            // Logic to find and update missed appointments
            // This would need implementation based on business rules
            
        } catch (Exception e) {
            logger.error("Error in missed appointment check", e);
        }
    }
}

