// JS riêng cho trang danh sách kết quả xét nghiệm

// Khởi tạo các biến
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let searchTerm = "";
let sortBy = "testDate";
let sortDir = "desc";

// Hàm tải danh sách kết quả xét nghiệm
async function loadTestResults() {
  try {
    const response = await fetch(
      `/api/test-results?page=${currentPage}&size=${pageSize}&sortBy=${sortBy}&sortDir=${sortDir}&search=${searchTerm}`
    );
    const data = await response.json();

    if (response.ok) {
      displayTestResults(data.content);
      updatePagination(data);
      updateStats();
    } else {
      showError("Không thể tải danh sách kết quả xét nghiệm");
    }
  } catch (error) {
    console.error("Lỗi:", error);
    showError("Đã xảy ra lỗi khi tải dữ liệu");
  }
}

// Hiển thị danh sách kết quả xét nghiệm
function displayTestResults(results) {
  const container = document.getElementById("test-results-container");
  container.innerHTML = "";

  results.forEach((result) => {
    const card = createResultCard(result);
    container.appendChild(card);
  });
}

// Tạo card cho mỗi kết quả xét nghiệm
function createResultCard(result) {
  const card = document.createElement("div");
  card.className = "test-result-card";

  const statusClass = getStatusClass(result.status);
  const formattedDate = new Date(result.testDate).toLocaleDateString("vi-VN");

  card.innerHTML = `
        <div class="test-result-header">
            <div class="test-code">${result.testCode}</div>
            <div class="test-date">${formattedDate}</div>
        </div>
        <div class="patient-info">
            <img src="${result.patient.avatarUrl || "/img/default-avatar.png"}" 
                 alt="Avatar" class="patient-avatar">
            <div>
                <div class="patient-name">${result.patient.user.fullName}</div>
                <div class="patient-code">${result.patient.patientCode}</div>
            </div>
        </div>
        <div class="test-details">
            <span class="test-status ${statusClass}">${result.status}</span>
            <p class="mt-2">
                <strong>Loại xét nghiệm:</strong> ${result.testType}<br>
                <strong>Kết quả:</strong> ${result.resultValue || "Chưa có"} ${
    result.unit || ""
  }<br>
                <strong>Khoảng tham chiếu:</strong> ${
                  result.referenceRange || "Không có"
                }
            </p>
        </div>
        <div class="test-actions">
            <a href="/testResult/detail/${result.id}" 
               class="btn btn-primary btn-sm">
                <i class="fas fa-eye me-1"></i>Xem chi tiết
            </a>
            <a href="/testResult/form/${result.id}" 
               class="btn btn-outline-primary btn-sm">
                <i class="fas fa-edit me-1"></i>Chỉnh sửa
            </a>
        </div>
    `;

  return card;
}

// Lấy class CSS cho trạng thái
function getStatusClass(status) {
  switch (status) {
    case "COMPLETED":
      return "status-completed";
    case "PENDING":
      return "status-pending";
    case "REVIEWED":
      return "status-reviewed";
    case "ABNORMAL":
      return "status-abnormal";
    default:
      return "";
  }
}

// Cập nhật phân trang
function updatePagination(data) {
  totalPages = data.totalPages;
  const pagination = document.getElementById("pagination");
  pagination.innerHTML = "";

  // Nút Previous
  const prevLi = document.createElement("li");
  prevLi.className = `page-item ${currentPage === 0 ? "disabled" : ""}`;
  prevLi.innerHTML = `
        <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">
            <i class="fas fa-chevron-left"></i>
        </a>
    `;
  pagination.appendChild(prevLi);

  // Các nút số trang
  for (let i = 0; i < totalPages; i++) {
    const li = document.createElement("li");
    li.className = `page-item ${i === currentPage ? "active" : ""}`;
    li.innerHTML = `
            <a class="page-link" href="#" onclick="changePage(${i})">${
      i + 1
    }</a>
        `;
    pagination.appendChild(li);
  }

  // Nút Next
  const nextLi = document.createElement("li");
  nextLi.className = `page-item ${
    currentPage === totalPages - 1 ? "disabled" : ""
  }`;
  nextLi.innerHTML = `
        <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">
            <i class="fas fa-chevron-right"></i>
        </a>
    `;
  pagination.appendChild(nextLi);
}

// Đổi trang
function changePage(page) {
  if (page >= 0 && page < totalPages) {
    currentPage = page;
    loadTestResults();
  }
}

// Cập nhật thống kê
async function updateStats() {
  try {
    const response = await fetch("/api/test-results/stats");
    const stats = await response.json();

    document.getElementById("total-tests").textContent = stats.total;
    document.getElementById("completed-tests").textContent = stats.completed;
    document.getElementById("pending-tests").textContent = stats.pending;
    document.getElementById("abnormal-tests").textContent = stats.abnormal;
  } catch (error) {
    console.error("Lỗi khi tải thống kê:", error);
  }
}

// Tìm kiếm
function handleSearch(event) {
  event.preventDefault();
  searchTerm = document.getElementById("search-input").value;
  currentPage = 0;
  loadTestResults();
}

// Sắp xếp
function handleSort(field) {
  if (sortBy === field) {
    sortDir = sortDir === "asc" ? "desc" : "asc";
  } else {
    sortBy = field;
    sortDir = "desc";
  }
  currentPage = 0;
  loadTestResults();
}

// Hiển thị thông báo lỗi
function showError(message) {
  const alertDiv = document.createElement("div");
  alertDiv.className = "alert alert-danger alert-dismissible fade show";
  alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

  const container = document.getElementById("alert-container");
  container.appendChild(alertDiv);

  setTimeout(() => {
    alertDiv.remove();
  }, 5000);
}

// Form validation
function validateForm() {
  const form = document.querySelector("form");
  const submitButton = document.getElementById("submit-button");

  if (form) {
    form.addEventListener("submit", function (event) {
      if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      } else {
        submitButton.disabled = true;
        submitButton.innerHTML = `
                    <span class="loading-spinner"></span>
                    Đang lưu...
                `;
      }
      form.classList.add("was-validated");
    });
  }
}

// File upload preview
function setupFileUpload() {
  const fileInput = document.getElementById("file");
  if (fileInput) {
    fileInput.addEventListener("change", function (event) {
      const file = event.target.files[0];
      if (file) {
        // Validate file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
          showError("Kích thước tệp không được vượt quá 5MB");
          fileInput.value = "";
          return;
        }

        // Validate file type
        const allowedTypes = ["application/pdf", "image/jpeg", "image/png"];
        if (!allowedTypes.includes(file.type)) {
          showError("Chỉ chấp nhận các định dạng: PDF, JPG, PNG");
          fileInput.value = "";
          return;
        }
      }
    });
  }
}

// Khởi tạo trang
document.addEventListener("DOMContentLoaded", () => {
  // Load danh sách nếu đang ở trang danh sách
  if (document.getElementById("test-results-container")) {
    loadTestResults();

    // Gắn sự kiện cho form tìm kiếm
    const searchForm = document.getElementById("search-form");
    if (searchForm) {
      searchForm.addEventListener("submit", handleSearch);
    }

    // Gắn sự kiện cho các nút sắp xếp
    document.querySelectorAll("[data-sort]").forEach((button) => {
      button.addEventListener("click", () => {
        handleSort(button.dataset.sort);
      });
    });
  }

  // Khởi tạo form validation nếu đang ở trang form
  validateForm();

  // Khởi tạo file upload nếu đang ở trang form
  setupFileUpload();
});
