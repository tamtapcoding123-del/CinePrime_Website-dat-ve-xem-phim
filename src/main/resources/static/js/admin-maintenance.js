// Admin → Bảo trì website: start date/time only when "Hẹn giờ" is chosen; "Để trống" clears the expected end.
(function () {
  'use strict';
  var form = document.getElementById('mt-form');
  if (!form) return;
  var schedule = form.querySelector('[data-mt-schedule]');
  function sync() {
    var scheduled = form.querySelector('input[name=startMode]:checked');
    var on = !!scheduled && scheduled.value === 'schedule';
    schedule.hidden = !on;
    schedule.querySelectorAll('input').forEach(function (input) { input.disabled = !on; });
  }
  form.querySelectorAll('input[name=startMode]').forEach(function (radio) { radio.addEventListener('change', sync); });
  sync();
  var clear = form.querySelector('[data-mt-clear-end]');
  if (clear) clear.addEventListener('click', function () {
    form.querySelector('[name=endDate]').value = '';
    form.querySelector('[name=endTime]').value = '';
    form.querySelector('[name=endDate]').focus();
  });
})();
