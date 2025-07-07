// Khởi tạo các biến
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let searchTerm = "";
let sortBy = "visitDate";
let sortDir = "desc";

// Bảo vệ trang: nếu chưa đăng nhập thì chuyển về login
if (typeof getToken === "function" && !getToken()) {
  window.location.href = "/auth/login";
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

// Cập nhật số liệu thống kê
function updateStats() {
  const records = document.querySelectorAll("#medical-records-container .card");
  const totalRecords = records.length;
  
  let completedRecords = 0;
  let pendingRecords = 0;
  
  records.forEach(record => {
    const status = record.querySelector(".card-text:last-of-type span").textContent.trim();
    if (status === "Đã hoàn thành") {
      completedRecords++;
    } else if (status === "Đang chờ") {
      pendingRecords++;
    }
  });

  document.getElementById("total-records").textContent = totalRecords;
  document.getElementById("completed-records").textContent = completedRecords;
  document.getElementById("pending-records").textContent = pendingRecords;
}

// Khởi tạo trang
document.addEventListener("DOMContentLoaded", () => {
  // Cập nhật số liệu thống kê
  updateStats();

  // Gắn sự kiện cho form tìm kiếm nếu có
  const searchForm = document.getElementById("search-form");
  if (searchForm) {
    searchForm.addEventListener("submit", function (event) {
      event.preventDefault();
      const searchInput = document.getElementById("search-input");
      const searchTerm = searchInput.value.toLowerCase();
      
      const records = document.querySelectorAll("#medical-records-container .card");
      records.forEach(record => {
        const recordNumber = record.querySelector(".card-title span").textContent.toLowerCase();
        const patientName = record.querySelector(".card-text:first-of-type span").textContent.toLowerCase();
        
        if (recordNumber.includes(searchTerm) || patientName.includes(searchTerm)) {
          record.style.display = "";
        } else {
          record.style.display = "none";
        }
      });
      
      // Cập nhật lại số liệu thống kê sau khi lọc
      updateStats();
    });
  }

  // Gắn hiệu ứng cho các nút sắp xếp nếu có
  document.querySelectorAll("[data-sort]").forEach((button) => {
    button.addEventListener("click", () => {
      // Không làm gì vì không còn fetch API
    });
  });
});

// Delete confirmation
function deleteRecord(id) {
  const modal = new bootstrap.Modal(document.getElementById("deleteModal"));
  const confirmBtn = document.getElementById("confirmDelete");

  confirmBtn.onclick = function () {
    window.location.href = `/medicalRecord/delete/${id}`;
  };

  modal.show();
}

// Print record
function printRecord() {
  window.print();
}

// Form validation and handling
document.addEventListener("DOMContentLoaded", function () {
  const form = document.querySelector("form");
  if (!form) return; // Không có form thì thoát

  const submitButton = document.getElementById("submit-button");

  // Form validation
  function validateForm() {
    let isValid = true;
    const requiredFields = form.querySelectorAll("[required]");

    requiredFields.forEach((field) => {
      if (!field.value.trim()) {
        isValid = false;
        field.classList.add("is-invalid");

        // Add error message if not exists
        if (
          !field.nextElementSibling ||
          !field.nextElementSibling.classList.contains("invalid-feedback")
        ) {
          const feedback = document.createElement("div");
          feedback.className = "invalid-feedback";
          feedback.textContent = "Trường này không được để trống";
          field.parentNode.insertBefore(feedback, field.nextSibling);
        }
      } else {
        field.classList.remove("is-invalid");
        // Remove error message if exists
        const feedback = field.nextElementSibling;
        if (feedback && feedback.classList.contains("invalid-feedback")) {
          feedback.remove();
        }
      }
    });

    return isValid;
  }

  // Handle form submission
  form.addEventListener("submit", function (e) {
    e.preventDefault();

    if (validateForm()) {
      submitButton.disabled = true;
      submitButton.innerHTML =
        '<span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...';

      // Submit form
      this.submit();
    }
  });

  // Real-time validation on input
  const inputs = form.querySelectorAll("input, select, textarea");
  inputs.forEach((input) => {
    input.addEventListener("input", function () {
      if (this.hasAttribute("required")) {
        if (this.value.trim()) {
          this.classList.remove("is-invalid");
          const feedback = this.nextElementSibling;
          if (feedback && feedback.classList.contains("invalid-feedback")) {
            feedback.remove();
          }
        }
      }
    });
  });

  // Add shadow effect on hover for buttons
  const buttons = document.querySelectorAll(".shadow-hover");
  buttons.forEach((button) => {
    button.addEventListener("mouseenter", function () {
      this.classList.add("shadow");
    });

    button.addEventListener("mouseleave", function () {
      this.classList.remove("shadow");
    });
  });

  // Confirm before leaving if form has changes
  let formChanged = false;

  inputs.forEach((input) => {
    input.addEventListener("change", () => {
      formChanged = true;
    });
  });

  window.addEventListener("beforeunload", (e) => {
    if (formChanged) {
      e.preventDefault();
      e.returnValue = "";
    }
  });

  // Reset form changed state after successful submission
  form.addEventListener("submit", () => {
    if (validateForm()) {
      formChanged = false;
    }
  });
});

// Format date inputs to local format
function formatDate(date) {
  if (!date) return "";
  return new Date(date).toLocaleDateString("vi-VN");
}

// Initialize any date pickers
document.addEventListener("DOMContentLoaded", function () {
  const dateInputs = document.querySelectorAll('input[type="date"]');
  dateInputs.forEach((input) => {
    // Set max date to today
    input.max = new Date().toISOString().split("T")[0];

    // Format displayed date
    if (input.value) {
      const formattedDate = formatDate(input.value);
      input.setAttribute("data-formatted-date", formattedDate);
    }
  });
});
