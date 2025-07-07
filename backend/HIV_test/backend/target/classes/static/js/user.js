// user.js

// Đăng nhập và lưu JWT
async function login(username, password) {
    try {
        const res = await fetch('/api/auth/signin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!res.ok) {
            throw new Error('Đăng nhập thất bại');
        }
        
        const data = await res.json();
        console.log('Login response:', data); // Log toàn bộ response để debug
        
        // Backend trả về token trong data.accessToken hoặc data.token
        const token = data.accessToken || data.token;
        if (!token) {
            throw new Error('Không nhận được token từ server');
        }
        
        // Lưu token và thông tin user
        localStorage.setItem('accessToken', token);
        localStorage.setItem('refreshToken', data.refreshToken || '');
        localStorage.setItem('userRole', data.role || '');
        localStorage.setItem('userId', data.id || '');
        
        return data;
    } catch (error) {
        console.error('Login error:', error);
        throw error;
    }
}

// Lấy JWT từ localStorage
function getToken() {
    return localStorage.getItem('accessToken');
}

// Gọi API với JWT trong header
async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    if (!token) {
        console.warn('No token found, redirecting to login...');
        window.location.href = '/auth/login';
        return;
    }

    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
        ...options.headers
    };

    console.log('Calling API:', url); // Log URL
    console.log('With headers:', headers); // Log headers

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        if (response.status === 401) {
            console.warn('Token expired or invalid, redirecting to login...');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userId');
            window.location.href = '/auth/login';
            return;
        }

        return response;
    } catch (error) {
        console.error('API call error:', error);
        throw error;
    }
}

// Đăng xuất
function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    window.location.href = '/auth/login';
}

// Export các hàm
window.login = login;
window.getToken = getToken;
window.fetchWithAuth = fetchWithAuth;
window.logout = logout; 