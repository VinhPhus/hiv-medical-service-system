-- Default ARV Protocols
INSERT IGNORE INTO arv_protocols (id, name, code, description, medications, dosage_per_day, recommended_duration_weeks, patient_category, contraindications, side_effects, monitoring_requirements, is_active, created_by, created_at, updated_at) VALUES
(1, 'TDF + 3TC + DTG (Người lớn)', 'TLD-ADULT', 'Phác đồ ARV cho người lớn', '{"TDF": "300mg", "3TC": "300mg", "DTG": "50mg"}', 1, 52, 'ADULT', 'Suy thận nặng, dị ứng thành phần', 'Buồn nôn, đau đầu, mất ngủ', 'Theo dõi chức năng thận, gan', true, 'admin', NOW(), NOW()),
(2, 'TDF + 3TC + EFV (Phụ nữ mang thai)', 'TLE-PREGNANT', 'Phác đồ ARV cho phụ nữ mang thai', '{"TDF": "300mg", "3TC": "300mg", "EFV": "600mg"}', 1, 52, 'PREGNANT_WOMAN', 'Thai kỳ 3 tháng đầu, rối loạn tâm thần', 'Chóng mặt, mơ mộng bất thường', 'Theo dõi thai nhi, chức năng gan', true, 'admin', NOW(), NOW()),
(3, 'ABC + 3TC + LPV/r (Trẻ em)', 'ABC-CHILD', 'Phác đồ ARV cho trẻ em', '{"ABC": "60mg", "3TC": "30mg", "LPV/r": "80/20mg"}', 2, 52, 'CHILD', 'Dị ứng ABC, suy gan nặng', 'Tiêu chảy, buồn nôn', 'Theo dõi tăng trưởng, chức năng gan', true, 'admin', NOW(), NOW()),
(4, 'TDF + 3TC + RAL (Người cao tuổi)', 'TLD-ELDERLY', 'Phác đồ ARV cho người cao tuổi', '{"TDF": "300mg", "3TC": "300mg", "RAL": "400mg"}', 2, 52, 'ELDERLY', 'Suy thận, tương tác thuốc', 'Đau cơ, mệt mỏi', 'Theo dõi chức năng thận, tương tác thuốc', true, 'admin', NOW(), NOW());

-- Doctor Profile
INSERT IGNORE INTO doctors (id, user_id, doctor_code, license_number, specialization, qualification, experience_years, consultation_fee, bio, is_available, working_hours, status, created_at, updated_at) VALUES
(1, 2, 'DR001', 'BS-12345', 'HIV/AIDS Specialist', 'Bác sĩ chuyên khoa I', 10, 200000.00, 'Bác sĩ chuyên về điều trị HIV/AIDS với 10 năm kinh nghiệm', true, '08:00-17:00', 'ACTIVE', NOW(), NOW());

-- Doctor Schedule
INSERT IGNORE INTO doctor_schedules (id, doctor_id, day_of_week, start_time, end_time, is_available, max_appointments, created_at, updated_at) VALUES
(1, 1, 'MONDAY', '08:00:00', '17:00:00', true, 20, NOW(), NOW()),
(2, 1, 'TUESDAY', '08:00:00', '17:00:00', true, 20, NOW(), NOW()),
(3, 1, 'WEDNESDAY', '08:00:00', '17:00:00', true, 20, NOW(), NOW()),
(4, 1, 'THURSDAY', '08:00:00', '17:00:00', true, 20, NOW(), NOW()),
(5, 1, 'FRIDAY', '08:00:00', '17:00:00', true, 20, NOW(), NOW()),
(6, 1, 'SATURDAY', '08:00:00', '12:00:00', true, 10, NOW(), NOW());
