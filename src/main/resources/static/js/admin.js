/* ============================================================
   CS黑市商城 - Admin 模块 (含ECharts数据大屏)
   ============================================================ */

const Admin = {
  user: null,
  logPage: 1,
  logType: 'browse',
  pageSize: 20,

  async init(user) {
    this.user = user;
    document.getElementById('app-admin').classList.remove('hidden');
    document.getElementById('app').classList.remove('hidden');
    await this.loadDashboard();
    this.bindEvents();
  },

  bindEvents() {
    // ECharts 大屏的tab切换
    document.querySelectorAll('.period-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        document.querySelectorAll('.period-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.loadCharts(tab.dataset.period);
      });
    });
  },

  /* --- 导航 --- */
  showSection(section) {
    ['admin-dashboard','admin-salespersons','admin-performance','admin-logs'].forEach(s => {
      document.getElementById(s).classList.toggle('hidden', s !== section);
    });
    document.querySelectorAll('#admin-app .sidebar .nav-item').forEach(el => el.classList.remove('active'));
    const item = document.querySelector(`[data-admin-section="${section}"]`);
    if (item) item.classList.add('active');

    if (section === 'admin-dashboard') this.loadDashboard();
    if (section === 'admin-salespersons') this.loadSalespersons();
    if (section === 'admin-performance') this.loadPerformance();
    if (section === 'admin-logs') this.loadLogs('browse');
  },

  /* ==================== 数据大屏 ==================== */
  async loadDashboard() {
    try {
      const res = await api.get('/admin/dashboard');
      const d = res.data;
      document.getElementById('admin-stat-revenue').textContent = '¥' + ((d.totalRevenue||0)/1).toLocaleString();
      document.getElementById('admin-stat-orders').textContent = d.totalOrders || 0;
      document.getElementById('admin-stat-users').textContent = d.totalUsers || 0;
      document.getElementById('admin-stat-stock').textContent = '¥' + ((d.stockValue||0)/1).toLocaleString();
    } catch (e) { toast('加载仪表板失败', 'error'); }

    await this.loadCharts('daily');
  },

  async loadCharts(period) {
    await Promise.all([
      this.renderTrendChart(period),
      this.renderCategoryPie(),
      this.renderLeaderboardChart(period),
      this.renderAnomalyList(),
    ]);
  },

  async renderTrendChart(period) {
    try {
      const [trendRes, predRes] = await Promise.all([
        api.get(`/analytics/sales-trends?period=${period}`),
        api.get('/analytics/sales-prediction?days=7')
      ]);

      const key = Object.keys(trendRes.data)[0];
      const history = trendRes.data[key] || [];
      const predData = predRes.data || {};
      const forecast = predData['预测'] || [];

      // 合并x轴数据
      const allDates = history.map(d => d.date).concat(forecast.map(d => d.date));
      // 销售额: 历史 + 预测
      const revData = history.map(d => d.revenue || 0).concat(forecast.map(d => d.revenue || 0));
      // 销量: 历史 + 预测
      const qtyData = history.map(d => d.quantity || 0).concat(forecast.map(d => d.quantity || 0));

      const isDaily = period === 'daily';
      const series = [
        { name: '销售额', type: 'line', data: revData,
          smooth: true, lineStyle: { color: '#f0a500' }, itemStyle: { color: '#f0a500' } },
        { name: '销量', type: 'bar', yAxisIndex: 1, data: qtyData,
          itemStyle: { color: 'rgba(52,152,219,0.6)' } }
      ];

      // daily模式下叠加预测虚线
      if (isDaily && forecast.length > 0) {
        const histLen = history.length;
        const predRev = new Array(histLen - 1).fill(null);
        predRev.push(history[histLen - 1].revenue || 0);
        predRev.push(...forecast.map(d => d.revenue || 0));

        const predQty = new Array(histLen - 1).fill(null);
        predQty.push(history[histLen - 1].quantity || 0);
        predQty.push(...forecast.map(d => d.quantity || 0));

        series.push({
          name: '预测销售额', type: 'line', data: predRev,
          smooth: true,
          lineStyle: { color: '#f0a500', type: 'dashed', width: 2 },
          itemStyle: { color: '#f0a500' },
          symbol: 'diamond', symbolSize: 6
        });
        series.push({
          name: '预测销量', type: 'line', yAxisIndex: 1, data: predQty,
          smooth: true,
          lineStyle: { color: '#3498db', type: 'dashed', width: 2 },
          itemStyle: { color: '#3498db' },
          symbol: 'diamond', symbolSize: 6
        });
      }

      const chart = this.getChart('chart-trend');
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: series.map(s => s.name), textStyle: { color: '#9898b8' } },
        grid: { left: 50, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: allDates, axisLabel: { color: '#606078' } },
        yAxis: [
          { type: 'value', name: '销售额(¥)', nameTextStyle: { color: '#606078' }, axisLabel: { color: '#606078' } },
          { type: 'value', name: '销量(件)', nameTextStyle: { color: '#606078' }, axisLabel: { color: '#606078' } }
        ],
        series: series
      });
    } catch (e) { /* 静默 */ }
  },

  async renderCategoryPie() {
    try {
      const res = await api.get('/analytics/category-revenue');
      const chart = this.getChart('chart-category');
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', right: 10, textStyle: { color: '#9898b8' } },
        series: [{
          type: 'pie', radius: ['40%', '70%'], center: ['40%', '50%'],
          data: (res.data||[]).map(d => ({ name: d.label, value: d.value })),
          label: { color: '#9898b8' },
          itemStyle: { borderColor: '#0a0a14', borderWidth: 2 }
        }]
      });
    } catch (e) { /* 静默 */ }
  },

  async renderLeaderboardChart(period) {
    try {
      const res = await api.get(`/analytics/leaderboard?period=${period}`);
      const data = res.data['排行榜'] || [];
      const chart = this.getChart('chart-leaderboard');
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 100, right: 20, top: 10, bottom: 20 },
        xAxis: { type: 'value', axisLabel: { color: '#606078' } },
        yAxis: { type: 'category', data: data.slice(0,10).map(d => d.productName).reverse(),
          axisLabel: { color: '#9898b8', fontSize: 11 } },
        series: [{
          type: 'bar', data: data.slice(0,10).map(d => d.totalSold || 0).reverse(),
          itemStyle: { color: '#f0a500' }, barWidth: 16
        }]
      });
    } catch (e) { /* 静默 */ }
  },

  async renderAnomalyList() {
    try {
      const res = await api.get('/analytics/anomalies');
      const tbody = document.getElementById('anomaly-body');
      if (!res.data || !res.data.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="4">✅ 当前无异常，所有商品销售正常</td></tr>';
        return;
      }
      tbody.innerHTML = res.data.map(a => `
        <tr>
          <td>${this.esc(a.productName)}</td>
          <td>${a.todayQty}</td>
          <td>${a.avgQty}</td>
          <td><span class="status-tag ${a.anomaly.includes('骤增')?'anomaly-up':'anomaly-down'}">${a.anomaly}</span></td>
        </tr>
      `).join('');
    } catch (e) { /* 静默 */ }
  },

  getChart(id) {
    const dom = document.getElementById(id);
    if (!dom) return null;
    let instance = echarts.getInstanceByDom(dom);
    if (!instance) {
      instance = echarts.init(dom, null, { backgroundColor: 'transparent' });
    }
    return instance;
  },

  /* ==================== 销售人员管理 ==================== */
  async loadSalespersons() {
    try {
      const res = await api.get('/admin/salespersons');
      const tbody = document.getElementById('admin-sp-body');
      if (!res.data || !res.data.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="6">暂无销售人员</td></tr>';
        return;
      }
      tbody.innerHTML = res.data.map(u => `
        <tr>
          <td>${u.id}</td>
          <td>${this.esc(u.username)}</td>
          <td>${this.esc(u.name)}</td>
          <td>${u.email||'-'}</td>
          <td>${u.createTime||''}</td>
          <td>
            <button class="btn btn-blue" onclick="Admin.resetPassword(${u.id})">重置密码</button>
            <button class="btn btn-danger" onclick="Admin.deleteSalesperson(${u.id},'${this.esc(u.name)}')">删除</button>
          </td>
        </tr>
      `).join('');
    } catch (e) { toast('加载失败', 'error'); }
  },

  showAddSalesperson() {
    let old = document.getElementById('admin-sp-modal');
    if (old) old.remove();
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'admin-sp-modal';
    overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
    overlay.innerHTML = `
    <div class="modal" onclick="event.stopPropagation()">
      <h3>添加销售人员</h3>
      <div class="form-row"><label>用户名</label><input id="sp-username" placeholder="登录用"></div>
      <div class="form-row"><label>姓名</label><input id="sp-name" placeholder="显示名称"></div>
      <div class="form-row"><label>密码</label><input type="password" id="sp-password" placeholder="初始密码"></div>
      <div class="form-row"><label>邮箱</label><input id="sp-email" placeholder="email@csshop.com"></div>
      <div class="btn-row">
        <button class="btn btn-ghost" onclick="document.getElementById('admin-sp-modal').remove()">取消</button>
        <button class="btn btn-primary" id="sp-save-btn">添加</button>
      </div>
    </div>`;
    document.body.appendChild(overlay);
    document.getElementById('sp-save-btn').onclick = async () => {
      try {
        await api.post('/admin/salesperson/add', {
          username: document.getElementById('sp-username').value,
          password: document.getElementById('sp-password').value,
          name: document.getElementById('sp-name').value,
          email: document.getElementById('sp-email').value,
          operatorId: String(this.user.id)
        });
        toast('销售人员添加成功', 'success');
        overlay.remove();
        await this.loadSalespersons();
      } catch(e) { toast(e.message, 'error'); }
    };
  },

  async deleteSalesperson(userId, name) {
    if (!confirm(`确定删除销售人员 "${name}" 吗？`)) return;
    try {
      await api.delete(`/admin/salesperson/delete?userId=${userId}`);
      toast('已删除', 'success');
      await this.loadSalespersons();
    } catch(e) { toast(e.message, 'error'); }
  },

  async resetPassword(userId) {
    const newPwd = prompt('请输入新密码 (至少6位):');
    if (!newPwd || newPwd.length < 6) { toast('密码至少6位', 'error'); return; }
    try {
      await api.post('/admin/salesperson/reset-password', { userId: String(userId), newPassword: newPwd });
      toast('密码已重置', 'success');
    } catch(e) { toast(e.message, 'error'); }
  },

  /* ==================== 业绩监控 ==================== */
  async loadPerformance() {
    try {
      const [statsRes, profilesRes] = await Promise.all([
        api.get('/admin/stats/sales'),
        api.get('/analytics/user-profiles')
      ]);

      const tb = document.getElementById('admin-perf-body');
      if (!statsRes.data || !statsRes.data.length) {
        tb.innerHTML = '<tr class="empty-row"><td colspan="3">暂无销售数据</td></tr>';
      } else {
        tb.innerHTML = statsRes.data.map(s => `
          <tr>
            <td>${this.esc(s.productName)}</td>
            <td>${s.totalSold}</td>
            <td style="font-family:'JetBrains Mono',monospace;color:var(--gold)">¥${(s.totalRevenue||0).toFixed(2)}</td>
          </tr>
        `).join('');
      }

      // 用户画像
      const profiles = profilesRes.data || {};
      const profileDiv = document.getElementById('admin-profiles');
      let html = '';
      for (const [title, items] of Object.entries(profiles)) {
        html += `<div class="card" style="margin-bottom:12px"><h3 style="color:var(--gold);margin-bottom:8px">${title}</h3>`;
        html += '<div style="display:flex;flex-wrap:wrap;gap:8px">';
        (items||[]).forEach(i => {
          html += `<span style="background:rgba(240,165,0,0.08);padding:4px 12px;border-radius:20px;font-size:12px">${this.esc(i.label)}: <b style="color:var(--gold)">${i.value}</b></span>`;
        });
        html += '</div></div>';
      }
      profileDiv.innerHTML = html;
    } catch(e) { toast('加载业绩数据失败', 'error'); }
  },

  /* ==================== 日志查看 ==================== */
  async loadLogs(type) {
    this.logType = type;
    this.logPage = 1;
    document.querySelectorAll('#admin-app .log-tab').forEach(t => {
      t.classList.toggle('active', t.dataset.logType === type);
    });
    await this.renderLogs();
  },

  async renderLogs() {
    const tbody = document.getElementById('admin-log-body');
    const thead = document.getElementById('admin-log-head');
    try {
      let res;
      if (this.logType === 'browse') {
        res = await api.get(`/admin/logs/browse?page=${this.logPage}&size=${this.pageSize}`);
        thead.innerHTML = '<tr><th>用户ID</th><th>商品ID</th><th>分类</th><th>IP</th><th>时间</th></tr>';
        if (!res.data||!res.data.length) { tbody.innerHTML='<tr class="empty-row"><td colspan="5">暂无数据</td></tr>';return; }
        tbody.innerHTML = res.data.map(l => `<tr><td>${l.userId||'游客'}</td><td>${l.productId}</td><td>${this.esc(l.category||'-')}</td><td>${l.ip||'-'}</td><td>${l.createTime||''}</td></tr>`).join('');
      } else if (this.logType === 'login') {
        res = await api.get(`/admin/logs/login?page=${this.logPage}&size=${this.pageSize}`);
        thead.innerHTML = '<tr><th>用户ID</th><th>用户名</th><th>IP</th><th>角色</th><th>时间</th></tr>';
        if (!res.data||!res.data.length) { tbody.innerHTML='<tr class="empty-row"><td colspan="5">暂无数据</td></tr>';return; }
        tbody.innerHTML = res.data.map(l => `<tr><td>${l.userId}</td><td>${this.esc(l.username||'')}</td><td>${l.ip||'-'}</td><td>${l.role===0?'顾客':l.role===1?'销售':'管理员'}</td><td>${l.loginTime||''}</td></tr>`).join('');
      } else {
        res = await api.get(`/admin/logs/operation?page=${this.logPage}&size=${this.pageSize}`);
        thead.innerHTML = '<tr><th>操作者</th><th>操作内容</th><th>IP</th><th>时间</th></tr>';
        if (!res.data||!res.data.length) { tbody.innerHTML='<tr class="empty-row"><td colspan="4">暂无数据</td></tr>';return; }
        tbody.innerHTML = res.data.map(l => `<tr><td>${this.esc(l.operatorName||'')}</td><td>${this.esc(l.operation||'')}</td><td>${l.ip||'-'}</td><td>${l.createTime||''}</td></tr>`).join('');
      }
      document.getElementById('admin-log-page').textContent = `第 ${this.logPage} 页`;
    } catch (e) { tbody.innerHTML = '<tr class="empty-row"><td colspan="5">加载失败</td></tr>'; }
  },

  prevLogPage() { if (this.logPage > 1) { this.logPage--; this.renderLogs(); } },
  nextLogPage() { this.logPage++; this.renderLogs(); },

  esc(s) {
    if (!s) return '';
    const d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
  }
};
