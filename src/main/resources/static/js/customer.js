/* ============================================================
   CS黑市商城 - Customer 模块
   ============================================================ */

const Customer = {
  user: null,
  currentCategory: '全部',
  currentSearch: '',

  async init(user) {
    this.user = user;
    document.getElementById('app-customer').classList.remove('hidden');
    document.getElementById('app').classList.remove('hidden');

    console.log('Customer.init: DOM shown, loading data...');
    await this.loadCategories();
    console.log('Customer.init: categories loaded, loading products...');
    await this.loadProducts();
    console.log('Customer.init: products loaded, count=' + document.getElementById('cust-product-grid').children.length);
    await this.loadCartCount();
    await this.loadPersonalRecs();
    this.bindEvents();
  },

  bindEvents() {
    document.getElementById('cust-search').addEventListener('input', (e) => {
      this.currentSearch = e.target.value;
      this.loadProducts();
    });
    document.getElementById('cust-category-filter').addEventListener('change', (e) => {
      this.currentCategory = e.target.value;
      this.loadProducts();
    });
  },

  /* --- 分类 --- */
  async loadCategories() {
    try {
      const res = await api.get('/category/list');
      const sel = document.getElementById('cust-category-filter');
      sel.innerHTML = '<option value="全部">全部分类</option>';
      (res.data || []).forEach(c => {
        sel.innerHTML += `<option value="${c.name}">${c.name}</option>`;
      });

      const sidebar = document.getElementById('cust-sidebar-cats');
      sidebar.innerHTML = '';
      (res.data || []).forEach(c => {
        const div = document.createElement('div');
        div.className = 'nav-item';
        div.innerHTML = `<span>${c.name}</span>`;
        div.onclick = () => {
          this.currentCategory = c.name;
          document.getElementById('cust-category-filter').value = c.name;
          sidebar.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
          div.classList.add('active');
          this.loadProducts();
        };
        sidebar.appendChild(div);
      });
    } catch (e) { console.error('加载分类失败:', e); }
  },

  /* --- 商品 --- */
  async loadProducts() {
    try {
      const kw = encodeURIComponent(this.currentSearch);
      const cat = encodeURIComponent(this.currentCategory);
      const res = await api.get(`/product/search?keyword=${kw}&category=${cat}`);
      const grid = document.getElementById('cust-product-grid');
      if (!res.data || !res.data.length) {
        grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;padding:60px;color:var(--text-weak)">暂无商品</div>';
        return;
      }
      grid.innerHTML = res.data.map(p => this.productCard(p)).join('');
    } catch (e) {
      document.getElementById('cust-product-grid').innerHTML =
        '<div style="grid-column:1/-1;text-align:center;padding:60px;color:var(--red)">加载失败，请检查服务器</div>';
    }
  },

  productCard(p) {
    const stockClass = p.stock <= 3 ? 'low' : '';
    const stockText = p.stock === 0 ? '售罄' : `库存: ${p.stock}`;
    const disabledAttr = p.stock === 0 ? 'disabled' : '';
    return `
    <div class="product-card" onclick="Customer.viewDetail(${p.id})">
      <div class="img-wrap">
        <img class="product-img" src="/images/${p.image || 'placeholder.svg'}" 
             alt="${this.escapeHtml(p.name)}"
             onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
        <span class="placeholder" style="display:none">${this.categoryIcon(p.category)}</span>
      </div>
      <div class="info">
        <div class="name">${this.escapeHtml(p.name)}</div>
        <span class="category-tag">${this.escapeHtml(p.category || '未分类')}</span>
        <div class="price">¥${(p.price || 0).toFixed(2)}</div>
        <span class="stock ${stockClass}">${stockText}</span>
      </div>
      <div class="footer-btn">
        <button class="btn" ${disabledAttr} onclick="event.stopPropagation();Customer.addToCart(${p.id})">
          ${p.stock === 0 ? '已售罄' : '🛒 加入购物车'}
        </button>
      </div>
    </div>`;
  },

  async viewDetail(productId) {
    const startTime = Date.now();
    try {
      const res = await api.get(`/product/detail?id=${productId}&userId=${this.user.id}`);
      const p = res.data;
      const alsoRecs = await api.get(`/recommend/also-viewed?productId=${productId}&userId=${this.user.id}`);
      this.showDetailModal(p, alsoRecs.data || [], startTime, productId);
    } catch (e) {
      toast('加载商品详情失败: ' + e.message, 'error');
    }
  },

  showDetailModal(product, recs, startTime, productId) {
    let old = document.getElementById('product-detail-modal');
    if (old) old.remove();

    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'product-detail-modal';

    const recordDuration = () => {
      const elapsed = Math.floor((Date.now() - startTime) / 1000);
      if (elapsed > 0 && auth()) {
        api.post(`/product/browse-duration?userId=${auth().id}&productId=${productId}&durationSeconds=${elapsed}`).catch(()=>{});
      }
    };

    overlay.onclick = (e) => { if (e.target === overlay) { recordDuration(); overlay.remove(); } };
    overlay.recordDuration = recordDuration;

    const recHTML = recs.length ? `
      <div class="recommend-section">
        <h3>浏览过此商品的用户也买了</h3>
        <div class="recommend-row">
          ${recs.map(r => `
            <div class="rec-card" onclick="let m=document.getElementById('product-detail-modal');m.recordDuration();m.remove();Customer.viewDetail(${r.id})">
              <div class="rec-name">${this.escapeHtml(r.name)}</div>
              <div class="rec-price">¥${(r.price||0).toFixed(2)}</div>
              <div class="rec-reason">${r.reason}</div>
            </div>
          `).join('')}
        </div>
      </div>` : '';

    overlay.innerHTML = `
    <div class="modal" style="max-width:600px" onclick="event.stopPropagation()">
      <h3>${this.escapeHtml(product.name)}</h3>
      <p style="color:var(--text-dim);margin-bottom:8px">分类: ${this.escapeHtml(product.category||'未分类')}</p>
      <p style="font-size:24px;font-weight:800;color:var(--gold);font-family:'JetBrains Mono',monospace;margin-bottom:12px">
        ¥${(product.price||0).toFixed(2)}
      </p>
      <p style="color:var(--text-dim);margin-bottom:12px">${this.escapeHtml(product.description||'暂无描述')}</p>
      <p style="color:var(--text-dim);margin-bottom:16px">库存: <span style="color:${product.stock<=3?'var(--red)':'var(--green)'}">${product.stock}</span></p>
      ${recHTML}
      <div class="btn-row" style="margin-top:16px">
        <button class="btn btn-ghost" onclick="let m=document.getElementById('product-detail-modal');m.recordDuration();m.remove()">关闭</button>
        <button class="btn btn-primary" ${product.stock===0?'disabled':''}
          onclick="Customer.addToCart(${product.id});let m=document.getElementById('product-detail-modal');m.recordDuration();m.remove()">
          🛒 加入购物车
        </button>
      </div>
    </div>`;
    document.body.appendChild(overlay);
  },

  /* --- 购物车 --- */
  async addToCart(productId) {
    try {
      await api.post('/cart/add', { userId: this.user.id, productId, count: 1 });
      toast('已加入购物车', 'success');
      await this.loadCartCount();
    } catch (e) { toast(e.message, 'error'); }
  },

  async loadCartCount() {
    try {
      const res = await api.get(`/cart/list?userId=${this.user.id}`);
      const count = (res.data || []).reduce((s, i) => s + i.count, 0);
      const badge = document.getElementById('cart-badge');
      badge.textContent = count;
      badge.style.display = count > 0 ? 'inline-flex' : 'none';
    } catch (e) { /* 静默 */ }
  },

  async loadCart() {
    try {
      const res = await api.get(`/cart/list?userId=${this.user.id}`);
      const tbody = document.getElementById('cust-cart-body');
      if (!res.data || !res.data.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="5">购物车是空的</td></tr>';
        document.getElementById('cust-cart-total').textContent = '0.00';
        return;
      }
      let total = 0;
      tbody.innerHTML = res.data.map(item => {
        total += (item.totalMoney || 0);
        return `
        <tr>
          <td>${this.escapeHtml(item.productName)}</td>
          <td style="font-family:'JetBrains Mono',monospace">¥${(item.price||0).toFixed(2)}</td>
          <td>
            <button class="qty-btn" onclick="Customer.decreaseCart(${item.id})">−</button>
            <span class="qty-num">${item.count}</span>
            <button class="qty-btn" onclick="Customer.addToCart(${item.productId})">+</button>
          </td>
          <td style="font-family:'JetBrains Mono',monospace;color:var(--gold)">¥${(item.totalMoney||0).toFixed(2)}</td>
          <td><button class="btn btn-danger" onclick="Customer.removeFromCart(${item.id})">删除</button></td>
        </tr>`;
      }).join('');
      document.getElementById('cust-cart-total').textContent = total.toFixed(2);
    } catch (e) { toast('加载购物车失败', 'error'); }
  },

  async decreaseCart(cartId) {
    try {
      await api.post(`/cart/decrease?id=${cartId}`);
      await this.loadCart();
      await this.loadCartCount();
    } catch (e) { toast(e.message, 'error'); }
  },

  async removeFromCart(cartId) {
    try {
      await api.delete(`/cart/remove?id=${cartId}`);
      toast('已移除', 'success');
      await this.loadCart();
      await this.loadCartCount();
    } catch (e) { toast(e.message, 'error'); }
  },

  async checkout() {
    if (!confirm('确定结算购物车中的所有商品吗？')) return;
    try {
      const res = await api.post(`/order/checkout?userId=${this.user.id}`);
      toast(res.data || '下单成功', 'success');
      await this.loadCart();
      await this.loadCartCount();
      await this.loadOrders();
    } catch (e) { toast(e.message, 'error'); }
  },

  /* --- 订单 --- */
  async loadOrders() {
    try {
      const res = await api.get(`/order/list?userId=${this.user.id}`);
      const tbody = document.getElementById('cust-order-body');
      if (!res.data || !res.data.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="6">暂无订单</td></tr>';
        return;
      }
      tbody.innerHTML = res.data.map(o => {
        let statusHTML = '', btnHTML = '';
        if (o.status === 1) {
          statusHTML = '<span class="status-tag status-pending">待付款</span>';
          btnHTML = `<button class="btn btn-primary" onclick="Customer.pay('${o.orderNo}')">支付</button>`;
        } else if (o.status === 2) {
          statusHTML = '<span class="status-tag status-paid">已付款</span>';
          btnHTML = '<span style="color:var(--text-dim);font-size:12px">等待发货</span>';
        } else if (o.status === 3) {
          statusHTML = '<span class="status-tag status-shipped">已发货</span>';
          btnHTML = '<span style="color:var(--green)">✅ 已完成</span>';
        }
        return `
        <tr>
          <td style="font-family:'JetBrains Mono',monospace;font-size:11px">${o.orderNo.substring(0,8)}...</td>
          <td>${this.escapeHtml(o.productName)}</td>
          <td>${o.count}</td>
          <td style="font-family:'JetBrains Mono',monospace;color:var(--gold)">¥${(o.totalAmount||0).toFixed(2)}</td>
          <td>${statusHTML}</td>
          <td>${btnHTML}</td>
        </tr>`;
      }).join('');
    } catch (e) { toast('加载订单失败', 'error'); }
  },

  async pay(orderNo) {
    try {
      await api.post(`/order/pay?orderNo=${orderNo}`);
      toast('支付成功！', 'success');
      await this.loadOrders();
      await this.loadPersonalRecs();
    } catch (e) { toast(e.message, 'error'); }
  },

  /* --- 个性化推荐 --- */
  async loadPersonalRecs() {
    try {
      const res = await api.get(`/recommend/personal?userId=${this.user.id}`);
      const container = document.getElementById('cust-personal-recs');
      if (!res.data || !res.data.length) {
        container.innerHTML = '';
        return;
      }
      container.innerHTML = `
        <div class="recommend-section">
          <h3>为你推荐</h3>
          <div class="recommend-row">
            ${res.data.map(r => `
              <div class="rec-card" onclick="Customer.viewDetail(${r.id})">
                <div class="rec-name">${this.escapeHtml(r.name)}</div>
                <div class="rec-price">¥${(r.price||0).toFixed(2)}</div>
                <div class="rec-reason">${r.reason}</div>
              </div>
            `).join('')}
          </div>
        </div>`;
    } catch (e) { /* 推荐失败不影响主流程 */ }
  },

  /* --- 工具 --- */
  categoryIcon(cat) {
    const map = { '手枪':'🔫','步枪':'🎯','狙击枪':'🔭','冲锋枪':'💥','装备':'⭐' };
    return map[cat] || '📦';
  },

  escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  },

  /* --- 导航切换 --- */
  showSection(section) {
    ['cust-shop','cust-cart','cust-orders'].forEach(s => {
      document.getElementById(s).classList.toggle('hidden', s !== section);
    });
    document.querySelectorAll('#customer-app .sidebar .nav-item').forEach(el => {
      el.classList.remove('active');
    });
    const item = document.querySelector(`[data-cust-section="${section}"]`);
    if (item) item.classList.add('active');

    if (section === 'cust-cart') this.loadCart();
    if (section === 'cust-orders') this.loadOrders();
    if (section === 'cust-shop') this.loadProducts();
  }
};

