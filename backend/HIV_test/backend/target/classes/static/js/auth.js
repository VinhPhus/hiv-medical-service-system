// auth.js - JS dùng chung cho login, register, register-anonymous

// Toggle password visibility (áp dụng cho các form có input password và nút toggle)
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM đã được tải');
    // Toggle password visibility (login, register, register-anonymous)
    document.querySelectorAll('.password-toggle').forEach(function(toggle) {
        const input = toggle.closest('.form-group, .input-group, .mb-3').querySelector('input[type="password"]');
        if (input) {
            toggle.addEventListener('click', function () {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                toggle.querySelector('i').classList.toggle('fa-eye');
                toggle.querySelector('i').classList.toggle('fa-eye-slash');
            });
        }
    });

    // Tạo mã ẩn danh tự động (register-anonymous)
    const genBtn = document.getElementById('generateCodeBtn');
    const codeInput = document.getElementById('anonymousCode');
    if (genBtn && codeInput) {
        genBtn.addEventListener('click', function() {
            const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
            let code = '';
            for(let i=0; i<8; i++) code += chars.charAt(Math.floor(Math.random()*chars.length));
            codeInput.value = code;
        });
    }

    // Form validation (áp dụng cho các form có class needs-validation)
    document.querySelectorAll('.needs-validation').forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // Xử lý đăng nhập
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        console.log('Đã tìm thấy form đăng nhập');
        loginForm.addEventListener('submit', handleLogin);
    }

    // Xử lý đăng ký
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        console.log('Đã tìm thấy form đăng ký');
        registerForm.addEventListener('submit', handleRegister);
    }

    var helpBtn = document.getElementById('showAnonymousHelp');
    if (helpBtn) {
        helpBtn.addEventListener('click', function(e) {
            e.preventDefault();
            fetch('/auth/anonymous-help')
                .then(response => response.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const content = doc.body ? doc.body.innerHTML : html;
                    document.getElementById('anonymousHelpContent').innerHTML = content;
                })
                .catch(() => {
                    document.getElementById('anonymousHelpContent').innerHTML = '<div class="text-danger">Không thể tải hướng dẫn.</div>';
                });
        });
    }
});

// Hàm xử lý đăng ký
function handleRegister(event) {
    event.preventDefault();
    console.log('Form đăng ký đã được gửi');

    const form = event.target;
    const password = form.querySelector('#password').value;
    const confirmPassword = form.querySelector('#confirmPassword').value;

    // Kiểm tra mật khẩu khớp nhau
    if (password !== confirmPassword) {
        showError(form, 'Mật khẩu xác nhận không khớp');
        return;
    }

    // Kiểm tra đồng ý điều khoản
    const agreeTerms = form.querySelector('#agreeTerms');
    if (!agreeTerms.checked) {
        showError(form, 'Bạn phải đồng ý với điều khoản sử dụng');
        return;
    }

    // Hiển thị trạng thái đang tải
    const registerBtn = form.querySelector('button[type="submit"]');
    const originalBtnText = registerBtn.innerHTML;
    registerBtn.disabled = true;
    registerBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang đăng ký...';

    // Gửi form
    form.submit();
}

// Hàm xử lý đăng nhập
function handleLogin(event) {
    event.preventDefault();
    console.log('Form đăng nhập đã được gửi');
    
    const form = event.target;
    const username = form.querySelector('#username').value;
    const password = form.querySelector('#password').value;
    const rememberMe = form.querySelector('#remember-me')?.checked || false;
    
    // Kiểm tra form
    if (!username || !password) {
        showError(form, 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu');
        return;
    }

    // Hiển thị trạng thái đang tải
    const loginBtn = form.querySelector('button[type="submit"]');
    const originalBtnText = loginBtn.innerHTML;
    loginBtn.disabled = true;
    loginBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang đăng nhập...';

    // Gửi yêu cầu đăng nhập
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);
    formData.append('remember-me', rememberMe);

    fetch(form.action, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.redirected) {
            window.location.href = response.url;
            return;
        }
        return response.text();
    })
    .then(html => {
        if (html) {
            // Phân tích phản hồi HTML
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            // Kiểm tra thông báo lỗi
            const errorElement = doc.querySelector('.alert-danger');
            if (errorElement) {
                showError(form, errorElement.textContent.trim());
                loginBtn.disabled = false;
                loginBtn.innerHTML = originalBtnText;
            } else {
                // Nếu không tìm thấy thông báo lỗi, hiển thị lỗi chung
                showError(form, 'Đăng nhập không thành công. Vui lòng kiểm tra lại tên đăng nhập và mật khẩu.');
                loginBtn.disabled = false;
                loginBtn.innerHTML = originalBtnText;
            }
        }
    })
    .catch(error => {
        console.error('Lỗi đăng nhập:', error);
        showError(form, 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.');
        loginBtn.disabled = false;
        loginBtn.innerHTML = originalBtnText;
    });
}

// Hiển thị thông báo lỗi
function showError(form, message) {
    // Xóa các thông báo lỗi hiện có
    const existingErrors = form.querySelectorAll('.alert-danger');
    existingErrors.forEach(error => error.remove());

    // Tạo thông báo lỗi mới
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger alert-dismissible fade show';
    errorDiv.setAttribute('role', 'alert');
    errorDiv.innerHTML = `
        <i class="fas fa-exclamation-circle me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
    `;

    // Chèn vào đầu form
    form.insertBefore(errorDiv, form.firstChild);

    // Cuộn đến thông báo lỗi
    errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Hàm xử lý chuyển hướng theo role
function getRedirectUrlByRole(role) {
    role = (role || 'PATIENT').toUpperCase();
    const roleMap = {
        'ADMIN': 'admin',
        'DOCTOR': 'doctor',
        'STAFF': 'staff',
        'MANAGER': 'manager',
        'PATIENT': 'patient'
    };
    return `/dashboard/${roleMap[role] || 'patient'}`;
}

// Hàm hiển thị thông báo lỗi
function showErrorAlert(message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show';
    alertDiv.innerHTML = `
        <i class="fas fa-exclamation-circle me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.querySelector('.card').insertBefore(alertDiv, document.getElementById('loginForm'));
}

// Khởi tạo form đăng nhập
function initLoginForm() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        try {
            const response = await login(username, password);
            console.log('Login successful:', response);
            window.location.href = getRedirectUrlByRole(response.role);
        } catch (error) {
            console.error('Login error:', error);
            showErrorAlert('Đăng nhập thất bại! Vui lòng kiểm tra lại tên đăng nhập và mật khẩu.');
        }
    });
}

// Khởi tạo khi trang load
document.addEventListener('DOMContentLoaded', function() {
    initLoginForm();
});
