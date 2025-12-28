# 在线商城系统 (Online Shopping System)

## 👤 作者信息
* **学号**：202330452131
* **姓名**：叶尊

## 📝 项目简介
本项目是一个基于 Spring Boot + MySQL + Docker + MyBatis-Plus 的在线购物系统课程设计，实现了电商系统中常见的用户端购物功能与管理员端管理功能。
系统采用前后端一体化方式开发，后端负责业务逻辑与数据库交互。
（注：管理员账号密码为admin admin123）

## 📂 技术栈
### 后端
* Java 17
* Spring Boot 3.2.1
* MyBatis-Plus
* MySQL 8.x
* HikariCP
* Maven

### 前端
* HTML / CSS / JavaScript
（基于 Spring Boot 静态资源与模板引擎渲染）

### 运行环境
* IntelliJ IDEA
* Docker（用于 MySQL）
* JDK 17+

## 系统功能说明
### 用户端功能
* 用户注册
* 用户登录 / 注销
* 商品列表展示（按数据库商品信息加载）
* 商品浏览与查询
* 添加商品至购物车
* 下单与付款流程（模拟）
* 订单状态查看
* 历史订单查询

### 管理员端功能
* 商品管理
* 添加商品
* 修改商品信息
* 删除商品
* 商品库存维护
* 订单管理
* 销售数据统计（基础统计）

## 源代码结构说明

```text
src
└── main
    ├── java
    │   └── com.shop.onlineshopping
    │       ├── controller
    │       │   控制器层：处理前端请求
    │       ├── service
    │       │   业务逻辑层：实现系统核心功能
    │       ├── mapper
    │       │   数据访问层：与数据库交互
    │       ├── entity
    │       │   实体类：对应数据库表
    │       └── OnlineShoppingApplication.java
    │           项目启动类
    └── resources
        ├── mapper
        │   MyBatis 映射文件
        ├── static
        │   静态资源（页面、商品图片）
        └── application.properties
            项目配置文件

```


## 数据库设计
### 数据库名称
* product

### 商品数据说明
商品数据基于 CS2（Counter-Strike 2）装备与武器，包括：
* 装备
* 手枪
* 中级武器
* 步枪
* 狙击枪





### ⚠️ 重要提示 (测试必读)
> **记得每次换账号测试前（例如从管理员切换到顾客），一定要先点击页面右上角的“退出登录”！**
>
> *说明：本项目使用浏览器缓存 (LocalStorage) 保持登录状态。如果不点击退出直接刷新页面，系统会自动恢复上一次的登录身份。*

---
