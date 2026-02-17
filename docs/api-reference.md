# API 接口文档

汽车配件管理系统 RESTful API 接口参考

---

## 📋 目录

- [接口说明](#-接口说明)
- [认证方式](#-认证方式)
- [统一响应格式](#-统一响应格式)
- [错误码说明](#-错误码说明)
- [接口列表](#-接口列表)

---

## 📖 接口说明

### 基本信息

- **基础路径**: `/api`
- **数据格式**: JSON
- **字符编码**: UTF-8

### Swagger 文档

交互式 API 文档访问地址：

- **开发环境**: http://localhost:8080/doc.html
- **生产环境**: http://your-domain.com/doc.html

---

## 🔐 认证方式

### JWT Token 认证

需要权限的接口需在请求头中携带 Token：

```
请求头:
token: <your_jwt_token>
```

或

```
请求头:
Authorization: Bearer <your_jwt_token>
```

### 获取 Token

调用登录接口获取 Token：

```bash
POST /api/users/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roleId": 1,
      "roleName": "管理员"
    }
  }
}
```

### Token 说明

- **有效期**: 8 小时（28800 秒）
- **过期处理**: 返回 401 错误，需重新登录
- **刷新机制**: 暂不支持自动刷新，过期后重新登录

---

## 📤 统一响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
  }
}
```

### 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 错误响应

```json
{
  "code": 401,
  "message": "未登录",
  "data": null
}
```

---

## ❌ 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未登录或 Token 无效 | 重新登录获取 Token |
| 403 | 无权限访问 | 联系管理员分配权限 |
| 404 | 资源不存在 | 检查请求路径或资源 ID |
| 500 | 服务器内部错误 | 联系技术支持 |

---

## 📚 接口列表

### 1. 认证与用户

#### 1.1 用户登录

```
POST /api/users/login
```

**请求参数**:

```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800138000",
      "email": "admin@example.com",
      "roleId": 1,
      "roleName": "管理员",
      "status": 1
    }
  }
}
```

#### 1.2 获取当前用户信息

```
GET /api/users/info
Headers: token: <your_jwt_token>
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "roleId": 1,
    "roleName": "管理员",
    "status": 1
  }
}
```

#### 1.3 用户登出

```
POST /api/users/logout
Headers: token: <your_jwt_token>
```

#### 1.4 分页查询用户（管理员）

```
GET /api/users/page?page=1&pageSize=10&username=xxx&realName=xxx&roleId=1
Headers: token: <your_jwt_token>
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "roleId": 1,
        "roleName": "管理员",
        "status": 1,
        "createTime": "2025-01-01T00:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 1.5 新增用户（管理员）

```
POST /api/users
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "realName": "新用户",
  "phone": "13800138001",
  "email": "user@example.com",
  "roleId": 3,
  "status": 1
}
```

#### 1.6 更新用户（管理员）

```
PUT /api/users
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "id": 2,
  "realName": "修改后的名字",
  "phone": "13800138002",
  "roleId": 2,
  "status": 1
}
```

#### 1.7 删除用户（管理员）

```
DELETE /api/users/{id}
Headers: token: <your_jwt_token>
```

---

### 2. 配件管理

#### 2.1 分页查询配件

```
GET /api/parts/page?page=1&pageSize=10&partCode=P001&partName=刹车片&category=制动系统
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "partCode": "P001",
        "partName": "刹车片",
        "specification": "前轮",
        "brand": "Bosch",
        "supplier": "博世汽配",
        "unitPrice": 150.00,
        "category": "制动系统",
        "unit": "件",
        "minStock": 10,
        "status": 1,
        "createTime": "2025-01-01T00:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

#### 2.2 获取配件详情

```
GET /api/parts/{id}
```

#### 2.3 获取所有启用配件

```
GET /api/parts/all
```

#### 2.4 新增配件（管理员/仓库）

```
POST /api/parts
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "partCode": "P002",
  "partName": "机油滤清器",
  "specification": "通用",
  "brand": "Mann",
  "supplier": "曼牌汽配",
  "unitPrice": 50.00,
  "category": "滤清器",
  "unit": "个",
  "minStock": 20,
  "description": "高品质机油滤清器",
  "status": 1
}
```

#### 2.5 更新配件（管理员/仓库）

```
PUT /api/parts
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "id": 1,
  "partName": "更新后的配件名称",
  "unitPrice": 180.00,
  "minStock": 15
}
```

#### 2.6 删除配件（管理员/仓库）

```
DELETE /api/parts/{id}
Headers: token: <your_jwt_token>
```

---

### 3. 库存管理

#### 3.1 分页查询库存

```
GET /api/inventory/page?page=1&pageSize=10&keyword=P001&lowStock=true
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "partId": 1,
        "partNo": "P001",
        "partName": "刹车片",
        "brand": "Bosch",
        "quantity": 5,
        "minQuantity": 10,
        "maxQuantity": 100,
        "warehouseLocation": "A 区 -01 货架",
        "updateTime": "2025-01-19T10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

#### 3.2 根据配件 ID 查询库存

```
GET /api/inventory/part/{partId}
```

#### 3.3 低库存预警列表

```
GET /api/inventory/low-stock
```

#### 3.4 调整库存（管理员/仓库）

```
POST /api/inventory/adjust
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "partId": 1,
  "adjustQuantity": 10,
  "reason": "采购入库"
}
```

#### 3.5 更新库存数量（管理员/仓库）

```
PUT /api/inventory/stock?partId=1&quantity=100
Headers: token: <your_jwt_token>
```

---

### 4. 库存流水

#### 4.1 分页查询流水

