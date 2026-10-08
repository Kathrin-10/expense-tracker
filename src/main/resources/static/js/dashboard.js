(async () => {
  try {
    const d = await App.request('/api/dashboard');
    document.getElementById('total').textContent = App.money(d.totalExpenses);
    document.getElementById('count').textContent = d.totalNumberOfExpenses;
    document.getElementById('highest').textContent = App.money(d.highestExpense);
    document.getElementById('lowest').textContent = App.money(d.lowestExpense);
    document.getElementById('current').textContent = App.money(d.currentMonthExpense);
    const max = Math.max(...d.categoryTotals.map(x => Number(x.total)), 1);
    document.getElementById('category-bars').innerHTML = d.categoryTotals.length ? d.categoryTotals.map(x => `<div class="bar-row"><span>${App.escape(x.categoryName)}</span><div class="bar-bg"><div class="bar-fill" style="width:${Math.round(Number(x.total)/max*100)}%"></div></div><span class="bar-value">${App.money(x.total)}</span></div>`).join('') : '<div class="empty">No expenses recorded yet.</div>'
  } catch (e) {
    App.toast(e.message, 'error')
  }
})();