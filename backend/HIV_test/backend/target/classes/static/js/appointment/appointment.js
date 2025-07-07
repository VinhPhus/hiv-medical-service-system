// Hàm kiểm tra form trước khi submit
function validateAppointmentForm() {
    let isValid = true;
    const requiredFields = ['patient', 'doctor', 'appointmentDate', 'appointmentTime', 'appointmentType'];

    // Kiểm tra các trường bắt buộc
    requiredFields.forEach(field => {
        const element = document.getElementById(field);
        if (!element.value) {
            isValid = false;
            element.classList.add('is-invalid');
            showError(element, 'Trường này là bắt buộc');
        } else {
            element.classList.remove('is-invalid');
            hideError(element);
        }
    });

    // Kiểm tra ngày hẹn không được trong quá khứ
    const appointmentDate = document.getElementById('appointmentDate');
    const selectedDate = new Date(appointmentDate.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (selectedDate < today) {
        isValid = false;
        appointmentDate.classList.add('is-invalid');
        showError(appointmentDate, 'Ngày hẹn không thể trong quá khứ');
    }

    return isValid;
}

// Hiển thị thông báo lỗi
function showError(element, message) {
    let feedback = element.nextElementSibling;
    if (!feedback || !feedback.classList.contains('invalid-feedback')) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        element.parentNode.insertBefore(feedback, element.nextSibling);
    }
    feedback.textContent = message;
}

// Ẩn thông báo lỗi
function hideError(element) {
    const feedback = element.nextElementSibling;
    if (feedback && feedback.classList.contains('invalid-feedback')) {
        feedback.textContent = '';
    }
}

// Xử lý sự kiện submit form
document.getElementById('appointmentForm')?.addEventListener('submit', function(e) {
    if (!validateAppointmentForm()) {
        e.preventDefault();
        return false;
    }
});

// Xử lý sự kiện input để xóa thông báo lỗi khi người dùng bắt đầu nhập
document.querySelectorAll('.form-control, .form-select').forEach(element => {
    element.addEventListener('input', function() {
        this.classList.remove('is-invalid');
        hideError(this);
    });
});

// Hàm kiểm tra xem bác sĩ có lịch trống không
async function checkDoctorAvailability() {
    const doctorId = document.getElementById('doctor').value;
    const appointmentDate = document.getElementById('appointmentDate').value;
    const appointmentTime = document.getElementById('appointmentTime').value;

    if (!doctorId || !appointmentDate || !appointmentTime) return;

    try {
        const response = await fetch(`/api/appointments/check-availability?doctorId=${doctorId}&date=${appointmentDate}&time=${appointmentTime}`);
        const data = await response.json();

        const availabilityMessage = document.getElementById('availabilityMessage');
        if (data.available) {
            availabilityMessage.className = 'text-success';
            availabilityMessage.textContent = 'Bác sĩ có lịch trống vào thời gian này';
        } else {
            availabilityMessage.className = 'text-danger';
            availabilityMessage.textContent = 'Bác sĩ đã có lịch hẹn khác vào thời gian này';
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra lịch trống:', error);
    }
}

// Thêm sự kiện kiểm tra lịch trống khi thay đổi bác sĩ hoặc thời gian
document.getElementById('doctor')?.addEventListener('change', checkDoctorAvailability);
document.getElementById('appointmentDate')?.addEventListener('change', checkDoctorAvailability);
document.getElementById('appointmentTime')?.addEventListener('change', checkDoctorAvailability);

// Hàm xóa lịch hẹn
async function deleteAppointment(appointmentId) {
    if (!confirm('Bạn có chắc chắn muốn xóa lịch hẹn này không?')) return;

    try {
        const response = await fetch(`/appointment/${appointmentId}/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            // Xóa dòng khỏi bảng
            const row = document.querySelector(`tr[data-appointment-id="${appointmentId}"]`);
            if (row) {
                row.remove();
                showToast('success', 'Đã xóa lịch hẹn thành công');
            }
            
            // Kiểm tra nếu không còn lịch hẹn nào thì hiển thị empty state
            const tbody = document.querySelector('.appointment-table tbody');
            if (tbody && tbody.children.length === 0) {
                showEmptyState();
            }
        } else {
            showToast('error', 'Có lỗi xảy ra khi xóa lịch hẹn');
        }
    } catch (error) {
        console.error('Lỗi khi xóa lịch hẹn:', error);
        showToast('error', 'Có lỗi xảy ra khi xóa lịch hẹn');
    }
}

// Hàm thay đổi trạng thái lịch hẹn
async function changeStatus(appointmentId, currentStatus) {
    const newStatus = prompt('Nhập trạng thái mới (SCHEDULED, COMPLETED, CANCELLED):', currentStatus);
    if (!newStatus || !['SCHEDULED', 'COMPLETED', 'CANCELLED'].includes(newStatus.toUpperCase())) {
        showToast('error', 'Trạng thái không hợp lệ');
        return;
    }

    try {
        const response = await fetch(`/appointment/${appointmentId}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: newStatus.toUpperCase() })
        });

        if (response.ok) {
            // Cập nhật badge trạng thái
            const statusBadge = document.querySelector(`tr[data-appointment-id="${appointmentId}"] .status-badge`);
            if (statusBadge) {
                statusBadge.className = `status-badge status-${newStatus.toLowerCase()}`;
                statusBadge.innerHTML = `
                    <i class="fas fa-circle"></i>
                    ${getStatusText(newStatus)}
                `;
                showToast('success', 'Đã cập nhật trạng thái thành công');
            }
        } else {
            showToast('error', 'Có lỗi xảy ra khi cập nhật trạng thái');
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật trạng thái:', error);
        showToast('error', 'Có lỗi xảy ra khi cập nhật trạng thái');
    }
}

