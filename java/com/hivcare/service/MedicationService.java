package com.hivcare.service;

import com.hivcare.dto.response.MedicationReminderResponse;
import com.hivcare.dto.response.ARVRegimenResponse;
import com.hivcare.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationService {

    @SuppressWarnings("unused")
    public List<MedicationReminderResponse> getPatientReminders(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Return mock reminders
        return Arrays.asList(
            MedicationReminderResponse.builder()
                .id(1L)
                .medicationName("TDF + 3TC + DTG")
                .dosage("1 viên")
                .reminderTime(LocalTime.of(8, 0))
                .isActive(true)
                .lastTaken(LocalDateTime.now().minusHours(12))
                .nextRefillDate(LocalDateTime.of(2024, 12, 30, 0, 0))
                .instructions("Uống cùng với thức ăn")
                .build(),
                
            MedicationReminderResponse.builder()
                .id(2L)
                .medicationName("TDF + 3TC + DTG")
                .dosage("1 viên")
                .reminderTime(LocalTime.of(20, 0))
                .isActive(true)
                .lastTaken(LocalDateTime.now().minusHours(12))
                .nextRefillDate(LocalDateTime.of(2024, 12, 30, 0, 0))
                .instructions("Uống cùng với thức ăn")
                .build()
        );
    }

    public List<ARVRegimenResponse> getAvailableARVRegimens() {
        return Arrays.asList(
            ARVRegimenResponse.builder()
                .id(1L)
                .name("TDF + 3TC + DTG")
                .description("Phác đồ chuẩn cho người lớn")
                .components(Arrays.asList("Tenofovir", "Lamivudine", "Dolutegravir"))
                .suitableFor(Arrays.asList("Người lớn", "Phụ nữ không mang thai"))
                .contraindications(Arrays.asList("Suy thận nặng", "Dị ứng thuốc"))
                .sideEffects(Arrays.asList("Buồn nôn nhẹ", "Đau đầu"))
                .dosageInstructions("1 viên/ngày, uống cùng thức ăn")
                .isPreferred(true)
                .build(),
                
            ARVRegimenResponse.builder()
                .id(2L)
                .name("ABC + 3TC + DTG")
                .description("Phác đồ thay thế cho người có vấn đề về thận")
                .components(Arrays.asList("Abacavir", "Lamivudine", "Dolutegravir"))
                .suitableFor(Arrays.asList("Người lớn", "Có vấn đề về thận"))
                .contraindications(Arrays.asList("HLA-B*5701 dương tính", "Bệnh gan nặng"))
                .sideEffects(Arrays.asList("Phản ứng dị ứng", "Mệt mỏi"))
                .dosageInstructions("1 viên/ngày")
                .isPreferred(false)
                .build(),
                
            ARVRegimenResponse.builder()
                .id(3L)
                .name("TDF + FTC + EFV")
                .description("Phác đồ cũ, ít được sử dụng")
                .components(Arrays.asList("Tenofovir", "Emtricitabine", "Efavirenz"))
                .suitableFor(Arrays.asList("Người lớn"))
                .contraindications(Arrays.asList("Rối loạn tâm thần", "Mang thai"))
                .sideEffects(Arrays.asList("Chóng mặt", "Mơ mộng bất thường", "Trầm cảm"))
                .dosageInstructions("1 viên/ngày, tránh uống khi đói")
                .isPreferred(false)
                .build()
        );
    }
}
