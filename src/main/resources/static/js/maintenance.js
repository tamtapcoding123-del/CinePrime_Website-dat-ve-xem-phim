// Maintenance card: "Tải lại trang" button, a quiet re-check every 60 s while the tab is visible, and the progress width
// (set here because the CSP forbids inline style attributes).
(function () {
  'use strict';
  document.querySelectorAll('[data-maint-fill]').forEach(function (bar) {
    var value = Math.max(0, Math.min(100, parseInt(bar.getAttribute('data-maint-fill'), 10) || 0));
    bar.style.width = value + '%';
  });
  document.querySelectorAll('[data-maint-reload]').forEach(function (button) {
    button.addEventListener('click', function () { window.location.reload(); });
  });
  var main = document.querySelector('[data-maint-refresh]');
  if (!main) return;
  var seconds = parseInt(main.getAttribute('data-maint-refresh'), 10) || 60;
  var status = document.querySelector('[data-maint-status]');
  window.setInterval(function () {
    if (document.visibilityState !== 'visible') return;
    if (status) status.textContent = 'Đang kiểm tra lại…';
    window.location.reload();
  }, seconds * 1000);
})();
