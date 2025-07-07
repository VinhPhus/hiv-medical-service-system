// Hiệu ứng focus cho input
const inputs = document.querySelectorAll(
  ".contact-form-card input, .contact-form-card textarea, .contact-form-card select"
);
inputs.forEach((input) => {
  input.addEventListener("focus", function () {
    this.style.boxShadow = "0 0 0 2px #0d6efd33";
    this.style.borderColor = "#0d6efd";
  });
  input.addEventListener("blur", function () {
    this.style.boxShadow = "";
    this.style.borderColor = "";
  });
});

// Hiệu ứng gửi thành công (giả lập)
const form = document.querySelector("form");
if (form) {
  form.addEventListener("submit", function (e) {
    if (form.checkValidity()) {
      e.preventDefault();
      const btn = form.querySelector('button[type="submit"]');
      btn.disabled = true;
      btn.textContent = "Đang gửi...";
      setTimeout(() => {
        btn.textContent = "Gửi tin nhắn";
        btn.disabled = false;
        alert("Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
        form.reset();
      }, 1200);
    }
  });
}

// Hiệu ứng động cho icon khi hover card
const cards = document.querySelectorAll(".contact-info-card");
cards.forEach((card) => {
  card.addEventListener("mouseenter", function () {
    const icon = card.querySelector(".feature-icon");
    if (icon) icon.style.transform = "scale(1.15) rotate(-8deg)";
  });
  card.addEventListener("mouseleave", function () {
    const icon = card.querySelector(".feature-icon");
    if (icon) icon.style.transform = "";
  });
});
