(async () => {
  try {
    const rows = await App.request('/api/reports/monthly');
    const total = rows.reduce((s, r) => s + Number(r.totalExpenses), 0);
    document.getElementById('reportRows').innerHTML = rows.length ? rows.map(r => `<tr><td>${r.monthName.charAt(0)+r.monthName.slice(1).toLowerCase()} ${r.year}</td><td>${r.numberOfExpenses}</td><td class="amount">${App.money(r.totalExpenses)}</td><td><div class="share"><div class="share-bg"><div class="share-fill" style="width:${total?Math.round(Number(r.totalExpenses)/total*100):0}%"></div></div></div></td></tr>`).join('') : '<tr><td colspan="4" class="empty">No report data yet.</td></tr>';
    const max = Math.max(...rows.map(r => Number(r.totalExpenses)), 1);
    document.getElementById('reportChart').innerHTML = rows.slice().reverse().map(r => `<div class="chart-col"><span class="chart-value">${App.money(r.totalExpenses)}</span><div class="chart-bar" style="height:${Math.max(3,Number(r.totalExpenses)/max*190)}px"></div><span class="chart-label">${r.monthName.slice(0,3)} ${String(r.year).slice(-2)}</span></div>`).join('')
  } catch (e) {
    App.toast(e.message, 'error')
  }
})();