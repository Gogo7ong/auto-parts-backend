# 后端功能清单（auto-parts-backend）

> 基于代码（`src/main/java/com/djw/autopartsbackend/controller`）整理，按 Controller 列出路由、用途与权限要求。

## 1. 认证与权限

### Token 机制

- 登录成功后返回 `token`（JWT，默认 8 小时过期）。
- 需要权限控制的接口通过请求头 `token` 传入（支持 `Bearer <token>` 或直接传 token）。

### 角色（Role）

系统内置三种角色码（`role.role_code`）：

- `ADMIN`：管理员
- `WAREHOUSE`：仓库管理员
- `SALESMAN`：销售员

### 权限控制方式

- 通过自定义注解 `@RequireRole(...)` 标记需要鉴权的接口。
- `AuthInterceptor` 在命中 `@RequireRole` 时校验 `token`、用户状态与角色是否满足要求。
- 拦截范围：`/api/**`，但会放行登录与文档相关路径（见 `WebConfig`）。

## 2. 首页（HomeController）

- `GET /`：系统首页（欢迎信息、版本号、文档地址）

## 3. 登录（AuthController / UserController）

> 两处均提供登录接口，均返回 `token` 与 `user` 信息。

- `POST /api/user/login`：用户登录
- `POST /api/users/login`：用户登录

## 4. 用户管理（UserController，`/api/users`）

- `GET /api/users/info`：获取当前用户信息（从请求头 `token` 解析用户ID）
- `POST /api/users/logout`：用户登出

管理员权限（`@RequireRole({"ADMIN"})`）：

- `GET /api/users/page`：分页查询用户列表（支持按 `username` / `realName` / `roleId` 筛选）
- `GET /api/users/{id}`：查询用户详情
- `POST /api/users`：新增用户
- `PUT /api/users`：更新用户
- `DELETE /api/users/{id}`：删除用户

## 5. 配件管理（PartController，`/api/parts`）

- `GET /api/parts/all`：获取所有启用配件（用于下拉选择）
- `GET /api/parts/page`：分页查询配件（支持按 `partCode` / `partName` / `category` 筛选）
- `GET /api/parts/{id}`：配件详情

仓库/管理员权限（`@RequireRole({"ADMIN","WAREHOUSE"})`）：

- `POST /api/parts`：新增配件
- `PUT /api/parts`：更新配件
- `DELETE /api/parts/{id}`：删除配件

## 6. 库存管理（InventoryController，`/api/inventory`）

- `GET /api/inventory/page`：分页查询库存（支持 `keyword` 搜索与 `lowStock` 低库存筛选）
- `GET /api/inventory/part/{partId}`：根据配件ID查询库存
- `GET /api/inventory/low-stock`：低库存预警列表

仓库/管理员权限（`@RequireRole({"ADMIN","WAREHOUSE"})`）：

- `POST /api/inventory/adjust`：调整库存（请求体包含 `partId` / `adjustQuantity` / `reason`）
- `PUT /api/inventory/stock`：更新库存数量（query 参数：`partId`、`quantity`）

## 7. 库存流水（InventoryLogController，`/api/inventory-logs`）

- `GET /api/inventory-logs/page`：分页查询流水（支持按 `partId` / `operationType` / `relatedOrderNo` 筛选）
- `GET /api/inventory-logs/{id}`：流水详情

## 8. 采购管理（PurchaseOrderController，`/api/purchase-orders`）

- `GET /api/purchase-orders/page`：分页查询采购订单（支持按 `orderNo` / `supplier` / `status` 筛选）
- `GET /api/purchase-orders/{id}`：采购订单详情
- `GET /api/purchase-orders/{id}/detail`：采购订单详情（包含明细）

仓库/管理员权限（`@RequireRole({"ADMIN","WAREHOUSE"})`）：

- `POST /api/purchase-orders/with-items`：新增采购订单（含明细）
- `POST /api/purchase-orders`：新增采购订单（如未提供 `orderNo` 会自动生成）
- `PUT /api/purchase-orders`：更新采购订单

管理员权限（`@RequireRole({"ADMIN"})`）：

- `PUT /api/purchase-orders/{id}/approve`：审核（query 参数：`approveUserId`、`approveUserName`）
- `PUT /api/purchase-orders/{id}/complete`：完成
- `DELETE /api/purchase-orders/{id}`：删除

## 9. 销售管理（SalesOrderController，`/api/sales-orders`）

- `GET /api/sales-orders/page`：分页查询销售订单（支持按 `orderNo` / `customerName` / `status` 筛选）
- `GET /api/sales-orders/{id}`：销售订单详情
- `GET /api/sales-orders/{id}/detail`：销售订单详情（包含明细）

销售/管理员权限（`@RequireRole({"ADMIN","SALESMAN"})`）：

- `POST /api/sales-orders/with-items`：新增销售订单（含明细）
- `POST /api/sales-orders`：新增销售订单
- `PUT /api/sales-orders`：更新销售订单
- `PUT /api/sales-orders/{id}/complete`：完成

仓库/管理员权限：

- `PUT /api/sales-orders/{id}/ship`：出库（query 参数：`warehouseUserId`、`warehouseUserName`）
- `PUT /api/sales-orders/{id}/return`：退货

管理员权限：

- `DELETE /api/sales-orders/{id}`：删除

## 10. 数据统计与报表（StatisticsController，`/api/statistics`）

- `GET /api/statistics/inventory`：出入库统计（参数：`startDate`、`endDate`、`periodType`）
- `GET /api/statistics/turnover-rate`：库存周转率统计
- `GET /api/statistics/sales`：销售统计（参数：`startDate`、`endDate`、`periodType`）
- `GET /api/statistics/purchase`：采购统计（参数：`startDate`、`endDate`、`periodType`）

管理员权限（导出，`@RequireRole({"ADMIN"})`）：

- `GET /api/statistics/export/inventory`
- `GET /api/statistics/export/turnover-rate`
- `GET /api/statistics/export/sales`
- `GET /api/statistics/export/purchase`

## 11. 仪表板（DashboardController，`/api/dashboard`）

- `GET /api/dashboard/stats`：仪表板汇总统计数据

## 12. 核心业务流程（概述）

- 采购：创建采购单 → 审核（管理员）→ 完成（入库、库存增加、流水记录）
- 销售：创建销售单 → 出库（仓库）→ 完成（库存减少、流水记录）/退货（库存回滚）
- 库存：库存查询、低库存预警、手工调整、库存流水追踪
- 统计：出入库/销售/采购统计与导出（管理员）
