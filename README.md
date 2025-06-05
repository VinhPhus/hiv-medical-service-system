
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
##  Trang Chủ & Cộng Đồng

- **Thông tin cơ sở**:  
  Giới thiệu chi tiết về cơ sở y tế, đội ngũ bác sĩ và các dịch vụ cung cấp.

- **Tài liệu giáo dục**:  
  Cung cấp các tài liệu, hướng dẫn, video về HIV/AIDS nhằm nâng cao nhận thức và giảm kỳ thị.

- **Blog chia sẻ kinh nghiệm**:  
  Nơi bệnh nhân và cộng đồng chia sẻ câu chuyện, kinh nghiệm, tạo môi trường hỗ trợ lẫn nhau.

---

##  Dành cho Bệnh nhân (Customer)

###  Đăng ký & Quản lý lịch hẹn
- Đăng ký lịch khám và điều trị HIV trực tuyến.
- Tùy chọn chỉ định bác sĩ điều trị.
- Xem, hủy hoặc thay đổi lịch hẹn đã đặt.

###  Tra cứu thông tin sức khỏe
- Xem kết quả xét nghiệm (CD4, tải lượng HIV).
- Tra cứu phác đồ ARV đang điều trị.
- Theo dõi lịch sử khám bệnh chi tiết.

###  Nhắc nhở tự động
- Hệ thống gửi nhắc nhở lịch tái khám.
- Nhắc nhở uống thuốc theo đúng phác đồ điều trị.

###  Tư vấn trực tuyến
- Đặt lịch hẹn tư vấn trực tuyến với bác sĩ.
- Tùy chọn đăng ký ẩn danh để bảo vệ sự riêng tư và giảm e ngại kỳ thị.

---

##  Dành cho Bác sĩ (Doctor)

###  Quản lý lịch làm việc
- Xem và quản lý lịch hẹn khám, tư vấn của bản thân.

###  Truy cập hồ sơ bệnh án
- Xem đầy đủ lịch sử khám bệnh và điều trị của bệnh nhân.
- Truy cập kết quả xét nghiệm chi tiết.

###  Hỗ trợ điều trị
- Hệ thống hỗ trợ lựa chọn và tùy chỉnh phác đồ ARV có sẵn (ví dụ: TDF + 3TC + DTG, phác đồ riêng cho phụ nữ mang thai, trẻ em…).
- Ghi chú chẩn đoán và cập nhật phác đồ điều trị.

---

## Quản lý & Vận hành (Manager & Admin)

###  Quản lý thông tin bác sĩ
- Thông tin cá nhân, bằng cấp, chuyên môn, lịch làm việc.

###  Quản lý hồ sơ người dùng
- Lịch sử đặt hẹn tư vấn.
- Lịch sử khám và điều trị.

###  Dashboard & Report
- Cung cấp tổng quan về hoạt động của cơ sở y tế.
- Tạo báo cáo chi tiết: số lượng bệnh nhân, lượt khám, hiệu quả điều trị, v.v.

###  Quản lý hệ thống
- Phân quyền người dùng.
- Quản lý danh mục phác đồ điều trị, các loại xét nghiệm.
- Đảm bảo an toàn và bảo mật dữ liệu hệ thống.

---

>  *Hệ thống được thiết kế nhằm tăng cường khả năng tiếp cận dịch vụ y tế HIV, nâng cao hiệu quả điều trị, và tạo ra môi trường thân thiện – không kỳ thị cho cộng đồng.*

---
##  Sơ đồ hệ thống 
![image](https://github.com/user-attachments/assets/31eb88ff-ddb9-4009-a6ec-7fe8d68483ee)<br>


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

