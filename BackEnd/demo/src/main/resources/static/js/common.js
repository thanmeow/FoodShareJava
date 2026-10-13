// Shared helpers used by every page.

// All API calls go through this axios instance. After login the server gives us a
// JWT, which we keep in localStorage and send in the Authorization header.
const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

// Called with the { token, user } response from login / register.
function saveLogin(data) {
  localStorage.setItem('token', data.token);
  return data.user;
}

function errorMessage(err) {
  return (err.response && err.response.data && err.response.data.message) || err.message || 'Something went wrong';
}

// Returns the logged-in user, or sends them to the right page.
async function requireUser(role) {
  if (!localStorage.getItem('token')) {
    window.location.href = '/';
    return null;
  }
  try {
    const { data: user } = await api.get('/auth/me');
    if (role && user.role !== role) {
      window.location.href = pageFor(user);
      return null;
    }
    return user;
  } catch (err) {
    // Token expired or invalid
    localStorage.removeItem('token');
    window.location.href = '/';
    return null;
  }
}

function pageFor(user) {
  if (user.role === 'ADMIN') return '/admin.html';
  return user.role === 'DONOR' ? '/donor.html' : '/shelter.html';
}

function logout() {
  localStorage.removeItem('token');
  window.location.href = '/';
}

// Ask the browser for the current GPS position.
function detectLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Your browser does not support location'));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
      () => reject(new Error('Location permission denied - allow it in the address bar and retry')),
      { enableHighAccuracy: true, timeout: 15000 }
    );
  });
}

// Listen to live events from the server. handlers = { 'event-name': (data) => {} }
function connectEvents(handlers) {
  // EventSource can't send headers, so the token goes in the URL
  const token = localStorage.getItem('token');
  const source = new EventSource('/api/events?token=' + encodeURIComponent(token));
  Object.entries(handlers).forEach(([name, handler]) => {
    source.addEventListener(name, (event) => handler(JSON.parse(event.data)));
  });
  return source;
}

// Show a system notification (if allowed) and an on-page toast.
function notify(title, body) {
  showToast(title + (body ? ' - ' + body : ''), 'info');
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(title, { body });
  }
}

function showToast(message, type = 'success') {
  let box = document.getElementById('toasts');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toasts';
    document.body.appendChild(box);
  }
  const toast = document.createElement('div');
  toast.className = 'toast toast-' + type;
  toast.textContent = message;
  box.appendChild(toast);
  setTimeout(() => toast.remove(), 5000);
}

function escapeHtml(value) {
  const div = document.createElement('div');
  div.textContent = value == null ? '' : String(value);
  return div.innerHTML;
}

function formatTime(iso) {
  return new Date(iso).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
}

function timeLeft(iso) {
  const minutes = Math.round((new Date(iso) - Date.now()) / 60000);
  if (minutes <= 0) return 'expired';
  if (minutes < 60) return minutes + ' min left';
  const hours = Math.floor(minutes / 60);
  return hours + ' h ' + (minutes % 60) + ' min left';
}

function isExpired(post) {
  return new Date(post.expiresAt) <= new Date();
}

function contactHtml(title, contact) {
  return `
    <div class="contact">
      <strong>${escapeHtml(title)}</strong>
      <div>${escapeHtml(contact.name)}</div>
      <div>Phone: <a href="tel:${escapeHtml(contact.phone)}">${escapeHtml(contact.phone)}</a></div>
      <div>Email: <a href="mailto:${escapeHtml(contact.email)}">${escapeHtml(contact.email)}</a></div>
      ${contact.address ? `<div>Address: ${escapeHtml(contact.address)}</div>` : ''}
    </div>`;
}

function mapLink(post) {
  return `https://www.google.com/maps?q=${post.latitude},${post.longitude}`;
}

function showUserName(user) {
  document.getElementById('user-name').textContent = user.name;
  document.getElementById('logout-btn').addEventListener('click', logout);
}
