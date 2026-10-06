let categories = [];
const $ = id => document.getElementById(id);

async function loadCategories() {
  try {
    categories = await App.request('/api/categories');
    $('categoryGrid').innerHTML = categories.length ? categories.map(c => `
      <div class="category-card">
        <div><strong>${App.escape(c.name)}</strong><small>${c.expenseCount} expense${c.expenseCount === 1 ? '' : 's'}</small></div>
        <div class="category-actions"><button class="action-btn" data-action="edit" data-id="${c.id}">Edit</button><button class="action-btn delete" data-action="delete" data-id="${c.id}">Delete</button></div>
      </div>`).join('') : '<div class="empty">No categories found.</div>';
  } catch (error) {
    App.toast(error.message, 'error');
  }
}

function openCategory(category = null) {
  $('categoryId').value = category?.id || '';
  $('categoryModalTitle').textContent = category ? 'Edit category' : 'Add category';
  $('categoryName').value = category?.name || '';
  $('categoryError').classList.add('hidden');
  $('categoryModal').classList.remove('hidden');
  $('categoryName').focus();
}

$('addCategory').onclick = () => openCategory();
$('closeCategory').onclick = $('cancelCategory').onclick = () => $('categoryModal').classList.add('hidden');
$('categoryGrid').addEventListener('click', async event => {
  const button = event.target.closest('button[data-action]');
  if (!button) return;
  const category = categories.find(c => c.id === Number(button.dataset.id));
  if (!category) return;
  if (button.dataset.action === 'edit') {
    openCategory(category);
    return;
  }
  if (!confirm(`Delete “${category.name}”?\n\nA category with expenses cannot be deleted.`)) return;
  try {
    await App.request(`/api/categories/${category.id}`, { method: 'DELETE' });
    App.toast('Category deleted');
    loadCategories();
  } catch (error) {
    App.toast(error.message, 'error');
  }
});

$('categoryForm').onsubmit = async event => {
  event.preventDefault();
  const id = $('categoryId').value;
  try {
    await App.request(id ? `/api/categories/${id}` : '/api/categories', {
      method: id ? 'PUT' : 'POST',
      body: JSON.stringify({ name: $('categoryName').value.trim() })
    });
    $('categoryModal').classList.add('hidden');
    App.toast(id ? 'Category updated' : 'Category created');
    loadCategories();
  } catch (error) {
    $('categoryError').textContent = error.message;
    $('categoryError').classList.remove('hidden');
  }
};

loadCategories();
