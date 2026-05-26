/* ============================================================
   CS黑市商城 - Sales 模块
   ============================================================ */

const Sales = {
  user: null,
  currentPage: 1,
  pageSize: 15,
  logPage: 1,

  async init(user) {
    this.user = user;
    document.getElementById('app-sales').classList.remove('hidden');
    document.getElementById('app').classList.remove('hidden');
    await this.loadDashboard();
    this.bindEvents();
  },

  bindEvents() {
    document.getElementById('sales-search').addEventListener('input', () => this.loadProducts());
    document.getElementById('sales-cat-filter').addEventListener('change', () => this.loadProducts());
  },

  /* --- 导航 --- */
  showSection(section) {
    ['sales-dashboard','sales-products','sales-logs','sales-categories'].forEach(s => {
      document.getElementById(s).classList.toggle('hidden', s !== section);
    });
    document.querySelectorAll('#sales-app .sidebar .nav-item').forEach(el => el.classList.remove('active'));
    const item = document.querySelector(`[data-sales-section="${section}"]`);
    if (item) item.classList.add('active');

    if (section === 'sales-dashboard') this.loadDashboard();
    if (section === 'sales-products') this.loadProducts();
    if (section === 'sales-logs') this.loadLogs('browse');
    if (section === 'sales-categories') this.loadCategoryList();
  },

  /* --- 仪表板 --- */
  async loadDashboard() {
    try {
      const res = await api.get('/sales/dashboard');
      const d = res.data;
      document.getElementById('sales-stat-total').textContent = d.totalProducts || 0;
      document.getElementById('sales-stat-low').textContent = d.lowStockCount || 0;
      document.getElementById('sales-stat-pending').textContent = d.pendingShip || 0;
      document.getElementById('sales-stat-today').textContent = d.todayOrders || 0;
    } catch (e) { toast('加载仪表板失败', 'error'); }
  },

  /* --- 商品管理 --- */
  async loadProducts() {
    try {
      const kw = encodeURIComponent(document.getElementById('sales-search').value || '');
      const cat = encodeURIComponent(document.getElementById('sales-cat-filter').value || '');
      const res = await api.get(`/product/search?keyword=${kw}&category=${cat}`);
      const tbody = document.getElementById('sales-product-body');
      if (!res.data || !res.data.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="7">暂无商品</td></tr>';
        return;
      }
      tbody.innerHTML = res.data.map(p => `
        <tr>
          <td>${p.id}</td>
          <td>${this.esc(p.name)}</td>
          <td>${this.esc(p.category||'-')}</td>
          <td style="font-family:'JetBrains Mono',monospace">¥${(p.price||0).toFixed(2)}</td>
          <td style="color:${p.stock<=3?'var(--red)':'var(--green)'}">${p.stock}</td>
          <td><span class="status-tag ${p.status===1?'status-shipped':'status-pending'}">${p.status===1?'上架':'下架'}</span></td>
          <td>
            <button class="btn btn-ghost" onclick="Sales.editProduct(${p.id},'${this.esc(p.name)}',${p.price},${p.stock})">编辑</button>
            <button class="btn ${p.status===1?'btn-danger':'btn-success'}" onclick="Sales.toggleStatus(${p.id})">${p.status===1?'下架':'上架'}</button>
          </td>
        </tr>
      `).join('');
    } catch (e) { toast('加载商品失败', 'error'); }
  },

  async toggleStatus(productId) {
    try {
      await api.post('/sales/product/toggle-status', { productId, operatorId: this.user.id });
      toast('状态已更新', 'success');
      await this.loadProducts();
    } catch (e) { toast(e.message, 'error'); }
  },

  editProduct(id, name, price, stock) {
    let old = document.getElementById('sales-edit-modal');
    if (old) old.remove();
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'sales-edit-modal';
    overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
    overlay.innerHTML = `
    <div class="modal" onclick="event.stopPropagation()">
      <h3>编辑商品: ${name}</h3>
      <div class="form-row"><label>价格</label><input type="number" id="edit-price" value="${price}" step="0.01"></div>
      <div class="form-row"><label>库存</label><input type="number" id="edit-stock" value="${stock}"></div>
      <div class="btn-row">
        <button class="btn btn-ghost" onclick="document.getElementById('sales-edit-modal').remove()">取消</button>
        <button class="btn btn-primary" id="edit-save-btn">保存</button>
      </div>
    </div>`;
    document.body.appendChild(overlay);
    document.getElementById('edit-save-btn').onclick = async () => {
      const newPrice = parseFloat(document.getElementById('edit-price').value);
      const newStock = parseInt(document.getElementById('edit-stock').value);
      try {
        await api.post('/sales/product/update', { id, price: newPrice, stock: newStock });
        toast('修改成功', 'success');
        overlay.remove();
        await this.loadProducts();
      } catch(e) { toast(e.message, 'error'); }
    };
  },

  /* --- 日志 --- */
  async loadLogs(type) {
    this.logType = type;
    this.logPage = 1;
    document.querySelectorAll('#sales-app .log-tab').forEach(t => {
      t.classList.toggle('active', t.dataset.logType === type);
    });
    await this.renderLogs();
  },

  async renderLogs() {
    const tbody = document.getElementById('sales-log-body');
    const thead = document.getElementById('sales-log-head');
    try {
      let res;
      if (this.logType === 'browse') {
        res = await api.get(`/sales/logs/browse?page=${this.logPage}&size=${this.pageSize}`);
        thead.innerHTML = '<tr><th>用户ID</th><th>商品ID</th><th>分类</th><th>IP</th><th>时间</th></tr>';
        if (!res.data||!res.data.length) { tbody.innerHTML = '<tr class="empty-row"><td colspan="5">暂无浏览日志</td></tr>'; return; }
        tbody.innerHTML = res.data.map(l => `
          <tr><td>${l.userId||'游客'}</td><td>${l.productId}</td><td>${this.esc(l.category||'-')}</td><td>${l.ip||'-'}</td><td>${l.createTime||''}</td></tr>
        `).join('');
      } else if (this.logType === 'login') {
        res = await api.get(`/sales/logs/login?page=${this.logPage}&size=${this.pageSize}`);
        thead.innerHTML = '<tr><th>用户ID</th><th>用户名</th><th>IP</th><th>角色</th><th>时间</th></tr>';
        if (!res.data||!res.data.length) { tbody.innerHTML = '<tr class="empty-row"><td colspan="5">暂无登录日志</td></tr>'; return; }
        tbody.innerHTML = res.data.map(l => `
          <tr><td>${l.userId}</td><td>${this.esc(l.username||'')}</td><td>${l.ip||'-'}</td><td>${l.role===0?'顾客':l.role===1?'销售':'管理员'}</td><td>${l.loginTime||''}</td></tr>
        `).join('');
      } else {
        res = await api.get(`/sales/logs/operation?page=${this.logPage}&size=${this.pageSize}`);
        thead.innerHTML = '<tr><th>操作者</th><th>操作内容</th><th>IP</th><th>时间</th></tr>';
        if (!res.data||!res.data.length) { tbody.innerHTML = '<tr class="empty-row"><td colspan="4">暂无操作日志</td></tr>'; return; }
        tbody.innerHTML = res.data.map(l => `
          <tr><td>${this.esc(l.operatorName||'')}</td><td>${this.esc(l.operation||'')}</td><td>${l.ip||'-'}</td><td>${l.createTime||''}</td></tr>
        `).join('');
      }
      document.getElementById('sales-log-page').textContent = `第 ${this.logPage} 页`;
    } catch (e) { tbody.innerHTML = '<tr class="empty-row"><td colspan="5">加载失败</td></tr>'; }
  },

  prevLogPage() { if (this.logPage > 1) { this.logPage--; this.renderLogs(); } },
  nextLogPage() { this.logPage++; this.renderLogs(); },

  /* --- 分类管理 --- */
  async loadCategoryList() {
    try {
      const res = await api.get('/category/list');
      const tbody = document.getElementById('sales-cat-body');
      if (!res.data||!res.data.length) { tbody.innerHTML = '<tr class="empty-row"><td colspan="4">暂无分类</td></tr>'; return; }
      tbody.innerHTML = res.data.map(c => `
        <tr><td>${c.id}</td><td>${this.esc(c.name)}</td><td>${c.createTime||''}</td>
        <td><button class="btn btn-danger" onclick="Sales.deleteCategory(${c.id},'${this.esc(c.name)}')">删除</button></td></tr>
      `).join('');
    } catch (e) { /* 静默 */ }
  },

  async deleteCategory(id, name) {
    if (!confirm(`确定删除分类 "${name}" 吗？\n注意：该分类下的商品不会被删除，但分类将不可用。`)) return;
    try {
      await api.delete(`/category/delete?id=${id}`);
      toast('分类已删除', 'success');
      await this.loadCategoryList();
    } catch (e) { toast('删除失败: ' + e.message, 'error'); }
  },

  async addCategory() {
    const inp = document.getElementById('sales-new-cat');
    const name = inp.value.trim();
    if (!name) { toast('请输入分类名称', 'error'); return; }
    try {
      await api.post('/sales/category/add', { name });
      toast('分类添加成功', 'success');
      inp.value = '';
      await this.loadCategoryList();
    } catch (e) { toast(e.message, 'error'); }
  },

  /* --- 新增商品 --- */
  showAddProduct() {
    let old = document.getElementById('sales-add-modal');
    if (old) old.remove();

    // 加载分类列表供下拉选择
    api.get('/category/list').then(res => {
      const catOptions = (res.data || []).map(c => `<option value="${c.name}">${c.name}</option>`).join('');

      const overlay = document.createElement('div');
      overlay.className = 'modal-overlay';
      overlay.id = 'sales-add-modal';
      overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
      overlay.innerHTML = `
      <div class="modal" style="max-width:500px" onclick="event.stopPropagation()">
        <h3>添加新商品</h3>
        <div class="form-row"><label>商品名称</label><input id="add-name" placeholder="武器名称"></div>
        <div class="form-row"><label>分类</label><select id="add-category">${catOptions}</select></div>
        <div class="form-row"><label>价格 (¥)</label><input type="number" id="add-price" step="0.01" placeholder="0.00"></div>
        <div class="form-row"><label>库存</label><input type="number" id="add-stock" placeholder="0"></div>
        <div class="form-row"><label>描述</label><input id="add-desc" placeholder="商品描述"></div>
        <div class="btn-row">
          <button class="btn btn-ghost" onclick="document.getElementById('sales-add-modal').remove()">取消</button>
          <button class="btn btn-primary" id="add-save-btn">添加</button>
        </div>
      </div>`;
      document.body.appendChild(overlay);

      document.getElementById('add-save-btn').onclick = async () => {
        const name = document.getElementById('add-name').value.trim();
        const category = document.getElementById('add-category').value;
        const price = parseFloat(document.getElementById('add-price').value);
        const stock = parseInt(document.getElementById('add-stock').value);
        const description = document.getElementById('add-desc').value.trim();

        if (!name) { toast('请输入商品名称', 'error'); return; }
        if (!category) { toast('请选择分类', 'error'); return; }
        if (isNaN(price) || price <= 0) { toast('请输入有效价格', 'error'); return; }
        if (isNaN(stock) || stock < 0) { toast('请输入有效库存', 'error'); return; }

        try {
          await api.post('/product/add', { name, category, price, stock, description, status: 1 });
          toast('商品添加成功', 'success');
          overlay.remove();
          await this.loadProducts();
          // 刷新分类筛选下拉
          document.getElementById('sales-cat-filter').innerHTML = '<option value="">全部</option>' + catOptions;
        } catch (e) { toast('添加失败: ' + e.message, 'error'); }
      };
    }).catch(e => { toast('加载分类失败', 'error'); });
  },

  esc(s) {
    if (!s) return '';
    const d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
  }
};
