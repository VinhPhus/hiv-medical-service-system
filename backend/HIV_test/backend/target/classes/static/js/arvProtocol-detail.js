// Constants
const API_BASE_URL = '/api/arv-protocols';
const PROTOCOL_ID = document.getElementById('protocol-id')?.value;

// DOM Elements
const protocolDetails = document.getElementById('protocol-details');
const activateButton = document.getElementById('activate-button');
const deactivateButton = document.getElementById('deactivate-button');
const editButton = document.getElementById('edit-button');
const deleteButton = document.getElementById('delete-button');

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    loadProtocolDetails();
    setupEventListeners();
});

function setupEventListeners() {
    if (activateButton) {
        activateButton.addEventListener('click', () => toggleProtocolStatus(true));
    }
    
    if (deactivateButton) {
        deactivateButton.addEventListener('click', () => toggleProtocolStatus(false));
    }
    
    if (editButton) {
        editButton.addEventListener('click', () => {
            window.location.href = `/arvProtocol/edit/${PROTOCOL_ID}`;
        });
    }
    
    if (deleteButton) {
        deleteButton.addEventListener('click', deleteProtocol);
    }
}

async function loadProtocolDetails() {
    try {
        const response = await fetch(`${API_BASE_URL}/${PROTOCOL_ID}`);
        if (!response.ok) throw new Error('Network response was not ok');
        
        const protocol = await response.json();
        displayProtocolDetails(protocol);
        updateActionButtons(protocol.active);
        
    } catch (error) {
        console.error('Error loading protocol details:', error);
        showErrorMessage('Không thể tải thông tin phác đồ. Vui lòng thử lại sau.');
    }
}

function displayProtocolDetails(protocol) {
    if (!protocolDetails) return;

    const medicationsList = formatMedications(protocol.medications);
    const statusClass = protocol.active ? 'status-active' : 'status-inactive';
    const statusText = protocol.active ? 'Đang sử dụng' : 'Ngưng sử dụng';

    protocolDetails.innerHTML = `
        <div class="protocol-header">
            <div class="protocol-title">
                <h2>${protocol.name}</h2>
                <span class="protocol-code">${protocol.code}</span>
            </div>
            <span class="status-badge ${statusClass}">${statusText}</span>
        </div>

        <div class="protocol-section">
            <h3>Thông tin chung</h3>
            <div class="info-grid">
                <div class="info-item">
                    <span class="info-label">Đối tượng:</span>
                    <span class="info-value">${formatCategory(protocol.patientCategory)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Liều lượng:</span>
                    <span class="info-value">${protocol.dosagePerDay} lần/ngày</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Thời gian điều trị:</span>
                    <span class="info-value">${protocol.recommendedDurationWeeks} tuần</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Ngày tạo:</span>
                    <span class="info-value">${formatDate(protocol.createdAt)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Cập nhật:</span>
                    <span class="info-value">${formatDate(protocol.updatedAt)}</span>
                </div>
            </div>
        </div>

        <div class="protocol-section">
            <h3>Mô tả</h3>
            <p class="protocol-description">${protocol.description || 'Không có mô tả'}</p>
        </div>

        <div class="protocol-section">
            <h3>Thuốc sử dụng</h3>
            <div class="medications-list">
                ${medicationsList}
            </div>
        </div>

        <div class="protocol-section">
            <h3>Chống chỉ định</h3>
            <p class="protocol-contraindications">
                ${protocol.contraindications || 'Không có thông tin chống chỉ định'}
            </p>
        </div>

        <div class="protocol-section">
            <h3>Tác dụng phụ</h3>
            <p class="protocol-side-effects">
                ${protocol.sideEffects || 'Không có thông tin tác dụng phụ'}
            </p>
        </div>

        <div class="protocol-section">
            <h3>Yêu cầu theo dõi</h3>
            <p class="protocol-monitoring">
                ${protocol.monitoringRequirements || 'Không có yêu cầu theo dõi đặc biệt'}
            </p>
        </div>
    `;
}

function updateActionButtons(isActive) {
    if (activateButton) {
        activateButton.style.display = isActive ? 'none' : 'inline-block';
    }
    
    if (deactivateButton) {
        deactivateButton.style.display = isActive ? 'inline-block' : 'none';
    }
}

async function toggleProtocolStatus(activate) {
    try {
        const url = `${API_BASE_URL}/${PROTOCOL_ID}/${activate ? 'activate' : 'deactivate'}`;
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Network response was not ok');
        
        await loadProtocolDetails();
        showSuccessMessage(`Phác đồ đã được ${activate ? 'kích hoạt' : 'ngưng sử dụng'}`);
        
    } catch (error) {
        console.error('Error toggling protocol status:', error);
        showErrorMessage(`Không thể ${activate ? 'kích hoạt' : 'ngưng sử dụng'} phác đồ. Vui lòng thử lại sau.`);
    }
}

async function deleteProtocol() {
    if (!confirm('Bạn có chắc chắn muốn xóa phác đồ này không?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${PROTOCOL_ID}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('Network response was not ok');
        
        showSuccessMessage('Xóa phác đồ thành công');
        setTimeout(() => {
            window.location.href = '/arvProtocol/list';
        }, 1500);
        
    } catch (error) {
        console.error('Error deleting protocol:', error);
        showErrorMessage('Không thể xóa phác đồ. Vui lòng thử lại sau.');
    }
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

function formatMedications(medicationsJson) {
    if (!medicationsJson) return '<p>Không có thông tin thuốc</p>';

    try {
        const medications = JSON.parse(medicationsJson);
        return Object.entries(medications)
            .map(([name, dosage]) => `
                <div class="medication-item">
                    <span class="medication-name">${name}</span>
                    <span class="medication-dosage">${dosage}</span>
                </div>
            `)
            .join('');
    } catch (error) {
        console.error('Error parsing medications:', error);
        return `<p>${medicationsJson}</p>`;
    }
}

function showSuccessMessage(message) {
    // TODO: Implement success message display
    alert(message);
}

function showErrorMessage(message) {
    // TODO: Implement error message display
    alert(message);
} 