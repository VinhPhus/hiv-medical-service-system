package com.hivcare.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hivcare.entity.Appointment;
import com.hivcare.entity.MedicationReminder;

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
    public void sendAppointmentReminder(Appointment appointment) {
        if (!emailEnabled || appointment.isAnonymous()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(appointment.getPatient().getUser().getEmail());
            message.setSubject("Nhắc nhở lịch hẹn - HIV Care Center");
            
            String body = String.format(
                "Kính chào %s,\n\n" +
                "Bạn có lịch hẹn vào ngày mai:\n\n" +
                "Mã lịch hẹn: %s\n" +
                "Bác sĩ: %s\n" +
                "Thời gian: %s\n" +
                "Loại khám: %s\n\n" +
                "Vui lòng đến đúng giờ.\n\n" +
                "Trân trọng,\n" +
                "HIV Care Center",
                appointment.getPatient().getUser().getFullName(),
                appointment.getAppointmentCode(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                appointment.getAppointmentType().toString()
            );
            
            message.setText(body);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send appointment reminder email: " + e.getMessage());
        }
    }

    @Async
    public void sendMedicationReminder(MedicationReminder reminder) {
        if (!emailEnabled) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(reminder.getTreatment().getPatient().getUser().getEmail());
            message.setSubject("Nhắc nhở uống thuốc - HIV Care Center");
            
            String body = String.format(
                "Kính chào %s,\n\n" +
                "Đã đến giờ uống thuốc:\n\n" +
                "Tên thuốc: %s\n" +
                "Liều lượng: %s\n" +
                "Hướng dẫn: %s\n\n" +
                "Vui lòng uống thuốc đúng giờ theo chỉ định của bác sĩ.\n\n" +
                "Trân trọng,\n" +
                "HIV Care Center",
                reminder.getTreatment().getPatient().getUser().getFullName(),
                reminder.getMedicationName(),
                reminder.getDosage() != null ? reminder.getDosage() : "Theo chỉ định",
                reminder.getInstructions() != null ? reminder.getInstructions() : "Không có hướng dẫn đặc biệt"
            );
            
            message.setText(body);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send medication reminder email: " + e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String email, String fullName) {
        if (!emailEnabled) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Chào mừng đến với HIV Care Center");
            
            String body = String.format(
                "Kính chào %s,\n\n" +
                "Chào mừng bạn đến với HIV Care Center!\n\n" +
                "Tài khoản của bạn đã được tạo thành công. Bạn có thể:\n" +
                "- Đặt lịch hẹn với bác sĩ\n" +
                "- Xem kết quả xét nghiệm\n" +
                "- Theo dõi lịch sử điều trị\n" +
                "- Nhận nhắc nhở uống thuốc\n\n" +
                "Chúng tôi cam kết bảo mật thông tin cá nhân của bạn.\n\n" +
                "Trân trọng,\n" +
                "HIV Care Center",
                fullName
            );
            
            message.setText(body);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
}
