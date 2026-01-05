// Timeland - Interactive JavaScript

const prefersReducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches ?? false;

document.addEventListener('DOMContentLoaded', () => {
    initMobileMenu();
    initRevealOnScroll();
    initRulesToggles();
    initSmoothScroll();
    initScrollUI();
    initKeyboardShortcuts();
});

function initMobileMenu() {
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const navLinks = document.querySelector('.nav-links');

    if (!mobileMenuBtn || !navLinks) return;

    mobileMenuBtn.addEventListener('click', () => {
        navLinks.classList.toggle('active');
        mobileMenuBtn.classList.toggle('active');
    });

    document.querySelectorAll('.nav-links a').forEach((link) => {
        link.addEventListener('click', () => {
            navLinks.classList.remove('active');
            mobileMenuBtn.classList.remove('active');
        });
    });
}

function initRevealOnScroll() {
    if (prefersReducedMotion) return;

    const observerOptions = {
        threshold: 0.12,
        rootMargin: '0px 0px -50px 0px',
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) return;
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
            observer.unobserve(entry.target);
        });
    }, observerOptions);

    document.querySelectorAll('.info-card, .donate-card, .rule-category').forEach((el) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(18px)';
        el.style.transition = 'opacity 0.7s ease, transform 0.7s ease';
        observer.observe(el);
    });
}

function initScrollUI() {
    const navbar = document.querySelector('.navbar');
    const hero = document.querySelector('.hero-content');
    const scrollTopBtn = document.getElementById('scrollTop');

    let ticking = false;

    const update = () => {
        const y = window.scrollY || 0;

        if (navbar) navbar.classList.toggle('scrolled', y > 50);
        if (scrollTopBtn) scrollTopBtn.classList.toggle('show', y > 300);

        if (hero) {
            if (!prefersReducedMotion && y < window.innerHeight) {
                hero.style.transform = `translateY(${y * 0.12}px)`;
                hero.style.opacity = String(Math.max(0, 1 - y / 900));
            } else {
                hero.style.transform = '';
                hero.style.opacity = '';
            }
        }

        ticking = false;
    };

    const onScroll = () => {
        if (ticking) return;
        ticking = true;
        window.requestAnimationFrame(update);
    };

    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener(
        'resize',
        () => {
            document.querySelectorAll('.rule-category-content.active').forEach((content) => {
                content.style.maxHeight = `${content.scrollHeight}px`;
            });
        },
        { passive: true },
    );

    update();
}

function toggleRule(header) {
    const content = header?.nextElementSibling;
    if (!content) return;

    const icon = header.querySelector('.toggle-icon');

    const isOpen = content.classList.toggle('active');

    if (isOpen) {
        content.style.maxHeight = `${content.scrollHeight}px`;
        if (icon) icon.textContent = '−';
    } else {
        content.style.maxHeight = '0px';
        if (icon) icon.textContent = '+';
    }
}

function initRulesToggles() {
    document.querySelectorAll('.rule-category').forEach((category) => {
        const header = category.querySelector('.rule-category-header');
        const content = category.querySelector('.rule-category-content');
        const icon = category.querySelector('.toggle-icon');

        if (!header || !content || !icon) return;

        content.classList.add('active');
        content.style.maxHeight = `${content.scrollHeight}px`;
        icon.textContent = '−';
    });
}

function copyIP() {
    const ipEl = document.getElementById('server-ip');
    const copyMessage = document.getElementById('copy-message');
    if (!ipEl || !copyMessage) return;

    const ip = ipEl.textContent.trim();

    const showCopyMessage = () => {
        copyMessage.classList.add('show');
        window.setTimeout(() => copyMessage.classList.remove('show'), 2000);
    };

    const fallbackCopy = (text) => {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        document.body.appendChild(textArea);
        textArea.select();
        try {
            document.execCommand('copy');
            showCopyMessage();
        } finally {
            document.body.removeChild(textArea);
        }
    };

    if (navigator.clipboard?.writeText) {
        navigator.clipboard
            .writeText(ip)
            .then(showCopyMessage)
            .catch(() => fallbackCopy(ip));
        return;
    }

    fallbackCopy(ip);
}

function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (!href || href === '#') return;

            const target = document.querySelector(href);
            if (!target) return;

            e.preventDefault();

            const offsetTop = target.offsetTop - 80;
            window.scrollTo({
                top: Math.max(0, offsetTop),
                behavior: prefersReducedMotion ? 'auto' : 'smooth',
            });
        });
    });
}

function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: prefersReducedMotion ? 'auto' : 'smooth',
    });
}

function initKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        if (e.key !== 'Escape') return;

        const navLinks = document.querySelector('.nav-links');
        const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
        if (!navLinks || !mobileMenuBtn) return;

        if (!navLinks.classList.contains('active')) return;

        navLinks.classList.remove('active');
        mobileMenuBtn.classList.remove('active');
    });
}
