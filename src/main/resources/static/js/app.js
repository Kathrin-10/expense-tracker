const App = {
  csrfToken() {
    // Prefer the server-rendered token for this page. The XSRF-TOKEN cookie can
    // survive a session rotation and become stale after login/logout.
    const metaToken = document.querySelector('meta[name=csrf-token]')?.content;
    if (metaToken) return metaToken;
    const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  },
  async request(url, options = {}) {
    const csrf = this.csrfToken();
    const response = await fetch(url, {
      ...options,
      headers: {
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
        ...(options.headers || {})
      },
      credentials: 'same-origin'
    });
    let body = null;
    try { body = await response.json(); } catch (_) {}
    if (response.status === 401) { window.location.href = url.startsWith('/api/admin/') ? '/admin/login?error=true' : '/login?deactivated=true'; return null; }
    if (!response.ok) {
      throw new Error(body?.message || body?.error || `Request failed (${response.status})`);
    }
    return body;
  },
  money(value) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(Number(value || 0));
  },
  date(value) {
    if (!value) return '—';
    return new Date(value + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  },
  escape(value) {
    return String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  },
  escapeAttribute(value) {
    return this.escape(value);
  },
  confirmAction(message, { title = 'Please confirm', confirmText = 'Continue', destructive = false } = {}) {
    return new Promise(resolve => {
      const previousFocus = document.activeElement;
      const backdrop = document.createElement('div');
      backdrop.className = 'confirm-backdrop';
      backdrop.innerHTML = `
        <section class="confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="confirmTitle" aria-describedby="confirmMessage" tabindex="-1">
          <h2 id="confirmTitle"></h2>
          <p id="confirmMessage"></p>
          <div class="confirm-actions">
            <button type="button" class="btn ghost" data-confirm-cancel>Cancel</button>
            <button type="button" class="btn ${destructive ? 'danger' : 'primary'}" data-confirm-accept></button>
          </div>
        </section>`;
      backdrop.querySelector('#confirmTitle').textContent = title;
      backdrop.querySelector('#confirmMessage').textContent = message;
      backdrop.querySelector('[data-confirm-accept]').textContent = confirmText;
      document.body.appendChild(backdrop);

      const finish = accepted => {
        document.removeEventListener('keydown', onKeydown);
        backdrop.remove();
        if (previousFocus instanceof HTMLElement) previousFocus.focus();
        resolve(accepted);
      };
      const onKeydown = event => {
        if (event.key === 'Escape') finish(false);
      };

      backdrop.querySelector('[data-confirm-cancel]').addEventListener('click', () => finish(false));
      backdrop.querySelector('[data-confirm-accept]').addEventListener('click', () => finish(true));
      backdrop.addEventListener('click', event => {
        if (event.target === backdrop) finish(false);
      });
      document.addEventListener('keydown', onKeydown);
      backdrop.querySelector('[data-confirm-cancel]').focus();
    });
  },
  toast(message, type = 'success') {
    const root = document.getElementById('toast');
    if (!root) return;
    const variant = ['success', 'hint', 'error'].includes(type) ? type : 'success';
    root.setAttribute('aria-live', variant === 'error' ? 'assertive' : 'polite');
    const item = document.createElement('div');
    item.className = `toast ${variant}`;
    item.setAttribute('role', variant === 'error' ? 'alert' : 'status');
    item.textContent = message;
    root.appendChild(item);
    setTimeout(() => item.remove(), 3600);
  }
};
