// Constants
const API_BASE_URL = '/api/arv-protocols';
const PROTOCOL_ID = document.getElementById('protocol-id')?.value;
const IS_EDIT_MODE = !!PROTOCOL_ID;

// DOM Elements
const protocolForm = document.getElementById('protocol-form');
const nameInput = document.getElementById('name');
const codeInput = document.getElementById('code');
const descriptionInput = document.getElementById('description');
const categorySelect = document.getElementById('category');
const medicationsInput = document.getElementById('medications');
const dosageInput = document.getElementById('dosage');
const durationInput = document.getElementById('duration');
const contraindicationsInput = document.getElementById('contraindications');
const sideEffectsInput = document.getElementById('side-effects');
const monitoringInput = document.getElementById('monitoring');
const activeSwitch = document.getElementById('active');

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    setupForm();
    if (IS_EDIT_MODE) {
        loadProtocolData();
    }
});

function setupForm() {
    if (!protocolForm) return;

    protocolForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (validateForm()) {
            await saveProtocol();
        }
    });

    // Real-time validation
    if (codeInput) {
        codeInput.addEventListener('blur', validateCode);
    }
}

async function loadProtocolData() {
    try {
        const response = await fetch(`${API_BASE_URL}/${PROTOCOL_ID}`);
        if (!response.ok) throw new Error('Network response was not ok');
        
        const protocol = await response.json();
        populateForm(protocol);
        
    } catch (error) {
        console.error('Error loading protocol data:', error);
        showErrorMessage('Không thể tải thông tin phác đồ. Vui lòng thử lại sau.');
    }
}

function populateForm(protocol) {
    nameInput.value = protocol.name || '';
    codeInput.value = protocol.code || '';
    descriptionInput.value = protocol.description || '';
    categorySelect.value = protocol.patientCategory || '';
    medicationsInput.value = protocol.medications || '';
    dosageInput.value = protocol.dosagePerDay || '';
    durationInput.value = protocol.recommendedDurationWeeks || '';
    contraindicationsInput.value = protocol.contraindications || '';
    sideEffectsInput.value = protocol.sideEffects || '';
    monitoringInput.value = protocol.monitoringRequirements || '';
    activeSwitch.checked = protocol.active;
}

async function saveProtocol() {
    try {
        const protocolData = {
            name: nameInput.value,
            code: codeInput.value,
            description: descriptionInput.value,
            patientCategory: categorySelect.value,
            medications: medicationsInput.value,
            dosagePerDay: parseInt(dosageInput.value),
            recommendedDurationWeeks: parseInt(durationInput.value),
            contraindications: contraindicationsInput.value,
            sideEffects: sideEffectsInput.value,
            monitoringRequirements: monitoringInput.value,
            active: activeSwitch.checked
        };

        const url = IS_EDIT_MODE ? `${API_BASE_URL}/${PROTOCOL_ID}` : API_BASE_URL;
        const method = IS_EDIT_MODE ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(protocolData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Network response was not ok');
        }

        showSuccessMessage(IS_EDIT_MODE ? 'Cập nhật phác đồ thành công' : 'Tạo phác đồ mới thành công');
        setTimeout(() => {
            window.location.href = '/arvProtocol/list';
        }, 1500);

    } catch (error) {
        console.error('Error saving protocol:', error);
        showErrorMessage(IS_EDIT_MODE ? 
            'Không thể cập nhật phác đồ. Vui lòng thử lại sau.' : 
            'Không thể tạo phác đồ mới. Vui lòng thử lại sau.');
    }
}

// Validation Functions
function validateForm() {
    let isValid = true;

    // Required fields
    if (!nameInput.value.trim()) {
        showFieldError(nameInput, 'Vui lòng nhập tên phác đồ');
        isValid = false;
    }

    if (!codeInput.value.trim()) {
        showFieldError(codeInput, 'Vui lòng nhập mã phác đồ');
        isValid = false;
    }

    if (!categorySelect.value) {
        showFieldError(categorySelect, 'Vui lòng chọn đối tượng áp dụng');
        isValid = false;
    }

    if (!medicationsInput.value.trim()) {
        showFieldError(medicationsInput, 'Vui lòng nhập thông tin thuốc');
        isValid = false;
    }

    if (!dosageInput.value || dosageInput.value <= 0) {
        showFieldError(dosageInput, 'Vui lòng nhập liều lượng hợp lệ');
        isValid = false;
    }

    if (!durationInput.value || durationInput.value <= 0) {
        showFieldError(durationInput, 'Vui lòng nhập thời gian điều trị hợp lệ');
        isValid = false;
    }

    return isValid;
}

async function validateCode() {
    if (!codeInput.value.trim()) return;

    try {
        const response = await fetch(`${API_BASE_URL}/code/${codeInput.value}`);
        const data = await response.json();

        if (data && (!IS_EDIT_MODE || data.id !== parseInt(PROTOCOL_ID))) {
            showFieldError(codeInput, 'Mã phác đồ này đã tồn tại');
            return false;
        }

        clearFieldError(codeInput);
        return true;

    } catch (error) {
        console.error('Error validating code:', error);
        return true; // Allow submission if validation check fails
    }
}

function showFieldError(field, message) {
    field.classList.add('is-invalid');
    const errorDiv = field.nextElementSibling;
    if (errorDiv && errorDiv.classList.contains('invalid-feedback')) {
        errorDiv.textContent = message;
    } else {
        const div = document.createElement('div');
        div.className = 'invalid-feedback';
        div.textContent = message;
        field.parentNode.insertBefore(div, field.nextSibling);
    }
}

function clearFieldError(field) {
    field.classList.remove('is-invalid');
    const errorDiv = field.nextElementSibling;
    if (errorDiv && errorDiv.classList.contains('invalid-feedback')) {
        errorDiv.textContent = '';
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