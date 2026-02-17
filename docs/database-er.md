# 数据库设计文档

本文档描述汽车配件管理系统的数据库设计，包括 ER 图、表结构说明和关系说明。

---

## 📋 目录

- [数据库 ER 图](#-数据库 er 图)
- [表结构说明](#-表结构说明)
- [表关系说明](#-表关系说明)
- [索引设计](#-索引设计)
- [数据字典](#-数据字典)

---

## 🗺️ 数据库 ER 图

```
┌─────────────────┐       ┌─────────────────┐
│     role        │       │      part       │
├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │
│ role_name       │       │ part_code       │
│ role_code       │       │ part_name       │
│ description     │       │ specification   │
│ create_time     │       │ brand           │
│ update_time     │       │ supplier        │
└────────┬────────┘       │ unit_price      │
         │                │ category        │
         │ 1:N            │ unit            │
         ▼                │ min_stock       │
┌─────────────────┐       │ description     │
│      user       │       │ status          │
├─────────────────┤       │ create_time     │
│ id              │       │ update_time     │
│ username        │       └────────┬────────┘
│ password        │                │ 1:1
│ real_name       │                ▼
│ phone           │       ┌─────────────────┐
│ email           │       │   inventory     │
│ role_id (FK)    │       ├─────────────────┤
│ status          │       │ id              │
│ create_time     │       │ part_id (FK)    │
│ update_time     │       │ stock_quantity  │
└────────┬────────┘       │ warehouse_loc   │
         │                │ last_update_time│
         │ 1:N            └────────┬────────┘
         │                         │ 1:N
         ▼                         ▼
┌─────────────────┐       ┌─────────────────┐
│ purchase_order  │       │ inventory_log   │
├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │
│ order_no        │       │ part_id (FK)    │
│ supplier        │       │ operation_type  │
│ supplier_contact│       │ quantity        │
│ supplier_phone  │       │ before_quantity │
│ total_amount    │       │ after_quantity  │
│ status          │       │ operator_id(FK) │
│ create_user_id  │       │ operator_name   │
│ approve_user_id │       │ related_order_no│
│ approve_time    │       │ remark          │
│ remark          │       │ create_time     │
│ create_time     │       └─────────────────┘
│ update_time     │
└────────┬────────┘
         │ 1:N
         ▼
┌─────────────────┐
│purchase_order_  │
│     item        │
├─────────────────┤
│ id              │
│ order_id (FK)   │
│ part_id (FK)    │
│ part_code       │
│ part_name       │
│ quantity        │
│ unit_price      │
│ total_price     │
│ received_quantity│
│ remark          │
│ create_time     │
└─────────────────┘

┌─────────────────┐
│   sales_order   │
├─────────────────┤
│ id              │
│ order_no        │
│ customer_name   │
│ customer_phone  │
│ customer_address│
│ total_amount    │
│ status          │
│ create_user_id  │
│ warehouse_user_id│
│ warehouse_time  │
│ remark          │
│ create_time     │
│ update_time     │
└────────┬────────┘
         │ 1:N
         ▼
┌─────────────────┐
│sales_order_item │
├─────────────────┤
│ id              │
│ order_id (FK)   │
│ part_id (FK)    │
│ part_code       │
│ part_name       │
│ quantity        │
│ unit_price      │
│ total_price     │
│ shipped_quantity│
│ remark          │
│ create_time     │
└─────────────────┘
```

---

## 📊 表结构说明

### 1. role - 角色表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| role_name | VARCHAR(50) | NOT NULL | 角色名称 |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE | 角色编码 |
| description | VARCHAR(255) | - | 描述 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**: `idx_role_code`

**初始数据**:
```sql
INSERT INTO role (role_name, role_code, description) VALUES
('管理员', 'ADMIN', '系统管理员，拥有所有权限'),
('仓库员', 'WAREHOUSE', '仓库管理员，负责配件和库存管理'),
('销售员', 'SALESMAN', '销售人员，负责销售订单管理');
```

---

### 2. user - 用户表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt 加密） |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| phone | VARCHAR(20) | - | 手机号 |
| email | VARCHAR(100) | - | 邮箱 |
| role_id | BIGINT | NOT NULL, FK | 角色 ID |
| status | TINYINT | DEFAULT 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**: `idx_username`, `idx_phone`

**外键**: `role_id` → `role(id)`

---

### 3. part - 配件表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| part_code | VARCHAR(50) | NOT NULL, UNIQUE | 配件编号 |
| part_name | VARCHAR(100) | NOT NULL | 配件名称 |
| specification | VARCHAR(100) | - | 规格 |
| brand | VARCHAR(50) | - | 品牌 |
| supplier | VARCHAR(100) | - | 供应商 |
| unit_price | DECIMAL(10,2) | NOT NULL | 单价 |
| category | VARCHAR(50) | - | 分类 |
| unit | VARCHAR(20) | DEFAULT '件' | 单位 |
| min_stock | INT | DEFAULT 10 | 最小库存阈值 |
| description | TEXT | - | 描述 |
| status | TINYINT | DEFAULT 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**: `idx_part_code`, `idx_part_name`, `idx_category`

---

### 4. inventory - 库存表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| part_id | BIGINT | NOT NULL, UNIQUE, FK | 配件 ID |
| stock_quantity | INT | DEFAULT 0 | 库存数量 |
| warehouse_location | VARCHAR(100) | - | 仓库位置 |
| last_update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 最后更新时间 |

**索引**: `idx_stock_quantity`

**外键**: `part_id` → `part(id)` (ON DELETE CASCADE)

---

### 5. inventory_log - 库存流水表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| part_id | BIGINT | NOT NULL, FK | 配件 ID |
| operation_type | VARCHAR(20) | NOT NULL | 操作类型：IN/OUT/TRANSFER/CHECK |
| quantity | INT | NOT NULL | 数量 |
| before_quantity | INT | NOT NULL | 操作前数量 |
| after_quantity | INT | NOT NULL | 操作后数量 |
| operator_id | BIGINT | -, FK | 操作人 ID |
| operator_name | VARCHAR(50) | - | 操作人姓名 |
| related_order_no | VARCHAR(50) | - | 关联订单号 |
| remark | VARCHAR(255) | - | 备注 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**: `idx_part_id`, `idx_operation_type`, `idx_create_time`, `idx_related_order_no`

**外键**: 
- `part_id` → `part(id)` (ON DELETE CASCADE)
- `operator_id` → `user(id)` (ON DELETE SET NULL)

---

### 6. purchase_order - 采购订单表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | 订单编号 |
| supplier | VARCHAR(100) | NOT NULL | 供应商 |
| supplier_contact | VARCHAR(50) | - | 供应商联系人 |
| supplier_phone | VARCHAR(20) | - | 供应商电话 |
| total_amount | DECIMAL(12,2) | DEFAULT 0.00 | 总金额 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态 |
| create_user_id | BIGINT | NOT NULL, FK | 创建人 ID |
| create_user_name | VARCHAR(50) | - | 创建人姓名 |
| approve_user_id | BIGINT | -, FK | 审核人 ID |
| approve_user_name | VARCHAR(50) | - | 审核人姓名 |
| approve_time | DATETIME | - | 审核时间 |
| remark | VARCHAR(500) | - | 备注 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**: `idx_order_no`, `idx_status`, `idx_create_time`

**外键**: 
- `create_user_id` → `user(id)` (ON DELETE RESTRICT)
- `approve_user_id` → `user(id)` (ON DELETE SET NULL)

**状态说明**:
- `PENDING`: 待审核
- `APPROVED`: 已审核
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

---

### 7. purchase_order_item - 采购订单明细表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| order_id | BIGINT | NOT NULL, FK | 订单 ID |
| part_id | BIGINT | NOT NULL, FK | 配件 ID |
| part_code | VARCHAR(50) | - | 配件编号 |
| part_name | VARCHAR(100) | - | 配件名称 |
| quantity | INT | NOT NULL | 数量 |
| unit_price | DECIMAL(10,2) | NOT NULL | 单价 |
| total_price | DECIMAL(12,2) | NOT NULL | 总价 |
| received_quantity | INT | DEFAULT 0 | 已入库数量 |
| remark | VARCHAR(255) | - | 备注 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**: `idx_order_id`, `idx_part_id`

**外键**: 
- `order_id` → `purchase_order(id)` (ON DELETE CASCADE)
- `part_id` → `part(id)` (ON DELETE RESTRICT)

---

### 8. sales_order - 销售订单表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | 订单编号 |
| customer_name | VARCHAR(100) | NOT NULL | 客户名称 |
| customer_phone | VARCHAR(20) | - | 客户电话 |
| customer_address | VARCHAR(255) | - | 客户地址 |
| total_amount | DECIMAL(12,2) | DEFAULT 0.00 | 总金额 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态 |
| create_user_id | BIGINT | NOT NULL, FK | 创建人 ID |
| create_user_name | VARCHAR(50) | - | 创建人姓名 |
| warehouse_user_id | BIGINT | -, FK | 出库人 ID |
| warehouse_user_name | VARCHAR(50) | - | 出库人姓名 |
| warehouse_time | DATETIME | - | 出库时间 |
| remark | VARCHAR(500) | - | 备注 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**: `idx_order_no`, `idx_status`, `idx_create_time`

**外键**: 
- `create_user_id` → `user(id)` (ON DELETE RESTRICT)
- `warehouse_user_id` → `user(id)` (ON DELETE SET NULL)

**状态说明**:
- `PENDING`: 待出库
- `SHIPPED`: 已出库
- `COMPLETED`: 已完成
- `RETURNED`: 已退货

---

### 9. sales_order_item - 销售订单明细表

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 ID |
| order_id | BIGINT | NOT NULL, FK | 订单 ID |
| part_id | BIGINT | NOT NULL, FK | 配件 ID |
| part_code | VARCHAR(50) | - | 配件编号 |
| part_name | VARCHAR(100) | - | 配件名称 |
| quantity | INT | NOT NULL | 数量 |
| unit_price | DECIMAL(10,2) | NOT NULL | 单价 |
| total_price | DECIMAL(12,2) | NOT NULL | 总价 |
| shipped_quantity | INT | DEFAULT 0 | 已出库数量 |
| remark | VARCHAR(255) | - | 备注 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**: `idx_order_id`, `idx_part_id`

**外键**: 
- `order_id` → `sales_order(id)` (ON DELETE CASCADE)
- `part_id` → `part(id)` (ON DELETE RESTRICT)

---

## 🔗 表关系说明

### 一对多关系

| 主表 | 从表 | 关系说明 |
|------|------|----------|
| role | user | 一个角色对应多个用户 |
| part | inventory | 一个配件对应一个库存记录（1:1） |
| part | inventory_log | 一个配件对应多条库存流水 |
| purchase_order | purchase_order_item | 一个采购订单对应多个明细 |
| sales_order | sales_order_item | 一个销售订单对应多个明细 |
| user | purchase_order | 一个用户可创建多个采购订单 |
| user | sales_order | 一个用户可创建多个销售订单 |

### 关系图

```
role ──────< user
                    │
                    │ (创建)
                    ▼
part ──────< inventory    purchase_order >────── user
   │              │              │
   │              │              │ (包含)
   │              ▼              ▼
   │        inventory_log   purchase_order_item
   │
   │ (包含)
   ▼
sales_order >────── user
   │
   │ (包含)
   ▼
sales_order_item
```

---

## 📇 索引设计

### 索引优化原则

1. **主键索引**: 每张表都有主键索引
2. **外键索引**: 所有外键字段都建立索引
3. **查询索引**: 经常用于查询条件的字段建立索引
4. **唯一索引**: 保证数据唯一性的字段建立唯一索引

### 索引列表

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| role | idx_role_code | role_code | 普通索引 | 角色编码查询 |
| user | idx_username | username | 普通索引 | 用户名查询 |
| user | idx_phone | phone | 普通索引 | 手机号查询 |
| part | idx_part_code | part_code | 普通索引 | 配件编号查询 |
| part | idx_part_name | part_name | 普通索引 | 配件名称查询 |
| part | idx_category | category | 普通索引 | 分类查询 |
| inventory | idx_stock_quantity | stock_quantity | 普通索引 | 库存数量查询 |
| inventory_log | idx_part_id | part_id | 普通索引 | 配件 ID 查询 |
| inventory_log | idx_operation_type | operation_type | 普通索引 | 操作类型查询 |
| inventory_log | idx_create_time | create_time | 普通索引 | 时间查询 |
| inventory_log | idx_related_order_no | related_order_no | 普通索引 | 订单号查询 |
| purchase_order | idx_order_no | order_no | 普通索引 | 订单号查询 |
| purchase_order | idx_status | status | 普通索引 | 状态查询 |
| purchase_order | idx_create_time | create_time | 普通索引 | 创建时间查询 |
| purchase_order_item | idx_order_id | order_id | 普通索引 | 订单 ID 查询 |
| purchase_order_item | idx_part_id | part_id | 普通索引 | 配件 ID 查询 |
| sales_order | idx_order_no | order_no | 普通索引 | 订单号查询 |
| sales_order | idx_status | status | 普通索引 | 状态查询 |
| sales_order | idx_create_time | create_time | 普通索引 | 创建时间查询 |
| sales_order_item | idx_order_id | order_id | 普通索引 | 订单 ID 查询 |
| sales_order_item | idx_part_id | part_id | 普通索引 | 配件 ID 查询 |

---

## 📖 数据字典

### 状态枚举

#### user.status
| 值 | 说明 |
|----|------|
| 0 | 禁用 |
| 1 | 启用 |

#### part.status
| 值 | 说明 |
|----|------|
| 0 | 禁用 |
| 1 | 启用 |

#### inventory_log.operation_type
| 值 | 说明 |
|----|------|
| IN | 入库 |
| OUT | 出库 |
| TRANSFER | 调拨 |
| CHECK | 盘点 |

#### purchase_order.status
| 值 | 说明 |
|----|------|
| PENDING | 待审核 |
| APPROVED | 已审核 |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |

#### sales_order.status
| 值 | 说明 |
|----|------|
| PENDING | 待出库 |
| SHIPPED | 已出库 |
| COMPLETED | 已完成 |
| RETURNED | 已退货 |

---

## 📝 SQL 脚本说明

| 脚本文件 | 说明 |
|----------|------|
| init.sql | 数据库初始化脚本，创建所有表和初始数据 |
| mock_data.sql | 模拟数据脚本，用于测试 |
| update_20250212.sql | 数据库更新脚本 |
| add_deleted_field.sql | 添加逻辑删除字段脚本 |

---

## 🔧 数据库维护

### 备份脚本

```bash
#!/bin/bash
mysqldump -u root -p auto-parts-backend > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 恢复脚本

```bash
mysql -u root -p auto-parts-backend < backup_20250101_120000.sql
```

---

## 📞 技术支持

如有数据库相关问题，请联系数据库管理员。
