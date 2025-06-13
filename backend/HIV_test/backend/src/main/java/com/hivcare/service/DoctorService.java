package com.hivcare.service;

import com.hivcare.dto.response.DoctorResponse;
import com.hivcare.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoctorService {

    public List<DoctorResponse> getAllActiveDoctors() {
        return getMockDoctors().stream()
                .filter(doctor -> doctor.getAvailableToday())
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long id) {
        return getMockDoctors().stream()
                .filter(doctor -> doctor.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<DoctorResponse> getAvailableDoctors() {
        return getMockDoctors().stream()
                .filter(DoctorResponse::getAvailableToday)
                .collect(Collectors.toList());
    }

    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return getMockDoctors().stream()
                .filter(doctor -> doctor.getSpecialization().toLowerCase()
                        .contains(specialization.toLowerCase()))
                .collect(Collectors.toList());
    }

    public DoctorResponse getCurrentDoctorProfile() {
        // For demo purposes, return the first doctor
        return getMockDoctors().get(0);
    }

    private List<DoctorResponse> getMockDoctors() {
        return Arrays.asList(
            DoctorResponse.builder()
                .id(1L)
                .fullName("BS. Nguyễn Văn Minh")
                .email("doctor1@hiv.care")
                .phoneNumber("0901234567")
                .licenseNumber("BS001234")
                .specialization("Bệnh nhiễm trùng")
                .qualification("Thạc sĩ Y khoa, Chuyên khoa II Nhiễm trùng")
                .experienceYears(15)
                .bio("Bác sĩ có 15 năm kinh nghiệm trong điều trị HIV/AIDS và các bệnh nhiễm trùng. Tốt nghiệp Đại học Y Hà Nội và có chứng chỉ chuyên khoa II.")
                .avatar("/images/doctors/doctor1.jpg")
                .rating(4.8)
                .totalReviews(156)
                .availableToday(true)
                .availableDays(Arrays.asList("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6"))
                .workingHoursStart(LocalTime.of(8, 0))
                .workingHoursEnd(LocalTime.of(17, 0))
                .clinicAddress("Bệnh viện Nhiệt đới Trung ương, Hà Nội")
                .isOnlineConsultationAvailable(true)
                .consultationFee(500000.0)
                .languages("Tiếng Việt, English")
                .createdAt(LocalDateTime.now().minusMonths(6))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build(),

            DoctorResponse.builder()
                .id(2L)
                .fullName("BS. Trần Thị Lan")
                .email("doctor2@hiv.care")
                .phoneNumber("0901234568")
                .licenseNumber("BS001235")
                .specialization("Tâm lý học lâm sàng")
                .qualification("Tiến sĩ Tâm lý học, Chuyên gia tư vấn HIV")
                .experienceYears(12)
                .bio("Chuyên gia tâm lý với 12 năm kinh nghiệm tư vấn cho người sống với HIV. Chuyên về hỗ trợ tâm lý và thích ứng với bệnh tật.")
                .avatar("/images/doctors/doctor2.jpg")
                .rating(4.9)
                .totalReviews(203)
                .availableToday(true)
                .availableDays(Arrays.asList("Thứ 2", "Thứ 4", "Thứ 6", "Thứ 7"))
                .workingHoursStart(LocalTime.of(9, 0))
                .workingHoursEnd(LocalTime.of(18, 0))
                .clinicAddress("Trung tâm Tư vấn Tâm lý, TP.HCM")
                .isOnlineConsultationAvailable(true)
                .consultationFee(400000.0)
                .languages("Tiếng Việt")
                .createdAt(LocalDateTime.now().minusMonths(8))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build(),

            DoctorResponse.builder()
                .id(3L)
                .fullName("BS. Lê Hoàng Nam")
                .email("doctor3@hiv.care")
                .phoneNumber("0901234569")
                .licenseNumber("BS001236")
                .specialization("Nội khoa tổng quát")
                .qualification("Bác sĩ Nội khoa, Chứng chỉ điều trị HIV")
                .experienceYears(10)
                .bio("Bác sĩ nội khoa với chuyên môn về điều trị HIV và các bệnh đi kèm. Có kinh nghiệm làm việc tại các bệnh viện lớn.")
                .avatar("/images/doctors/doctor3.jpg")
                .rating(4.7)
                .totalReviews(89)
                .availableToday(false)
                .availableDays(Arrays.asList("Thứ 3", "Thứ 5", "Thứ 7", "Chủ nhật"))
                .workingHoursStart(LocalTime.of(7, 30))
                .workingHoursEnd(LocalTime.of(16, 30))
                .clinicAddress("Bệnh viện Đại học Y Dược TP.HCM")
                .isOnlineConsultationAvailable(false)
                .consultationFee(350000.0)
                .languages("Tiếng Việt, English")
                .createdAt(LocalDateTime.now().minusMonths(4))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build(),

            DoctorResponse.builder()
                .id(4L)
                .fullName("BS. Phạm Thị Mai")
                .email("doctor4@hiv.care")
                .phoneNumber("0901234570")
                .licenseNumber("BS001237")
                .specialization("Sản phụ khoa")
                .qualification("Thạc sĩ Sản phụ khoa, Chuyên gia HIV ở phụ nữ mang thai")
                .experienceYears(8)
                .bio("Chuyên gia sản phụ khoa với kinh nghiệm điều trị HIV ở phụ nữ mang thai và phòng ngừa lây truyền từ mẹ sang con.")
                .avatar("/images/doctors/doctor4.jpg")
                .rating(4.6)
                .totalReviews(67)
                .availableToday(true)
                .availableDays(Arrays.asList("Thứ 2", "Thứ 3", "Thứ 5", "Thứ 6"))
                .workingHoursStart(LocalTime.of(8, 30))
                .workingHoursEnd(LocalTime.of(17, 30))
                .clinicAddress("Bệnh viện Phụ sản Trung ương")
                .isOnlineConsultationAvailable(true)
                .consultationFee(450000.0)
                .languages("Tiếng Việt")
                .createdAt(LocalDateTime.now().minusMonths(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build()
        );
    }
}