// Hàm lấy text hiển thị cho trạng thái
function getStatusText(status) {
    const statusMap = {
        'SCHEDULED': 'Đã lên lịch',
        'COMPLETED': 'Đã hoàn thành',
        'CANCELLED': 'Đã hủy'
    };
    return statusMap[status] || status;
}

// Hàm hiển thị empty state
function showEmptyState() {
    const tbody = document.querySelector('.appointment-table tbody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-state">
                    <i class="fas fa-calendar-times"></i>
                    <h3>Không có lịch hẹn nào</h3>
                    <p>Chưa có lịch hẹn nào được tạo hoặc không tìm thấy kết quả phù hợp.</p>
                    <a href="/appointment/form" class="btn btn-appointment btn-appointment-primary">
                        <i class="fas fa-plus me-2"></i>Tạo lịch hẹn mới
                    </a>
                </td>
            </tr>
        `;
    }
}

// Hàm hiển thị toast message
function showToast(type, message) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type} show`;
    toast.innerHTML = `
        <div class="toast-header">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2"></i>
            <strong class="me-auto">${type === 'success' ? 'Thành công' : 'Lỗi'}</strong>
            <button type="button" class="btn-close" onclick="this.parentElement.parentElement.remove()"></button>
        </div>
        <div class="toast-body">${message}</div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Hàm tìm kiếm và lọc
function filterAppointments() {
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const typeFilter = document.getElementById('typeFilter');
    const dateFilter = document.getElementById('dateFilter');

    const searchTerm = searchInput?.value.toLowerCase() || '';
    const statusValue = statusFilter?.value || '';
    const typeValue = typeFilter?.value || '';
    const dateValue = dateFilter?.value || '';

    const rows = document.querySelectorAll('.appointment-table tbody tr:not(.empty-state)');
    let hasVisibleRows = false;

    rows.forEach(row => {
        const patientName = row.querySelector('[data-patient]')?.textContent.toLowerCase() || '';
        const doctorName = row.querySelector('[data-doctor]')?.textContent.toLowerCase() || '';
        const status = row.querySelector('[data-status]')?.dataset.status || '';
        const type = row.querySelector('[data-type]')?.dataset.type || '';
        const date = row.querySelector('[data-date]')?.dataset.date || '';

        const matchesSearch = patientName.includes(searchTerm) || doctorName.includes(searchTerm);
        const matchesStatus = !statusValue || status === statusValue;
        const matchesType = !typeValue || type === typeValue;
        const matchesDate = !dateValue || date === dateValue;

        const isVisible = matchesSearch && matchesStatus && matchesType && matchesDate;
        row.style.display = isVisible ? '' : 'none';
        if (isVisible) hasVisibleRows = true;
    });

    // Hiển thị empty state nếu không có kết quả
    if (!hasVisibleRows) {
        showEmptyState();
    }
}

// Thêm event listeners cho các filter
document.getElementById('searchInput')?.addEventListener('input', filterAppointments);
document.getElementById('statusFilter')?.addEventListener('change', filterAppointments);
document.getElementById('typeFilter')?.addEventListener('change', filterAppointments);
document.getElementById('dateFilter')?.addEventListener('change', filterAppointments);

// Khởi tạo các thành phần khi trang được tải
document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra nếu không có lịch hẹn nào thì hiển thị empty state
    const tbody = document.querySelector('.appointment-table tbody');
    if (tbody && tbody.children.length === 0) {
        showEmptyState();
    }

    // Khởi tạo các thành phần khác nếu cần
    // Form validation
    const appointmentForm = document.getElementById('appointmentForm');
    if (appointmentForm) {
        appointmentForm.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
            }
        });
    }

    // Initialize filters
    initializeFilters();

    // Initialize action buttons
    initializeActionButtons();

    // Initialize doctor availability check
    initializeDoctorAvailabilityCheck();

    // Khởi tạo form khi trang được tải
    initializeAppointmentForm();
});

function validateForm() {
    let isValid = true;
    const requiredFields = document.querySelectorAll('.appointment-form-control[required]');
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.classList.add('is-invalid');
            isValid = false;
        } else {
            field.classList.remove('is-invalid');
        }
    });

    // Validate date
    const dateField = document.querySelector('input[type="date"]');
    if (dateField) {
        const selectedDate = new Date(dateField.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (selectedDate < today) {
            dateField.classList.add('is-invalid');
            showToast('Ngày hẹn không thể là ngày trong quá khứ', 'error');
            isValid = false;
        }
    }

    return isValid;
}

function initializeFilters() {
    const searchInput = document.querySelector('.appointment-search input');
    const statusFilter = document.querySelector('.appointment-filter[name="status"]');
    const typeFilter = document.querySelector('.appointment-filter[name="type"]');

    if (searchInput) {
        searchInput.addEventListener('input', debounce(filterAppointments, 300));
    }

    if (statusFilter) {
        statusFilter.addEventListener('change', filterAppointments);
    }

    if (typeFilter) {
        typeFilter.addEventListener('change', filterAppointments);
    }
}

function filterAppointments() {
    const searchTerm = document.querySelector('.appointment-search input')?.value.toLowerCase();
    const statusFilter = document.querySelector('.appointment-filter[name="status"]')?.value;
    const typeFilter = document.querySelector('.appointment-filter[name="type"]')?.value;

    const rows = document.querySelectorAll('.appointment-table tbody tr');

    rows.forEach(row => {
        let showRow = true;

        // Search filter
        if (searchTerm) {
            const text = row.textContent.toLowerCase();
            showRow = text.includes(searchTerm);
        }

        // Status filter
        if (showRow && statusFilter) {
            const status = row.querySelector('.appointment-status')?.textContent.trim().toLowerCase();
            showRow = status === statusFilter.toLowerCase();
        }

        // Type filter
        if (showRow && typeFilter) {
            const type = row.querySelector('td:nth-child(5)')?.textContent.trim().toLowerCase();
            showRow = type === typeFilter.toLowerCase();
        }

        row.style.display = showRow ? '' : 'none';
    });

    updateEmptyState();
}

function updateEmptyState() {
    const tbody = document.querySelector('.appointment-table tbody');
    const visibleRows = tbody.querySelectorAll('tr:not([style*="display: none"])');
    const emptyState = document.querySelector('.appointment-empty');

    if (visibleRows.length === 0) {
        if (!emptyState) {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td colspan="7">
                    <div class="appointment-empty">
                        <i class="fas fa-calendar-times"></i>
                        <p>Không tìm thấy lịch hẹn nào</p>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        }
    } else if (emptyState) {
        emptyState.closest('tr').remove();
    }
}

function initializeActionButtons() {
    // View action
    document.querySelectorAll('.appointment-action-view').forEach(btn => {
        btn.addEventListener('click', function() {
            const appointmentId = this.closest('tr').querySelector('td:first-child').textContent;
            window.location.href = `/appointment/detail/${appointmentId}`;
        });
    });

    // Edit action
    document.querySelectorAll('.appointment-action-edit').forEach(btn => {
        btn.addEventListener('click', function() {
            const appointmentId = this.closest('tr').querySelector('td:first-child').textContent;
            window.location.href = `/appointment/form/${appointmentId}`;
        });
    });

    // Delete action
    document.querySelectorAll('.appointment-action-delete').forEach(btn => {
        btn.addEventListener('click', function() {
            const appointmentId = this.closest('tr').querySelector('td:first-child').textContent;
            if (confirm('Bạn có chắc chắn muốn xóa lịch hẹn này?')) {
                deleteAppointment(appointmentId);
            }
        });
    });
}

function initializeDoctorAvailabilityCheck() {
    const doctorSelect = document.querySelector('select[name="doctorId"]');
    const dateInput = document.querySelector('input[type="date"]');
    const timeInput = document.querySelector('input[type="time"]');

    if (doctorSelect && dateInput && timeInput) {
        const checkAvailability = debounce(() => {
            const doctorId = doctorSelect.value;
            const date = dateInput.value;
            const time = timeInput.value;

            if (doctorId && date && time) {
                fetch(`/api/appointments/check-availability?doctorId=${doctorId}&date=${date}&time=${time}`)
                    .then(response => response.json())
                    .then(data => {
                        const message = document.getElementById('availabilityMessage');
                        if (message) {
                            message.textContent = data.available 
                                ? 'Bác sĩ có lịch trống vào thời gian này' 
                                : 'Bác sĩ đã có lịch hẹn khác vào thời gian này';
                            message.className = data.available ? 'text-success' : 'text-danger';
                        }
                    })
                    .catch(error => console.error('Error:', error));
            }
        }, 300);

        doctorSelect.addEventListener('change', checkAvailability);
        dateInput.addEventListener('change', checkAvailability);
        timeInput.addEventListener('change', checkAvailability);
    }
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function initializeAppointmentForm() {
    const form = document.getElementById('appointmentForm');
    if (!form) return;

    // Thêm validation khi submit form
    form.addEventListener('submit', function(e) {
        if (!validateAppointmentForm()) {
            e.preventDefault();
            return false;
        }
    });

    // Thêm validation realtime cho các trường
    const fields = form.querySelectorAll('input, select');
    fields.forEach(field => {
        field.addEventListener('change', function() {
            validateField(this);
        });
    });

    // Khởi tạo các ràng buộc cho ngày và giờ
    initializeDateTimeConstraints();
}

function validateField(field) {
    let isValid = true;
    field.classList.remove('is-invalid', 'is-valid');

    // Kiểm tra trường bắt buộc
    if (field.hasAttribute('required') && !field.value.trim()) {
        field.classList.add('is-invalid');
        isValid = false;
    }

    // Kiểm tra select
    if (field.tagName === 'SELECT' && field.value === '') {
        field.classList.add('is-invalid');
        isValid = false;
    }

    // Nếu hợp lệ
    if (isValid) {
        field.classList.add('is-valid');
    }

    return isValid;
}

function validateDateTime() {
    const dateField = document.querySelector('input[type="date"]');
    const timeField = document.querySelector('input[type="time"]');
    let isValid = true;

    if (dateField && timeField) {
        const selectedDate = new Date(dateField.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // Kiểm tra ngày không trong quá khứ
        if (selectedDate < today) {
            dateField.classList.add('is-invalid');
            isValid = false;
        }

        // Kiểm tra giờ làm việc (8:00 - 17:00)
        if (timeField.value) {
            const [hours, minutes] = timeField.value.split(':').map(Number);
            const time = hours * 60 + minutes;
            const workStart = 8 * 60;  // 8:00
            const workEnd = 17 * 60;   // 17:00

            if (time < workStart || time > workEnd) {
                timeField.classList.add('is-invalid');
                isValid = false;
            }
        }
    }

    return isValid;
}

function initializeDateTimeConstraints() {
    const dateField = document.querySelector('input[type="date"]');
    const timeField = document.querySelector('input[type="time"]');

    if (dateField) {
        // Set ngày tối thiểu là ngày hiện tại
        const today = new Date().toISOString().split('T')[0];
        dateField.setAttribute('min', today);
    }

    if (timeField) {
        // Set giờ làm việc
        timeField.setAttribute('min', '08:00');
        timeField.setAttribute('max', '17:00');
    }
}

// Hiển thị thông báo
function showNotification(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        <i class="fas fa-info-circle me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    const container = document.querySelector('.appointment-container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);
    }

    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
} 