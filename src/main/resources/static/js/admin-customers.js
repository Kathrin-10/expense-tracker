let customers = [];
const $ = id => document.getElementById(id);
async function loadCustomers() {
  customers = await App.request('/api/admin/customers');
  render();
}

function render() {
  const filter = $('statusFilter').value;
  const list = customers.filter(c => filter === 'all' || (filter === 'active' ? c.active : !c.active));
  $('customerSummary').textContent = `${list.length} customer${list.length===1?'':'s'} shown`;
  $('customerEmpty').classList.toggle('hidden', list.length > 0);
  $('customerRows').innerHTML = list.map(c => `<tr><td><strong>${App.escape(c.fullName)}</strong></td><td>${App.escape(c.email)}</td><td>${App.escape(c.phone||'—')}</td><td><span class="pill ${c.active?'status-active':'status-inactive'}">${c.active?'Active':'Inactive'}</span></td><td><button class="action-btn" data-edit="${c.id}">Edit</button><button class="action-btn ${c.active?'delete':''}" data-toggle="${c.id}">${c.active?'Deactivate':'Activate'}</button></td></tr>`).join('');
}

function openEdit(c) {
  $('customerId').value = c.id;
  $('customerName').value = c.fullName;
  $('customerEmail').value = c.email;
  $('customerPhone').value = c.phone || '';
  $('customerActive').checked = c.active;
  $('customerError').classList.add('hidden');
  $('customerModal').classList.remove('hidden');
}
$('statusFilter').onchange = render;
$('closeCustomer').onclick = $('cancelCustomer').onclick = () => $('customerModal').classList.add('hidden');
$('customerRows').addEventListener('click', async e => {
  const edit = e.target.closest('[data-edit]'),
    toggle = e.target.closest('[data-toggle]');
  if (edit) {
    const customer = customers.find(c => c.id == edit.dataset.edit);
    if (customer) openEdit(customer);
    return;
  }
  if (toggle) {
    const c = customers.find(c => c.id == toggle.dataset.toggle);
    if (!c) return;
    const action = c.active ? 'Deactivate' : 'Activate';
    if (!await App.confirmAction(`${action} ${c.fullName}?`, {
        title: `${action} customer`,
        confirmText: action,
        destructive: c.active
      })) return;
    try {
      await App.request('/api/admin/customers/' + c.id, {
        method: 'PUT',
        body: JSON.stringify({
          fullName: c.fullName,
          email: c.email,
          phone: c.phone,
          active: !c.active
        })
      });
      App.toast(`Customer ${c.active?'deactivated':'activated'}`);
      await loadCustomers();
    } catch (err) {
      App.toast(err.message, 'error');
    }
  }
});
$('customerForm').onsubmit = async e => {
  e.preventDefault();
  try {
    await App.request('/api/admin/customers/' + $('customerId').value, {
      method: 'PUT',
      body: JSON.stringify({
        fullName: $('customerName').value.trim(),
        email: $('customerEmail').value.trim(),
        phone: $('customerPhone').value.trim() || null,
        active: $('customerActive').checked
      })
    });
    $('customerModal').classList.add('hidden');
    App.toast('Customer details updated');
    await loadCustomers();
  } catch (err) {
    $('customerError').textContent = err.message;
    $('customerError').classList.remove('hidden');
  }
};
loadCustomers().catch(e => App.toast(e.message, 'error'));