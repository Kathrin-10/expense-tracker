const profileForm = document.getElementById('profileForm'),
  fullName = document.getElementById('fullName'),
  email = document.getElementById('email'),
  phone = document.getElementById('phone'),
  profileError = document.getElementById('profileError'),
  passwordForm = document.getElementById('passwordForm'),
  currentPassword = document.getElementById('currentPassword'),
  newPassword = document.getElementById('newPassword'),
  passwordError = document.getElementById('passwordError');
profileForm.addEventListener('submit', async e => {
  e.preventDefault();
  try {
    await App.request('/api/profile', {
      method: 'PUT',
      body: JSON.stringify({
        fullName: fullName.value.trim(),
        email: email.value.trim(),
        phone: phone.value.trim() || null
      })
    });
    App.toast('Profile updated successfully');
  } catch (err) {
    profileError.textContent = err.message;
    profileError.classList.remove('hidden');
  }
});
passwordForm.addEventListener('submit', async e => {
  e.preventDefault();
  passwordError.classList.add('hidden');
  const body = new URLSearchParams({
    currentPassword: currentPassword.value,
    newPassword: newPassword.value
  });
  try {
    await App.request('/api/profile/password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body
    });
    passwordForm.reset();
    App.toast('Password updated successfully');
  } catch (err) {
    passwordError.textContent = err.message;
    passwordError.classList.remove('hidden');
  }
});