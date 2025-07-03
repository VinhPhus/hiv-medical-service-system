package com.hivcare.service;

import com.hivcare.entity.MedicationReminder;
import com.hivcare.entity.Treatment;
import com.hivcare.repository.MedicationReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class MedicationReminderService {

    @Autowired
    private MedicationReminderRepository medicationReminderRepository;

    @Autowired
    private NotificationService notificationService;

    public List<MedicationReminder> getAllReminders() {
        return medicationReminderRepository.findAll();
    }

    public List<MedicationReminder> getActiveReminders() {
        return medicationReminderRepository.findAllActive();
    }

    public List<MedicationReminder> getRemindersByTreatment(Long treatmentId) {
        return medicationReminderRepository.findByTreatmentId(treatmentId);
    }

    public List<MedicationReminder> getRemindersByPatient(Long patientId) {
        return medicationReminderRepository.findByPatientId(patientId);
    }

    public List<MedicationReminder> getActiveRemindersByPatient(Long patientId) {
        return medicationReminderRepository.findActiveByPatientId(patientId);
    }

    public void createRemindersForTreatment(Treatment treatment) {
        // Parse frequency and create reminders accordingly
        String frequency = treatment.getFrequency();
        if (frequency == null || frequency.isEmpty()) {
            return;
        }

        // Example: "2 times daily" or "3 times daily"
        String[] parts = frequency.split(" ");
        if (parts.length >= 3 && parts[1].equals("times") && parts[2].equals("daily")) {
            int timesPerDay = Integer.parseInt(parts[0]);
            int intervalHours = 24 / timesPerDay;

            for (int i = 0; i < timesPerDay; i++) {
                LocalTime reminderTime = LocalTime.of(9 + (i * intervalHours), 0); // Start from 9 AM
                
                MedicationReminder reminder = new MedicationReminder();
                reminder.setTreatment(treatment);
                reminder.setReminderTime(reminderTime);
                reminder.setMedicationName(treatment.getArvProtocol() != null ? 
                    treatment.getArvProtocol().getName() : "ARV Medication");
                reminder.setDosage(treatment.getDosageInstructions());
                reminder.setInstructions(treatment.getNotes());
                reminder.setActive(true);
                reminder.setNextReminder(LocalDateTime.now()
                    .withHour(reminderTime.getHour())
                    .withMinute(reminderTime.getMinute()));
                
                medicationReminderRepository.save(reminder);
            }
        }
    }

    public void deactivateRemindersForTreatment(Long treatmentId) {
        List<MedicationReminder> reminders = medicationReminderRepository.findByTreatmentId(treatmentId);
        for (MedicationReminder reminder : reminders) {
            reminder.setActive(false);
            medicationReminderRepository.save(reminder);
        }
    }

    public void pauseRemindersForTreatment(Long treatmentId) {
        deactivateRemindersForTreatment(treatmentId);
    }

    public void resumeRemindersForTreatment(Long treatmentId) {
        List<MedicationReminder> reminders = medicationReminderRepository.findByTreatmentId(treatmentId);
        for (MedicationReminder reminder : reminders) {
            reminder.setActive(true);
            medicationReminderRepository.save(reminder);
        }
    }

    public void deleteRemindersForTreatment(Long treatmentId) {
        List<MedicationReminder> reminders = medicationReminderRepository.findByTreatmentId(treatmentId);
        medicationReminderRepository.deleteAll(reminders);
    }

    public void processReminders() {
        List<MedicationReminder> dueReminders = medicationReminderRepository.findDueReminders(LocalDateTime.now());
        
        for (MedicationReminder reminder : dueReminders) {
            // Send notification
            notificationService.sendMedicationReminder(reminder);
            
            // Update last sent and next reminder time
            reminder.setLastSent(LocalDateTime.now());
            reminder.setNextReminder(LocalDateTime.now().plusDays(1)
                .withHour(reminder.getReminderTime().getHour())
                .withMinute(reminder.getReminderTime().getMinute()));
            
            medicationReminderRepository.save(reminder);
        }
    }

    public long getActiveRemindersCount() {
        return medicationReminderRepository.countActiveReminders();
    }
} 