```
GET /api/inventory-logs/page?page=1&pageSize=10&partId=1&operationType=IN&relatedOrderNo=PO20250101001
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "partId": 1,
        "partNo": "P001",
        "partName": "刹车片",
        "operationType": "IN",
        "quantity": 50,
        "beforeQuantity": 10,
        "afterQuantity": 60,
        "operatorName": "仓库管理员",
        "relatedOrderNo": "PO20250101001",
        "remark": "采购入库",
        "createTime": "2025-01-01T10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

#### 4.2 获取流水详情

```
GET /api/inventory-logs/{id}
```

---

### 5. 采购管理

#### 5.1 分页查询采购订单

```
GET /api/purchase-orders/page?page=1&pageSize=10&orderNo=PO20250101001&supplier=博世&status=PENDING
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "PO20250101001",
        "supplier": "博世汽配",
        "supplierContact": "李四",
        "supplierPhone": "13800138001",
        "totalAmount": 5000.00,
        "status": "PENDING",
        "createUserId": 1,
        "createUserName": "管理员",
        "remark": "紧急采购",
        "createTime": "2025-01-01T09:00:00"
      }
    ],
    "total": 20,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

#### 5.2 获取采购订单详情

```
GET /api/purchase-orders/{id}
```

#### 5.3 获取采购订单详情（含明细）

```
GET /api/purchase-orders/{id}/detail
```

#### 5.4 新增采购订单（管理员/仓库）

```
POST /api/purchase-orders
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "supplier": "博世汽配",
  "supplierContact": "李四",
  "supplierPhone": "13800138001",
  "remark": "紧急采购",
  "items": [
    {
      "partId": 1,
      "quantity": 50,
      "unitPrice": 100.00
    }
  ]
}
```

#### 5.5 审核采购订单（管理员）

```
PUT /api/purchase-orders/{id}/approve?approveUserId=1&approveUserName=管理员
Headers: token: <your_jwt_token>
```

#### 5.6 完成采购订单（管理员）

```
PUT /api/purchase-orders/{id}/complete
Headers: token: <your_jwt_token>
```

#### 5.7 删除采购订单（管理员）

```
DELETE /api/purchase-orders/{id}
Headers: token: <your_jwt_token>
```

---

### 6. 销售管理

#### 6.1 分页查询销售订单

```
GET /api/sales-orders/page?page=1&pageSize=10&orderNo=SO20250101001&customerName=张三&status=PENDING
```

#### 6.2 获取销售订单详情

```
GET /api/sales-orders/{id}
```

#### 6.3 获取销售订单详情（含明细）

```
GET /api/sales-orders/{id}/detail
```

#### 6.4 新增销售订单（管理员/销售）

```
POST /api/sales-orders
Headers: token: <your_jwt_token>
Content-Type: application/json

{
  "customerName": "张三",
  "customerPhone": "13800138002",
  "customerAddress": "北京市朝阳区",
  "remark": "加急发货",
  "items": [
    {
      "partId": 1,
      "quantity": 2,
      "unitPrice": 150.00
    }
  ]
}
```

#### 6.5 出库（管理员/仓库）

```
PUT /api/sales-orders/{id}/ship?warehouseUserId=2&warehouseUserName=仓库管理员
Headers: token: <your_jwt_token>
```

#### 6.6 完成销售订单（管理员/销售）

```
PUT /api/sales-orders/{id}/complete
Headers: token: <your_jwt_token>
```

#### 6.7 退货（管理员/仓库）

```
PUT /api/sales-orders/{id}/return
Headers: token: <your_jwt_token>
```

#### 6.8 删除销售订单（管理员）

```
DELETE /api/sales-orders/{id}
Headers: token: <your_jwt_token>
```

---

### 7. 数据统计

#### 7.1 出入库统计

```
GET /api/statistics/inventory?startDate=2025-01-01&endDate=2025-01-31&periodType=day
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2025-01-01",
      "inQuantity": 100,
      "outQuantity": 50
    }
  ]
}
```

#### 7.2 库存周转率统计

```
GET /api/statistics/turnover-rate
```

#### 7.3 销售统计

```
GET /api/statistics/sales?startDate=2025-01-01&endDate=2025-01-31&periodType=day
```

#### 7.4 采购统计

```
GET /api/statistics/purchase?startDate=2025-01-01&endDate=2025-01-31&periodType=day
```

#### 7.5 导出统计报表（管理员）

```
GET /api/statistics/export/inventory?startDate=2025-01-01&endDate=2025-01-31
Headers: token: <your_jwt_token>
```

---

### 8. 仪表板

#### 8.1 仪表板统计数据

```
GET /api/dashboard/stats
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalParts": 1258,
    "lowStockCount": 23,
    "todayPurchase": 15680.00,
    "todaySales": 28560.00
  }
}
```

---

## 📝 请求示例

### cURL 示例

```bash
# 登录
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 查询配件列表
curl -X GET "http://localhost:8080/api/parts/page?page=1&pageSize=10" \
  -H "token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 新增配件
curl -X POST http://localhost:8080/api/parts \
  -H "Content-Type: application/json" \
  -H "token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{"partCode":"P002","partName":"机油滤清器","unitPrice":50.00}'
```

### JavaScript 示例

```javascript
// 登录
const login = async (username, password) => {
  const response = await fetch('/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await response.json();
  localStorage.setItem('token', data.data.token);
  return data;
};

// 查询配件列表
const getParts = async (page, pageSize) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`/api/parts/page?page=${page}&pageSize=${pageSize}`, {
    headers: { 'token': token }
  });
  return await response.json();
};
```

---

## 📞 技术支持

如有 API 相关问题，请联系技术支持或查看 Swagger 文档。
