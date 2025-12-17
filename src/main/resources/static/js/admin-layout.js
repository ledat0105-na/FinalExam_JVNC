function toggleSidebar() {
    const sidebar = document.getElementById('adminSidebar');
    if (sidebar) {
        sidebar.classList.toggle('collapsed');
    }
}

function updateClock() {
    const now = new Date();
    const time = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    const date = now.toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
    const clockEl = document.getElementById('clock');
    const dateEl = document.getElementById('date');
    if (clockEl) clockEl.textContent = time;
    if (dateEl) dateEl.textContent = date;
}

function setActiveSidebarItem() {
    const currentPath = window.location.pathname;
    const menuItems = document.querySelectorAll('.sidebar-menu-item');
    
    menuItems.forEach(item => {
        const href = item.getAttribute('href');
        if (href && href !== '/logout') {
            if (currentPath === href || currentPath.startsWith(href + '/')) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        }
    });
    
    const accountsItem = document.querySelector('.sidebar-menu-item[href="/admin/accounts"]');
    if (accountsItem && (currentPath.includes('/admin/accounts') || currentPath.includes('/admin/wallet/deposit'))) {
        accountsItem.classList.add('active');
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        setInterval(updateClock, 1000);
        updateClock();
        setActiveSidebarItem();
    });
} else {
    setInterval(updateClock, 1000);
    updateClock();
    setActiveSidebarItem();
}

