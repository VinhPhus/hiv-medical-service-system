
#  HIV Treatment and Medical Services System


##  Giới thiệu

**HIV Treatment and Medical Services System** là một phần mềm hỗ trợ người bệnh HIV tiếp cận dịch vụ y tế một cách an toàn, tiện lợi và riêng tư.  
Hệ thống cung cấp đầy đủ thông tin, công cụ quản lý điều trị và tương tác giữa bệnh nhân và bác sĩ, nhằm mục tiêu:

- Tăng khả năng tiếp cận dịch vụ điều trị HIV.
- Giảm sự kỳ thị thông qua các chức năng ẩn danh và giáo dục cộng đồng.
- Hỗ trợ bác sĩ và quản lý bệnh viện tối ưu hóa quy trình điều trị.

---

##  Vai trò người dùng

- `Guest` – Người truy cập không đăng nhập
- `Customer` – Bệnh nhân đăng ký tài khoản
- `Staff` – Nhân viên y tế hỗ trợ
- `Doctor` – Bác sĩ điều trị
- `Manager` – Quản lý cơ sở y tế
- `Admin` – Quản trị hệ thống

---

##  Chức năng chính

- Trang chủ giới thiệu cơ sở y tế, blog chia sẻ, tài liệu giáo dục giảm kỳ thị HIV.
- Đăng ký lịch khám và điều trị, cho phép chọn bác sĩ.
- Tra cứu kết quả xét nghiệm (CD4, tải lượng HIV, ARV...).
- Nhắc lịch tái khám và nhắc nhở uống thuốc theo phác đồ.
- Đặt lịch tư vấn trực tuyến (cho phép ẩn danh).
- Bác sĩ chọn & tuỳ chỉnh phác đồ ARV phù hợp (VD: TDF + 3TC + DTG...).
- Quản lý hồ sơ bác sĩ (thông tin, bằng cấp, lịch làm việc...).
- Quản lý hồ sơ bệnh nhân, lịch sử tư vấn, lịch sử điều trị.
- Dashboard báo cáo thống kê cho Admin/Manager.

---
## Yêu cầu chức năng


##  Yêu cầu phi chức năng

### Bảo mật & Quyền riêng tư:
- Hỗ trợ đăng ký ẩn danh.
- Mã hóa thông tin hồ sơ và kết quả điều trị.
- Phân quyền chi tiết theo từng loại người dùng.

### Hiệu năng:
- Tối ưu tốc độ phản hồi cho các thao tác phổ biến.
- Tải trang nhanh, chịu tải cao (hỗ trợ nhiều người dùng cùng lúc).

###  Responsive UI:
- Tương thích đa nền tảng: desktop, tablet, mobile.

###  Khả năng mở rộng:
- Thiết kế mở rộng module dễ dàng.
- Dễ dàng tích hợp chatbot, AI, API bên thứ ba trong tương lai.

###  Giám sát & Phân tích:
- Dashboard quản trị real-time.
- Báo cáo chi tiết: tình trạng bệnh nhân, số ca điều trị, xu hướng phác đồ.

---

##  Công nghệ sử dụng

- **Java JDK 23**
- **Spring Boot** (Backend)
- **MySQL/PostgreSQL** (Database)
- **ReactJS / Next.js** (Frontend – tùy chọn)

---

