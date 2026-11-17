// Sidebar groups (Lịch chiếu) remember whether the admin left them open, so moving to another screen does not fold them.
// Loaded without defer right after the sidebar nav, so the saved state is applied before the first paint (CSP forbids inline scripts).
(() => {
  const key = id => `cineprime.admin.navGroup.${id}`;
  document.querySelectorAll('details.nav-group[data-nav-id]').forEach(group => {
    try { if (localStorage.getItem(key(group.dataset.navId)) === '1') group.open = true; } catch {}
    group.addEventListener('toggle', () => { try { localStorage.setItem(key(group.dataset.navId), group.open ? '1' : '0'); } catch {} });
  });
})();
