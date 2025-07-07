// Khi bấm vào logo hoặc Trang chủ sẽ về index.html

document.addEventListener('DOMContentLoaded', function() {
    // Logo HIV Care
    var logo = document.querySelector('.navbar-brand');
    if (logo) {
        logo.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/';
        });
    }
    // Link Trang chủ, Kết quả xét nghiệm, Đăng nhập, các mục cần đăng nhập
    var homeLinks = document.querySelectorAll('.nav-link, .btn');
    homeLinks.forEach(function(link) {
        var text = link.textContent.trim();
        if (text === 'Trang chủ') {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                window.location.href = '/';
            });
        }
        if (text === 'Kết quả xét nghiệm') {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                //if (window.isLoggedIn) {
                    window.location.href = '/testResult/list';
                //} else {
                    //showLoginRequired();}
            });
        }
        if (text === 'Hồ sơ y tế') {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                //if (window.isLoggedIn) {
                    window.location.href = '/medicalRecord/list';
                //} else {
                    //showLoginRequired();}
            });
        }
        if (text === 'Phác đồ ARV') {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                //if (window.isLoggedIn) {
                    window.location.href = '/arvProtocol/list';
                //} else {
                    //showLoginRequired();}
            });
        }
        if (text === 'Lịch hẹn') {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                //if (window.isLoggedIn) {
                    window.location.href = '/appointment/list';
                //} else {
                    //showLoginRequired();
                //}
            });
        }
        if (text === 'Đăng nhập') {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                window.location.href = '/auth/login';
            });
        }
    });
});

function showLoginRequired() {
    // Nếu đã có thông báo thì không tạo thêm
    if (document.getElementById('login-required-alert')) return;
    var alert = document.createElement('div');
    alert.id = 'login-required-alert';
    alert.className = 'alert alert-warning position-fixed top-0 start-50 translate-middle-x mt-3';
    alert.style.zIndex = 9999;
    alert.innerText = 'Bạn cần đăng nhập để truy cập chức năng này.';
    document.body.appendChild(alert);
    setTimeout(function() {
        alert.remove();
    }, 2500);
}
