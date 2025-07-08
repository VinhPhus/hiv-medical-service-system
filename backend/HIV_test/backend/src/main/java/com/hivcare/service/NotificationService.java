package com.hivcare.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hivcare.entity.Appointment;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.notification.email-enabled:true}")
    private boolean emailEnabled;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        if (!emailEnabled || appointment.isAnonymous()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(appointment.getPatient().getUser().getEmail());
            message.setSubject("Xác nhận lịch hẹn - HIV Care Center");
            
            String body = String.format(
                "Kính chào %s,\n\n" +
                "Lịch hẹn của bạn đã được xác nhận:\n\n" +
                "Mã lịch hẹn: %s\n" +
                "Bác sĩ: %s\n" +
                "Thời gian: %s\n" +
                "Loại khám: %s\n" +
                "Lý do: %s\n\n" +
                "Vui lòng đến đúng giờ. Nếu có thay đổi, xin liên hệ với chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "HIV Care Center",
                appointment.getPatient().getUser().getFullName(),
                appointment.getAppointmentCode(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                appointment.getAppointmentType().toString(),
                appointment.getReason() != null ? appointment.getReason() : "Không có"
            );
            
            message.setText(body);
            mailSender.send(message);
            
        } catch (Exception e) {
            // Log error but don't throw exception
            System.err.println("Failed to send appointment confirmation email: " + e.getMessage());
        }
    }

    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        if (!emailEnabled || appointment.isAnonymous()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(appointment.getPatient().getUser().getEmail());
            message.setSubject("Hủy lịch hẹn - HIV Care Center");
            
            String body = String.format(
                "Kính chào %s,\n\n" +
                "Lịch hẹn của bạn đã được hủy:\n\n" +
                "Mã lịch hẹn: %s\n" +
                "Bác sĩ: %s\n" +
                "Thời gian: %s\n\n" +
                "Nếu bạn muốn đặt lịch hẹn mới, vui lòng liên hệ với chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "HIV Care Center",
                appointment.getPatient().getUser().getFullName(),
                appointment.getAppointmentCode(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            
            message.setText(body);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send appointment cancellation email: " + e.getMessage());
        }
    }

}
