// Constants
const API_BASE_URL = '/api/arv-protocols';

// DOM Elements
const protocolList = document.getElementById('protocol-list');
const searchForm = document.getElementById('search-form');
const searchInput = document.getElementById('search-input');
const categoryFilter = document.getElementById('category-filter');
const statusFilter = document.getElementById('status-filter');
const sortSelect = document.getElementById('sort-select');
const pagination = document.getElementById('pagination');

// State
let currentPage = 0;
const pageSize = 10;
let totalPages = 0;
let currentSort = 'createdAt,desc';
let currentSearch = '';
let currentCategory = '';
let currentStatus = '';

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    loadProtocols();
    setupEventListeners();
});

function setupEventListeners() {
    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            currentPage = 0;
            loadProtocols();
        });
    }

    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => {
            currentPage = 0;
            loadProtocols();
        });
    }

    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            currentPage = 0;
            loadProtocols();
        });
    }

    if (sortSelect) {
        sortSelect.addEventListener('change', () => {
            currentPage = 0;
            loadProtocols();
        });
    }
}

// API Calls
async function loadProtocols() {
    try {
        const searchTerm = searchInput ? searchInput.value : '';
        const category = categoryFilter ? categoryFilter.value : '';
        const status = statusFilter ? statusFilter.value : '';
        const sort = sortSelect ? sortSelect.value : currentSort;

        const queryParams = new URLSearchParams({
            page: currentPage,
            size: pageSize,
            sortBy: sort.split(',')[0],
            sortDir: sort.split(',')[1],
            search: searchTerm,
            category: category,
            status: status
        });

        const response = await fetch(`${API_BASE_URL}?${queryParams}`);
        if (!response.ok) throw new Error('Network response was not ok');
        
        const data = await response.json();
        displayProtocols(data.content);
        updatePagination(data);
        
    } catch (error) {
        console.error('Error loading protocols:', error);
        showErrorMessage('Không thể tải danh sách phác đồ. Vui lòng thử lại sau.');
    }
}

function displayProtocols(protocols) {
    if (!protocolList) return;

    protocolList.innerHTML = protocols.map(protocol => `
        <div class="protocol-card">
            <div class="protocol-header">
                <div>
                    <span class="protocol-id">${protocol.code}</span>
                    <h5 class="mb-0">${protocol.name}</h5>
                    <span class="protocol-date">Cập nhật: ${formatDate(protocol.updatedAt)}</span>
                </div>
                <span class="status-badge ${getStatusClass(protocol.active)}">
                    ${protocol.active ? 'Đang sử dụng' : 'Ngưng sử dụng'}
                </span>
            </div>
            
            <div class="protocol-details">
                <div class="detail-item">
                    <i class="fas fa-users"></i>
                    <span>Đối tượng: ${formatCategory(protocol.patientCategory)}</span>
                </div>
                <div class="detail-item">
                    <i class="fas fa-pills"></i>
                    <span>Liều lượng: ${protocol.dosagePerDay}/ngày</span>
                </div>
                <div class="detail-item">
                    <i class="fas fa-calendar-alt"></i>
                    <span>Thời gian: ${protocol.recommendedDurationWeeks} tuần</span>
                </div>
            </div>

            <div class="action-buttons">
                <button class="btn btn-sm btn-outline-primary" onclick="viewProtocol(${protocol.id})">
                    <i class="fas fa-eye"></i> Chi tiết
                </button>
                ${hasEditPermission() ? `
                    <button class="btn btn-sm btn-outline-primary" onclick="editProtocol(${protocol.id})">
                        <i class="fas fa-edit"></i> Chỉnh sửa
                    </button>
                ` : ''}
                ${hasDeletePermission() ? `
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteProtocol(${protocol.id})">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                ` : ''}
            </div>
        </div>
    `).join('');
}

function updatePagination(data) {
    if (!pagination) return;

    totalPages = data.totalPages;
    
    let paginationHtml = '';
    
    // Previous button
    paginationHtml += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">
                <i class="fas fa-chevron-left"></i>
            </a>
        </li>
    `;

    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === currentPage) {
            paginationHtml += `
                <li class="page-item active">
                    <span class="page-link">${i + 1}</span>
                </li>
            `;
        } else if (i === 0 || i === totalPages - 1 || Math.abs(i - currentPage) <= 2) {
            paginationHtml += `
                <li class="page-item">
                    <a class="page-link" href="#" onclick="changePage(${i})">${i + 1}</a>
                </li>
            `;
        } else if (Math.abs(i - currentPage) === 3) {
            paginationHtml += `
                <li class="page-item disabled">
                    <span class="page-link">...</span>
                </li>
            `;
        }
    }

    // Next button
    paginationHtml += `
        <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">
                <i class="fas fa-chevron-right"></i>
            </a>
        </li>
    `;

    pagination.innerHTML = paginationHtml;
}

// Utility Functions
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCategory(category) {
    const categories = {
        ADULT: 'Người lớn',
        CHILD: 'Trẻ em',
        PREGNANT: 'Phụ nữ mang thai',
        SPECIAL: 'Đối tượng đặc biệt'
    };
    return categories[category] || category;
}

function getStatusClass(active) {
    return active ? 'status-active' : 'status-inactive';
}

function hasEditPermission() {
    // TODO: Implement based on user role
    return true;
}

function hasDeletePermission() {
    // TODO: Implement based on user role
    return true;
}

function showErrorMessage(message) {
    // TODO: Implement error message display
    alert(message);
}

// Navigation Functions
function viewProtocol(id) {
    window.location.href = `/arvProtocol/detail/${id}`;
}

function editProtocol(id) {
    window.location.href = `/arvProtocol/edit/${id}`;
}

async function deleteProtocol(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa phác đồ này không?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Network response was not ok');
        
        loadProtocols();
        showSuccessMessage('Xóa phác đồ thành công');
        
    } catch (error) {
        console.error('Error deleting protocol:', error);
        showErrorMessage('Không thể xóa phác đồ. Vui lòng thử lại sau.');
    }
}

function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadProtocols();
}

function showSuccessMessage(message) {
    // TODO: Implement success message display
    alert(message);
} 