const App = {
  async request(url, options = {}) {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(options.headers || {})
      }
    });
    let body = null;
    try { body = await response.json(); } catch (_) {}
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
  toast(message, type = 'success') {
    const root = document.getElementById('toast');
    if (!root) return;
    const item = document.createElement('div');
    item.className = `toast ${type}`;
    item.textContent = message;
    root.appendChild(item);
    setTimeout(() => item.remove(), 3600);
  }
};
