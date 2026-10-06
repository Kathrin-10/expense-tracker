let categories = [];
let page = 0;

const $e = (id) => document.getElementById(id);

async function loadCats() {
  categories = await App.request('/api/categories');
  const options = categories.map(c => `<option value="${c.id}">${App.escape(c.name)}</option>`).join('');
  $e('category').insertAdjacentHTML('beforeend', options);
  $e('expenseCategory').innerHTML = '<option value="">Select category</option>' + options;
}

function params() {
  const sortValue = $e('sort').value.split(':');
  const query = new URLSearchParams({
    page,
    size: 10,
    sort: sortValue[0],
    direction: sortValue[1]
  });
  if ($e('search').value.trim()) query.set('title', $e('search').value.trim());
  if ($e('category').value) query.set('categoryId', $e('category').value);
  if ($e('month').value) query.set('month', $e('month').value);
  if ($e('fromDate').value) query.set('fromDate', $e('fromDate').value);
  if ($e('toDate').value) query.set('toDate', $e('toDate').value);
  return query;
}

async function load() {
  try {
    const data = await App.request('/api/expenses?' + params());
    $e('resultSummary').textContent = `${data.totalElements} transaction${data.totalElements === 1 ? '' : 's'} found`;
    $e('pageInfo').textContent = data.totalPages ? `Page ${data.page + 1} of ${data.totalPages}` : 'Page 0 of 0';
    $e('prev').disabled = !data.hasPrevious;
    $e('next').disabled = !data.hasNext;
    $e('empty').classList.toggle('hidden', data.content.length > 0);
    $e('expenseRows').innerHTML = data.content.map(x => `
      <tr>
        <td><span class="expense-name">${App.escape(x.title)}</span>${x.description ? `<span class="expense-desc">${App.escape(x.description)}</span>` : ''}</td>
        <td><span class="pill">${App.escape(x.categoryName)}</span></td>
        <td>${App.date(x.expenseDate)}</td>
        <td class="amount">${App.money(x.amount)}</td>
        <td><div class="actions"><button class="action-btn" data-action="edit" data-id="${x.id}">Edit</button><button class="action-btn delete" data-action="delete" data-id="${x.id}" data-title="${App.escapeAttribute(x.title)}">Delete</button></div></td>
      </tr>`).join('');
  } catch (error) {
    App.toast(error.message, 'error');
  }
}

function openModal(expense = null) {
  $e('expenseId').value = expense?.id || '';
  $e('modalTitle').textContent = expense ? 'Edit expense' : 'Add expense';
  $e('modalEyebrow').textContent = expense ? 'EDIT TRANSACTION' : 'NEW TRANSACTION';
  $e('title').value = expense?.title || '';
  $e('amount').value = expense?.amount || '';
  $e('expenseDate').value = expense?.expenseDate || new Date().toISOString().slice(0, 10);
  $e('expenseCategory').value = expense?.categoryId || '';
  $e('description').value = expense?.description || '';
  $e('descCount').textContent = $e('description').value.length;
  $e('formError').classList.add('hidden');
  $e('modal').classList.remove('hidden');
  $e('title').focus();
}

async function editExpense(id) {
  try {
    openModal(await App.request('/api/expenses/' + id));
  } catch (error) {
    App.toast(error.message, 'error');
  }
}

async function removeExpense(id, title) {
  if (!confirm(`Delete “${title}”?\n\nThis action cannot be undone.`)) return;
  try {
    await App.request('/api/expenses/' + id, { method: 'DELETE' });
    App.toast('Expense deleted');
    if (page > 0 && $e('expenseRows').children.length === 1) page--;
    load();
  } catch (error) {
    App.toast(error.message, 'error');
  }
}

$e('addBtn').onclick = () => openModal();
$e('closeModal').onclick = $e('cancelModal').onclick = () => $e('modal').classList.add('hidden');
$e('description').oninput = e => $e('descCount').textContent = e.target.value.length;
$e('sort').onchange = () => { page = 0; load(); };
['search', 'category', 'month', 'fromDate', 'toDate'].forEach(id => {
  $e(id).addEventListener('change', () => { page = 0; load(); });
});
$e('search').addEventListener('keyup', e => { if (e.key === 'Enter') { page = 0; load(); } });
$e('clearFilters').onclick = () => {
  ['search', 'category', 'month', 'fromDate', 'toDate'].forEach(id => $e(id).value = '');
  page = 0;
  load();
};
$e('prev').onclick = () => { page--; load(); };
$e('next').onclick = () => { page++; load(); };
$e('exportBtn').onclick = () => {
  const query = new URLSearchParams();
  if ($e('fromDate').value) query.set('fromDate', $e('fromDate').value);
  if ($e('toDate').value) query.set('toDate', $e('toDate').value);
  window.location = '/api/expenses/export/csv?' + query;
};

$e('expenseRows').addEventListener('click', async event => {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const id = Number(button.dataset.id);
  if (button.dataset.action === 'edit') await editExpense(id);
  if (button.dataset.action === 'delete') await removeExpense(id, button.dataset.title);
});

$e('expenseForm').onsubmit = async event => {
  event.preventDefault();
  const id = $e('expenseId').value;
  const body = {
    title: $e('title').value.trim(),
    amount: Number($e('amount').value),
    expenseDate: $e('expenseDate').value,
    description: $e('description').value.trim() || null,
    categoryId: Number($e('expenseCategory').value)
  };
  try {
    await App.request(id ? '/api/expenses/' + id : '/api/expenses', {
      method: id ? 'PUT' : 'POST',
      body: JSON.stringify(body)
    });
    $e('modal').classList.add('hidden');
    App.toast(id ? 'Expense updated' : 'Expense added');
    load();
  } catch (error) {
    $e('formError').textContent = error.message;
    $e('formError').classList.remove('hidden');
  }
};

(async () => {
  try {
    await loadCats();
    await load();
    if (new URLSearchParams(window.location.search).get('action') === 'add') openModal();
  } catch (error) {
    App.toast(error.message, 'error');
  }
})();
