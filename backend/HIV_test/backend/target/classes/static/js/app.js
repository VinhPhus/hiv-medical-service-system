// Các hàm tiện ích cho frontend
function fetchWithAuth(url, options = {}) {
  // Thêm headers mặc định nếu cần
  options.headers = {
    ...options.headers,
    'Content-Type': 'application/json'
  };
  
  return fetch(url, options);
}

// Ví dụ gọi API lấy phác đồ cá nhân
fetchWithAuth('/api/arv-protocols/my-protocols')
  .then(res => res.json())
  .then(data => {
    // TODO: Xử lý dữ liệu phác đồ ở đây
    console.log('Phác đồ:', data);
  })
  .catch(err => {
    console.error('Không lấy được dữ liệu phác đồ:', err);
  });

// Hàm cập nhật thống kê dashboard bác sĩ
function updateDoctorDashboard() {
    fetchWithAuth('/api/dashboard/doctor')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
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
    // Kiểm tra nếu đang ở trang dashboard bác sĩ
    if (window.location.pathname.includes('/dashboard/doctor')) {
        updateDoctorDashboard();
        // Cập nhật thống kê mỗi 30 giây
        setInterval(updateDoctorDashboard, 30000);
    }
});
