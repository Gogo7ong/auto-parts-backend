# 汽车配件管理系统 - 后端服务

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.5-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**基于 Spring Boot 3 + MyBatis-Plus 的汽车配件管理系统后端服务**

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [API 文档](#-api-文档) • [配置说明](#-配置说明)

</div>

---

## 📋 项目简介

本系统是汽车配件管理系统的后端服务，提供 RESTful API 接口，支持配件管理、库存管理、采购管理、销售管理、数据统计等功能。采用 JWT 进行身份认证，基于 RBAC 模型实现角色权限控制。

### 核心特性

- 🔐 **安全认证**：JWT Token 认证，支持角色权限控制
- 📊 **数据统计**：多维度数据统计与可视化，支持 Excel 导出
- 📦 **库存管理**：实时库存监控，低库存预警
- 🛒 **采购销售**：完整的采购和销售订单流程
- 🔍 **搜索过滤**：支持多条件分页查询
- 📝 **操作日志**：库存流水全程追踪

---

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **框架** | Spring Boot | 3.5.9 |
| **ORM** | MyBatis-Plus | 3.5.5 |
| **数据库** | MySQL | 8.0+ |
| **安全** | Spring Security Crypto + JWT | - |
| **文档** | SpringDoc OpenAPI 3 | 2.7.0 |
| **Excel** | EasyExcel | 3.3.2 |
| **工具** | Hutool | 5.8.24 |
| **其他** | Lombok, Validation | - |

---

## 📦 项目结构

```
auto-parts-backend/
├── src/main/java/com/djw/autopartsbackend/
│   ├── controller/          # 控制器层（REST API）
│   ├── service/             # 服务层（业务逻辑）
│   ├── mapper/              # 数据访问层
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── vo/                  # 视图对象
│   ├── common/              # 公共类（Result、异常等）
│   ├── config/              # 配置类
│   ├── security/            # 安全相关（JWT、拦截器）
│   └── util/                # 工具类
├── src/main/resources/
│   ├── mapper/              # MyBatis XML 映射文件
│   ├── sql/                 # SQL 脚本
│   ├── application.yml      # 应用配置
│   └── logback-spring.xml   # 日志配置
├── docs/                    # 项目文档
├── pom.xml                  # Maven 配置
└── README.md                # 项目说明
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 1. 克隆项目

```bash
git clone <repository-url>
cd auto-parts-management-system/auto-parts-backend
```

### 2. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/sql/init.sql

# （可选）执行模拟数据脚本
source src/main/resources/sql/mock_data.sql
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auto-parts-backend?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 4. 启动项目

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run

# 或直接打包运行
mvn clean package
java -jar target/auto-parts-backend-0.0.1-SNAPSHOT.jar
```

### 5. 访问应用

- **应用首页**: http://localhost:8080
- **Swagger API 文档**: http://localhost:8080/doc.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 🔌 API 接口说明

### 认证方式

需要权限的接口需在请求头中携带 Token：

```
token: <your_jwt_token>
```

### 角色说明

| 角色码 | 名称 | 权限说明 |
|--------|------|----------|
| `ADMIN` | 管理员 | 拥有所有权限 |
| `WAREHOUSE` | 仓库管理员 | 配件、库存、采购管理 |
| `SALESMAN` | 销售员 | 销售订单管理 |

### 主要接口模块

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/users/login` | 用户登录 |
| 用户管理 | `/api/users` | 用户 CRUD |
| 配件管理 | `/api/parts` | 配件 CRUD |
| 库存管理 | `/api/inventory` | 库存查询、调整 |
| 库存流水 | `/api/inventory-logs` | 流水查询 |
| 采购管理 | `/api/purchase-orders` | 采购订单管理 |
| 销售管理 | `/api/sales-orders` | 销售订单管理 |
| 数据统计 | `/api/statistics` | 统计数据与导出 |
| 仪表板 | `/api/dashboard` | 首页统计数据 |

详细接口文档请访问 Swagger UI 查看。

---

## ⚙️ 配置说明

### application.yml 主要配置项

```yaml
# 服务器配置
server:
  port: 8080  # 服务端口

# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auto-parts-backend
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 驼峰转换
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志
  global-config:
    db-config:
      id-type: auto  # 主键策略
      logic-delete-field: deleted  # 逻辑删除字段

# JWT 配置
jwt:
  secret: "your-secret-key"  # 密钥（至少 32 位）
  issuer: "auto-parts-backend"  # 签发者
  expiration-seconds: 28800  # 过期时间（秒），默认 8 小时

# Swagger 配置
springdoc:
  swagger-ui:
    path: /doc.html
  api-docs:
    path: /v3/api-docs
```

---

## 📝 开发指南

### 添加新接口

1. 在 `controller/` 目录创建 Controller 类
2. 使用 `@Tag` 标注接口分组
3. 使用 `@RequireRole` 标注权限要求

```java
@Tag(name = "示例管理")
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    @Operation(summary = "获取列表")
    @GetMapping("/list")
    @RequireRole({"ADMIN"})
    public Result<List<ExampleVO>> list() {
        // ...
    }
}
```

### 添加新实体

1. 在 `entity/` 目录创建实体类
2. 在 `mapper/` 目录创建 Mapper 接口
3. 在 `src/main/resources/mapper/` 创建 XML 映射文件（如需要）

---

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest
```

---

## 📄 许可证

MIT License

---

## 👤 作者

毕业设计项目 - 汽车配件管理系统

---

## 🔗 相关链接

- [前端项目](../auto-parts-frontend/README.md)
- [后端功能清单](docs/backend-features.md)
- [部署文档](docs/deployment.md)
