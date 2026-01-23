// Mobile Menu Toggle
const mobileMenuToggle = document.getElementById('mobileMenuToggle');
const sidebar = document.getElementById('sidebar');

mobileMenuToggle.addEventListener('click', () => {
    sidebar.classList.toggle('open');
});

// Close sidebar when clicking outside on mobile
document.addEventListener('click', (e) => {
    if (window.innerWidth <= 768) {
        if (sidebar.classList.contains('open')) {
            if (!sidebar.contains(e.target) && !mobileMenuToggle.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        }
    }
});

// Close sidebar on window resize if it's open on desktop
window.addEventListener('resize', () => {
    if (window.innerWidth > 768) {
        sidebar.classList.remove('open');
    }
});

// Handle document card clicks (ready for Thymeleaf integration)
document.querySelectorAll('.doc-card:not(.start-new-card)').forEach(card => {
    card.addEventListener('click', () => {
        // This will be handled by Thymeleaf th:onclick or th:href
        console.log('Document card clicked');
    });
});

// Handle start new chat card click
document.querySelector('.start-new-card')?.addEventListener('click', () => {
    // This will trigger file upload or redirect
    console.log('Start new chat clicked');
});

// Handle upload button click
document.querySelector('.upload-btn')?.addEventListener('click', () => {
    // This will trigger file upload modal or redirect
    console.log('Upload button clicked');
});

