// Privacy Policy Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Initialize animations
    initializeAnimations();
    
    // Initialize privacy cards
    initializePrivacyCards();
    
    // Initialize contact info cards
    initializeContactCards();
    
    // Initialize smooth scrolling
    initializeSmoothScrolling();
    
    // Initialize counter animations
    initializeCounters();
    
    // Initialize privacy sections
    initializePrivacySections();
    
    // Initialize security badges
    initializeSecurityBadges();
});

// Initialize text animations
function initializeAnimations() {
    const animatedElements = document.querySelectorAll('.animate-text');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, {
        threshold: 0.1
    });
    
    animatedElements.forEach(element => {
        observer.observe(element);
    });
}

// Initialize privacy cards with enhanced interactions
function initializePrivacyCards() {
    const privacyCards = document.querySelectorAll('.card');
    
    privacyCards.forEach(card => {
        // Add privacy-card class for styling
        card.classList.add('privacy-card');
        
        // Add click event for privacy cards
        card.addEventListener('click', function(e) {
            // Don't trigger if clicking on a button
            if (e.target.tagName === 'A' || e.target.closest('a')) {
                return;
            }
            
            // Add ripple effect
            createRippleEffect(e, card);
            
            // Add temporary highlight
            card.style.transform = 'scale(1.02)';
            setTimeout(() => {
                card.style.transform = '';
            }, 200);
        });
        
        // Add hover sound effect (optional)
        card.addEventListener('mouseenter', function() {
            // You can add sound effects here if needed
            console.log('Privacy card hovered');
        });
    });
}

// Initialize contact info cards
function initializeContactCards() {
    const contactCards = document.querySelectorAll('.d-flex.flex-column.align-items-center');
    
    contactCards.forEach(card => {
        card.classList.add('contact-info-card');
        
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.transition = 'transform 0.3s ease';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

// Create ripple effect for cards
function createRippleEffect(event, element) {
    const ripple = document.createElement('span');
    const rect = element.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;
    
    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = x + 'px';
    ripple.style.top = y + 'px';
    ripple.classList.add('ripple');
    
    element.appendChild(ripple);
    
    setTimeout(() => {
        ripple.remove();
    }, 600);
}

// Initialize smooth scrolling for anchor links
function initializeSmoothScrolling() {
    const links = document.querySelectorAll('a[href^="#"]');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Initialize counter animations
function initializeCounters() {
    const counters = document.querySelectorAll('.counter');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                animateCounter(entry.target);
            }
        });
    }, {
        threshold: 0.5
    });
    
    counters.forEach(counter => {
        observer.observe(counter);
    });
}

// Animate counter numbers
function animateCounter(counterElement) {
    const target = counterElement.textContent;
    const isPercentage = target.includes('%');
    const isTime = target.includes('/');
    
    let finalValue;
    if (isPercentage) {
        finalValue = parseFloat(target.replace('%', ''));
    } else if (isTime) {
        finalValue = 24; // For 24/7
    } else {
        finalValue = parseInt(target.replace(/\D/g, ''));
    }
    
    let currentValue = 0;
    const increment = finalValue / 50;
    const timer = setInterval(() => {
        currentValue += increment;
        if (currentValue >= finalValue) {
            currentValue = finalValue;
            clearInterval(timer);
        }
        
        if (isPercentage) {
            counterElement.textContent = Math.floor(currentValue) + '%';
        } else if (isTime) {
            counterElement.textContent = Math.floor(currentValue) + '/7';
        } else {
            counterElement.textContent = Math.floor(currentValue) + '+';
        }
    }, 30);
}

// Initialize privacy sections
function initializePrivacySections() {
    const privacySections = document.querySelectorAll('.card.shadow-sm');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, {
        threshold: 0.2
    });
    
    privacySections.forEach(section => {
        section.classList.add('privacy-section');
        observer.observe(section);
    });
}

// Initialize security badges
function initializeSecurityBadges() {
    const securityCards = document.querySelectorAll('.card');
    
    securityCards.forEach((card, index) => {
        if (index < 3) { // Only add to first 3 cards
            const badge = document.createElement('div');
            badge.className = 'security-badge';
            badge.textContent = 'SECURE';
            card.style.position = 'relative';
            card.appendChild(badge);
        }
    });
}

// Privacy policy search functionality
function searchPrivacyPolicy(query) {
    const privacySections = document.querySelectorAll('.card.shadow-sm');
    const searchTerm = query.toLowerCase();
    
    privacySections.forEach(section => {
        const title = section.querySelector('h2')?.textContent.toLowerCase() || '';
        const content = section.textContent.toLowerCase();
        
        if (title.includes(searchTerm) || content.includes(searchTerm)) {
            section.style.display = 'block';
            section.style.animation = 'fadeIn 0.5s ease';
        } else {
            section.style.display = 'none';
        }
    });
}

// Loading animation
function showPrivacyLoading() {
    const loading = document.createElement('div');
    loading.className = 'privacy-loading';
    loading.innerHTML = '<div class="spinner"></div>';
    document.body.appendChild(loading);
}

function hidePrivacyLoading() {
    const loading = document.querySelector('.privacy-loading');
    if (loading) {
        loading.remove();
    }
}

// Privacy policy version checker
function checkPrivacyVersion() {
    const currentVersion = '1.0.0';
    const lastUpdated = '01/01/2024';
    
    console.log(`Privacy Policy Version: ${currentVersion}`);
    console.log(`Last Updated: ${lastUpdated}`);
    
    // You can add version checking logic here
    return {
        version: currentVersion,
        lastUpdated: lastUpdated
    };
}

// Add CSS for ripple effect and animations
const style = document.createElement('style');
style.textContent = `
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(13, 110, 253, 0.3);
        transform: scale(0);
        animation: ripple-animation 0.6s linear;
        pointer-events: none;
    }
    
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    .privacy-card {
        position: relative;
        overflow: hidden;
    }
    
    .privacy-section {
        transition: all 0.6s ease;
    }
    
    .privacy-section.visible {
        opacity: 1;
        transform: translateY(0);
    }
`;
document.head.appendChild(style);

// Export functions for global use
window.PrivacyPolicy = {
    searchPrivacyPolicy,
    showPrivacyLoading,
    hidePrivacyLoading,
    checkPrivacyVersion
};

// Initialize version checker
document.addEventListener('DOMContentLoaded', function() {
    PrivacyPolicy.checkPrivacyVersion();
}); 