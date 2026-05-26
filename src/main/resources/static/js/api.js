/* ============================================================
   CS黑市商城 - 统一API封装
   ============================================================ */

const API_BASE = '';

const api = {
  async request(method, path, data) {
    const opts = {
      method,
      headers: { 'Content-Type': 'application/json' },
    };
    if (data && method !== 'GET') {
      opts.body = JSON.stringify(data);
    }
    const url = path.includes('?') ? `${API_BASE}${path}` : `${API_BASE}${path}`;
    try {
      const res = await fetch(url, opts);
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }
      const json = await res.json();
      if (json.code === 0) {
        throw new Error(json.msg || '请求失败');
      }
      return json;
    } catch (err) {
      if (err.message && !err.message.startsWith('HTTP')) {
        throw err;
      }
      throw new Error('网络请求失败，请检查服务器是否启动');
    }
  },

  get(path)    { return this.request('GET', path); },
  post(path, d){ return this.request('POST', path, d); },
  delete(path) { return this.request('DELETE', path); },
};

/* --- 认证 --- */
function auth() {
  try {
    const u = localStorage.getItem('shop_user');
    return u ? JSON.parse(u) : null;
  } catch { return null; }
}
function saveAuth(user) { localStorage.setItem('shop_user', JSON.stringify(user)); }
function clearAuth()   { localStorage.removeItem('shop_user'); }

/* --- Toast --- */
function toast(msg, type) {
  const cls = type === 'error' ? 'toast-error' : 'toast-success';
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const el = document.createElement('div');
  el.className = `toast ${cls}`;
  el.textContent = msg;
  container.appendChild(el);
  setTimeout(() => { el.remove(); if (!container.children.length) container.remove(); }, 3000);
}

/* --- IP得到函数 (后端需要, 前端不用这个函数, 仅作占位) --- */
/* 所有的 IP 都在后端通过 HttpServletRequest 获取 */
