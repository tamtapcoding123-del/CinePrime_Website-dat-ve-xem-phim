// Thành viên screens (17/11/2026): tier switcher without reload, voucher descriptions under the short selects,
// and the campaign reach estimate. Plain JS; every action still works as a normal link or form without it.
(() => {
  const editor = document.querySelector('[data-tier-editor]');
  if (editor) {
    const show = code => {
      editor.querySelectorAll('[data-tier-panel]').forEach(p => { p.hidden = p.dataset.tierPanel !== code; });
      editor.querySelectorAll('[data-tier-tab]').forEach(a => {
        const on = a.dataset.tierTab === code;
        a.classList.toggle('active', on);
        if (on) a.setAttribute('aria-current', 'true'); else a.removeAttribute('aria-current');
      });
      const field = editor.querySelector('input[name="tier"]');
      if (field) field.value = code;
      document.querySelectorAll('.tier-strip .ladder li').forEach(li => {
        const link = li.querySelector('a');
        li.classList.toggle('current', !!link && link.href.endsWith('tier=' + code));
      });
      try { history.replaceState(null, '', '/admin/tiers?tier=' + code); } catch {}
    };
    editor.querySelectorAll('[data-tier-tab]').forEach(a => a.addEventListener('click', e => { e.preventDefault(); show(a.dataset.tierTab); }));
    // Ladder steps on this page point at /admin/tiers?tier=…: switch in place, keeping unsaved edits.
    document.querySelectorAll('.tier-strip .ladder a[href*="/admin/tiers?tier="]').forEach(a => a.addEventListener('click', e => {
      e.preventDefault(); show(new URL(a.href).searchParams.get('tier'));
    }));
    // A save that fails validation names the tier; open the panel holding an invalid field.
    editor.addEventListener('invalid', e => { const panel = e.target.closest('[data-tier-panel]'); if (panel && panel.hidden) show(panel.dataset.tierPanel); }, true);
  }

  // The selects show only the template code; its description sits underneath.
  document.querySelectorAll('[data-voucher-select]').forEach(select => {
    const out = select.parentElement.querySelector('[data-voucher-desc]');
    const sync = () => { if (out) out.textContent = select.selectedOptions[0]?.dataset.desc || ''; };
    select.addEventListener('change', sync); sync();
  });

  // Campaign: how many customers the chosen tier reaches, and what each receives.
  const campaign = document.querySelector('[data-campaign]');
  if (campaign) {
    const reach = campaign.querySelector('[data-reach]');
    const update = () => {
      const tier = campaign.querySelector('[name="minTier"]').value;
      let total = 0, counting = !tier;
      campaign.querySelectorAll('[data-tier-count]').forEach(n => { if (n.dataset.tierCount === tier) counting = true; if (counting) total += Number(n.dataset.count); });
      const voucher = campaign.querySelector('[name="voucherId"]').selectedOptions[0];
      const count = Number(campaign.querySelector('[name="count"]').value) || 0;
      const gift = campaign.querySelector('[name="productId"]').selectedOptions[0];
      const parts = [];
      if (voucher && voucher.value) parts.push(`${count} × ${voucher.dataset.desc || voucher.textContent}`);
      if (gift && gift.value) parts.push(`quà ${gift.textContent.split(' · ')[0]}`);
      if (!parts.length && campaign.querySelector('[name="message"]').value.trim()) parts.push('một thông báo');
      reach.textContent = `${total} khách đang hoạt động sẽ nhận ${parts.length ? parts.join(' + ') : '— chọn voucher, quà hoặc nhập thông báo'}. Khách đã nhận theo mã chiến dịch này sẽ được bỏ qua.`;
    };
    campaign.addEventListener('input', update); campaign.addEventListener('change', update); update();
  }
})();
