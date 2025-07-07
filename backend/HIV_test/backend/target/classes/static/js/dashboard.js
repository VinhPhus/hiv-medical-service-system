// dashboard.js

// Hàm gọi API với xác thực
function fetchWithAuth(url, options = {}) {
    options.headers = {
        ...options.headers,
        'Content-Type': 'application/json'
    };
    return fetch(url, options);
}

// Xác định vai trò dựa trên URL hoặc element trên trang
const path = window.location.pathname;

// Hàm cập nhật thống kê dashboard bác sĩ
function updateDoctorDashboard() {
    console.log('Đang cập nhật thống kê bác sĩ...');
    fetchWithAuth('/api/dashboard/doctor')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Dữ liệu nhận được:', data);
            
            // Cập nhật số bệnh nhân
            const totalPatientsElement = document.querySelector('[data-type="patients"]');
            if (totalPatientsElement) {
                totalPatientsElement.textContent = data.totalPatients || 0;
            }
            
            // Cập nhật số lịch hẹn hôm nay
            const appointmentsElement = document.querySelector('[data-type="appointments"]');
            if (appointmentsElement) {
                appointmentsElement.textContent = data.todayAppointments?.length || 0;
            }
            
            // Cập nhật số hồ sơ y tế
            const recordsElement = document.querySelector('[data-type="records"]');
            if (recordsElement) {
                recordsElement.textContent = data.totalMedicalRecords || 0;
            }
            
            // Cập nhật số xét nghiệm chờ xử lý
            const testsElement = document.querySelector('[data-type="tests"]');
            if (testsElement) {
                testsElement.textContent = data.pendingTests?.length || 0;
            }
        })
        .catch(error => {
            console.error('Lỗi khi cập nhật thống kê:', error);
        });
}

// Khởi tạo các chức năng khi trang được tải
document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra loại dashboard hiện tại
    if (path.includes('/dashboard/doctor')) {
        console.log('Khởi tạo dashboard bác sĩ...');
        updateDoctorDashboard();
        // Cập nhật thống kê mỗi 30 giây
        setInterval(updateDoctorDashboard, 30000);
    }
    
    if (path.includes("/dashboard/patient")) {
        // Lịch hẹn cá nhân
        fetchWithAuth("/api/appointments/my-appointments")
            .then((res) => res.json())
            .then((data) => {
                // TODO: Xử lý dữ liệu lịch hẹn ở đây
                console.log("Lịch hẹn:", data);
            })
            .catch((err) => {
                console.error("Không lấy được dữ liệu lịch hẹn:", err);
            });
    }

    if (path.includes("/dashboard/admin")) {
        // Thống kê hệ thống
        fetchWithAuth("/api/dashboard/stats")
            .then((res) => res.json())
            .then((data) => {
                // TODO: Xử lý dữ liệu thống kê ở đây
                console.log("Thống kê:", data);
            })
            .catch((err) => {
                console.error("Không lấy được dữ liệu thống kê:", err);
            });
    }

    if (path.includes("/dashboard/manager")) {
        // Danh sách nhân viên quản lý
        fetchWithAuth("/api/manager/my-staff")
            .then((res) => res.json())
            .then((data) => {
                // TODO: Xử lý dữ liệu nhân viên ở đây
                console.log("Nhân viên:", data);
            })
            .catch((err) => {
                console.error("Không lấy được dữ liệu nhân viên:", err);
            });
    }

    if (path.includes("/dashboard/staff")) {
        // Danh sách công việc của nhân viên
        fetchWithAuth("/api/staff/my-tasks")
            .then((res) => res.json())
            .then((data) => {
                // TODO: Xử lý dữ liệu công việc ở đây
                console.log("Công việc:", data);
            })
            .catch((err) => {
                console.error("Không lấy được dữ liệu công việc:", err);
            });
    }
}); 