/* ============================================================
   GuestCustomer - 游客浏览模式
   ============================================================ */

const GuestCustomer = {
  currentCategory: '全部',
  currentSearch: '',

  async init() {
    await this.loadCategories();
    await this.loadProducts();
    this.bindEvents();
  },

  bindEvents() {
    document.getElementById('cust-search').addEventListener('input', (e) => {
      this.currentSearch = e.target.value;
      this.loadProducts();
    });
    document.getElementById('cust-category-filter').addEventListener('change', (e) => {
      this.currentCategory = e.target.value;
      this.loadProducts();
    });
  },

  async loadCategories() {
    try {
      const res = await api.get('/category/list');
      const sel = document.getElementById('cust-category-filter');
      sel.innerHTML = '<option value="全部">全部分类</option>';
      (res.data || []).forEach(c => {
        sel.innerHTML += `<option value="${c.name}">${c.name}</option>`;
      });

      const sidebar = document.getElementById('cust-sidebar-cats');
      sidebar.innerHTML = '';
      (res.data || []).forEach(c => {
        const div = document.createElement('div');
        div.className = 'nav-item';
        div.innerHTML = `<span>${c.name}</span>`;
        div.onclick = () => {
          this.currentCategory = c.name;
          document.getElementById('cust-category-filter').value = c.name;
          sidebar.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
          div.classList.add('active');
          this.loadProducts();
        };
        sidebar.appendChild(div);
      });
    } catch (e) { console.error('加载分类失败:', e); }
  },

  async loadProducts() {
    try {
      const kw = encodeURIComponent(this.currentSearch);
      const cat = encodeURIComponent(this.currentCategory);
      const res = await api.get(`/product/search?keyword=${kw}&category=${cat}`);
      const grid = document.getElementById('cust-product-grid');
      if (!res.data || !res.data.length) {
        grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;padding:60px;color:var(--text-weak)">暂无商品</div>';
        return;
      }
      grid.innerHTML = res.data.map(p => Customer.productCard(p)).join('');
    } catch (e) {
      document.getElementById('cust-product-grid').innerHTML =
        '<div style="grid-column:1/-1;text-align:center;padding:60px;color:var(--red)">加载失败，请检查服务器</div>';
    }
  },

  viewDetail(productId) {
    // 游客查看详情(不记录浏览)
    api.get(`/product/detail?id=${productId}`).then(res => {
      const p = res.data;
      const overlay = document.createElement('div');
      overlay.className = 'modal-overlay';
      overlay.id = 'product-detail-modal';
      overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
      overlay.innerHTML = `
      <div class="modal" style="max-width:600px" onclick="event.stopPropagation()">
        <h3>${Customer.escapeHtml(p.name)}</h3>
        <p style="color:var(--text-dim);margin-bottom:8px">分类: ${Customer.escapeHtml(p.category||'未分类')}</p>
        <p style="font-size:24px;font-weight:800;color:var(--gold);font-family:'JetBrains Mono',monospace;margin-bottom:12px">
          ¥${(p.price||0).toFixed(2)}
        </p>
        <p style="color:var(--text-dim);margin-bottom:12px">${Customer.escapeHtml(p.description||'暂无描述')}</p>
        <p style="color:var(--text-dim);margin-bottom:16px">库存: <span style="color:${p.stock<=3?'var(--red)':'var(--green)'}">${p.stock}</span></p>
        <div class="btn-row" style="margin-top:16px">
          <button class="btn btn-ghost" onclick="document.getElementById('product-detail-modal').remove()">关闭</button>
          <button class="btn btn-primary" onclick="document.getElementById('product-detail-modal').remove();GuestCustomer.toLogin()">
            🔒 登录后购买
          </button>
        </div>
      </div>`;
      document.body.appendChild(overlay);
    }).catch(e => { toast('加载商品详情失败', 'error'); });
  },

  toLogin() {
    document.getElementById('app').classList.add('hidden');
    document.getElementById('login-page').classList.remove('hidden');
    showAuth('login');
    const guestBtns = document.getElementById('guest-login-btns');
    if (guestBtns) guestBtns.style.display = 'none';
  }
};

// 覆盖 Customer.addToCart 和 Customer.checkout 的游客拦截
const _origAddToCart = Customer.addToCart;
const _origCheckout = Customer.checkout;
Customer.addToCart = function(productId) {
  if (!auth()) { GuestCustomer.toLogin(); toast('请先登录后再购买', 'error'); return; }
  return _origAddToCart.call(this, productId);
};
Customer.checkout = function() {
  if (!auth()) { GuestCustomer.toLogin(); toast('请先登录后再结算', 'error'); return; }
  return _origCheckout.call(this);
};
