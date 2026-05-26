# CS黑市商城 — 网络应用架构设计与开发课程设计

## 👤 作者信息
- **学号**：202330452131
- **姓名**：叶尊
- **代码托管**：https://github.com/JOSEPHF691/Online-Shopping
- **在线部署**：http://123.57.251.90:8081

---

## 📝 项目简介

本项目是一个面向 CS2（Counter-Strike 2）游戏玩家的武器皮肤交易平台，基于 **Spring Boot 3.x + MyBatis-Plus + MySQL 8.0 + 原生前端** 技术栈开发。

系统支持 **三类用户角色**（顾客/销售员/管理员），实现了完整的电商购物流程、**大数据采集与分析**、**协同过滤推荐系统**、**ECharts 数据可视化大屏** 等功能。

---

## 🎯 功能概览

### 👤 顾客（Customer）
- 新用户注册 / 登录 / 注销
- 未登录游客浏览商品
- 商品分类筛选 + 关键词搜索
- 商品详情查看（自动记录浏览时长）
- 加入购物车 → 结算 → 支付 → 邮件通知
- 个性化推荐（协同过滤 UserCF）
- 关联推荐（"浏览过此商品的用户也买了"）

### 📦 销售员（Sales）
- 商品目录管理（添加/删除分类）
- 商品信息管理（新增、修改价格/库存、上下架）
- 销售仪表板（商品总数、库存告警、待发货、今日订单）
- 用户浏览 / 登录 / 操作日志查看

### 👑 管理员（Admin）
- 数据大屏（ECharts 可视化：销售趋势、类别占比、排行榜）
- 销售趋势预测（移动平均 + 线性回归）
- 销售异常检测（均值±2σ 自动判别）
- 用户画像分析（地域分布、购买力分档、偏好分类）
- 销售人员 ID 管理（添加/删除/密码重置）
- 销售业绩查询与监控
- 全部日志查看

### 📊 大数据系统
| 数据类型 | 采集内容 | 数据表 |
|---------|---------|--------|
| 登录信息 | 时间、IP 地址 | `login_log` |
| 浏览行为 | 商品类别、停留时长（秒） | `browse_log` |
| 购买记录 | 商品类别、日期、单价、数量 | `orders` |
| 操作日志 | 操作时间、内容、IP、账号 | `operation_log` |

### 🤖 推荐系统
- **简单推荐**："浏览过此商品的用户也买了"（基于共现统计）
- **协同过滤**：UserCF 算法 + Jaccard 相似度 + 三级降级策略

---

## 🛠 技术栈

| 层级 | 技术 |
|:---|:---|
| 后端框架 | Spring Boot 3.2.1 + MyBatis-Plus |
| 数据库 | MySQL 8.0（Docker 容器化） |
| 前端 | 原生 HTML / CSS / JavaScript（SPA 单页应用） |
| 可视化 | ECharts 5.5.0（CDN） |
| 邮件服务 | Spring Mail（QQ邮箱 SMTP） |
| 构建工具 | Maven Wrapper |
| 部署 | 阿里云轻量应用服务器（Ubuntu 24.04 + Docker） |
| AI 辅助 | Claude Code |

---

## 📂 项目结构

```
Online-Shopping/
├── pom.xml                           # Maven 依赖配置
├── init.sql                          # 数据库初始化（8张表 + 种子数据）
├── mvnw / mvnw.cmd                   # Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/shop/onlineshopping/
│   │   │   ├── controller/           # 9个控制器
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── OrdersController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── SalesController.java
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   └── RecommendController.java
│   │   │   ├── service/              # 9个服务类
│   │   │   ├── mapper/               # 8个Mapper接口
│   │   │   ├── entity/               # 8个实体类
│   │   │   ├── vo/                   # 8个视图对象
│   │   │   ├── dto/                  # 2个传输对象
│   │   │   └── common/               # 统一响应 Result.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/               # 前端静态资源
│   │           ├── index.html        # SPA主页面（三角色路由）
│   │           ├── css/style.css     # 暗色战术风样式
│   │           ├── js/
│   │           │   ├── api.js        # 统一API封装
│   │           │   ├── customer.js   # 顾客模块
│   │           │   ├── sales.js      # 销售员模块
│   │           │   └── admin.js      # 管理员模块
│   │           └── images/           # 商品图片
```

---

## 🚀 快速启动

### 环境要求
- JDK 17+
- Docker（用于运行 MySQL）
- 无需预装 Maven（使用 Maven Wrapper）

### 步骤

```bash
# 1. 启动 MySQL 容器
docker run -d --name mysql8 \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=product \
  -e MYSQL_USER=shop \
  -e MYSQL_PASSWORD=shop123 \
  mysql:8.0

# 2. 初始化数据库
mysql -h 127.0.0.1 -P 3307 -u shop -pshop123 product < init.sql

# 3. 启动应用
./mvnw spring-boot:run

# 4. 访问
# http://localhost:8081
```

---

## 🔑 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|:---|:---|:---:|:---|
| 👑 管理员 | admin | admin123 | 数据大屏、人员管理、业绩查询 |
| 📦 销售员 | seller1 | seller123 | 商品管理、分类管理、日志查看 |
| 📦 销售员 | seller2 | seller123 | 测试多销售员场景 |
| 🛒 顾客 | player1 | 123456 | CS玩家-小明（广东） |
| 🛒 顾客 | player2 | 123456 | CS玩家-小红（北京） |
| 🛒 顾客 | player3 | 123456 | CS玩家-大刘（四川） |
| 👤 游客 | 无需登录 | - | 可浏览商品，购买时引导登录 |

---

## 🤖 AI 辅助编程

本项目使用 **Claude Code**（Anthropic）作为 AI 编程助手。

- **使用频率**：每天约 1~2 小时，占编码时间 40%~50%
- **主要用途**：代码生成（CRUD模板）、调试排错、文档编写
- **AI 高效任务**：模板代码生成（效率 +80%）、样式调试（+70%）、SQL编写（+60%）
- **AI 有限任务**：协同过滤算法调优、前端架构决策、Docker 网络排障

详见报告「AI工具使用记录」章节。

---

## 📋 数据库设计（8张表）

| 表名 | 说明 |
|:---|:---|
| `user` | 用户表（支持 role=0/1/2 三角色） |
| `product` | 商品表（18 种 CS2 武器皮肤） |
| `cart` | 购物车表 |
| `orders` | 订单表 |
| `category` | 商品分类表 |
| `browse_log` | 浏览日志表（大数据采集） |
| `login_log` | 登录日志表（大数据采集） |
| `operation_log` | 操作日志表（大数据采集） |

---

## ⚠️ 注意事项

> **每次换账号测试前（例如从管理员切换到顾客），请先点击页面右上角的"退出"按钮。**
>
> 本项目使用 LocalStorage 保持登录状态，不点击退出直接刷新页面会自动恢复上一次的登录身份。
