# 部署文档

本文档介绍汽车配件管理系统的完整部署流程，包括开发环境、生产环境以及 Docker 容器化部署。

---

## 📋 目录

- [环境要求](#-环境要求)
- [开发环境部署](#-开发环境部署)
- [生产环境部署](#-生产环境部署)
- [Docker 容器化部署](#-docker-容器化部署)
- [Nginx 配置](#-nginx-配置)
- [常见问题](#-常见问题)

---

## 🔧 环境要求

### 服务器配置（推荐）

| 配置 | 最低要求 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 20GB | 50GB |
| 操作系统 | Linux (CentOS 7+/Ubuntu 18.04+) | - |

### 软件依赖

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 18+ | 前端构建（生产环境可选） |
| Nginx | 1.20+ | 反向代理（生产环境） |
| Docker | 20+ | 容器化部署（可选） |

---

## 🚀 开发环境部署

### 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库并执行初始化脚本
source /path/to/auto-parts-backend/src/main/resources/sql/init.sql

# （可选）导入模拟数据
source /path/to/auto-parts-backend/src/main/resources/sql/mock_data.sql
```

### 2. 后端服务

```bash
cd auto-parts-backend

# 修改数据库配置
# 编辑 src/main/resources/application.yml

# 启动服务
./mvnw spring-boot:run

# 验证启动成功
curl http://localhost:8080
```

### 3. 前端服务

```bash
cd auto-parts-frontend

# 安装依赖
npm install

# 配置环境变量
# 确认 .env.development 中 VITE_API_BASE_URL=http://localhost:8080/api

# 启动开发服务器
npm run dev
```

访问 http://localhost:5173

---

## 🌐 生产环境部署

### 方式一：传统部署

#### 1. 后端部署

```bash
# 1. 打包
cd auto-parts-backend
mvn clean package -DskipTests

# 2. 上传 jar 包到服务器
scp target/auto-parts-backend-0.0.1-SNAPSHOT.jar user@server:/opt/auto-parts/

# 3. 创建启动脚本
cat > /opt/auto-parts/start.sh << 'EOF'
#!/bin/bash
nohup java -jar -Xms512m -Xmx1g \
  -Dspring.profiles.active=prod \
  /opt/auto-parts/auto-parts-backend-0.0.1-SNAPSHOT.jar \
  > /opt/auto-parts/logs/app.log 2>&1 &
echo "应用启动成功，PID: $!"
EOF

chmod +x /opt/auto-parts/start.sh

# 4. 启动服务
/opt/auto-parts/start.sh
```

#### 2. 前端部署

```bash
cd auto-parts-frontend

# 1. 修改生产环境配置
# 编辑 .env.production
# VITE_API_BASE_URL=https://your-domain.com/api

# 2. 构建
npm run build

# 3. 上传到 Nginx 目录
scp -r dist/* user@server:/usr/share/nginx/html/
```

#### 3. Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Swagger 文档
    location /doc.html {
        proxy_pass http://localhost:8080/doc.html;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /webjars/ {
        proxy_pass http://localhost:8080/webjars/;
    }
    
    location /v3/ {
        proxy_pass http://localhost:8080/v3/;
    }
}
```

---

## 🐳 Docker 容器化部署

### 1. 构建后端镜像

```dockerfile
# auto-parts-backend/Dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/auto-parts-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1g"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 2. 构建前端镜像

```dockerfile
# auto-parts-frontend/Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 3. Docker Compose 部署

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: auto-parts-mysql
    environment:
      MYSQL_ROOT_PASSWORD: your_root_password
      MYSQL_DATABASE: auto-parts-backend
    volumes:
      - mysql_data:/var/lib/mysql
      - ./auto-parts-backend/src/main/resources/sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - auto-parts-network
    restart: always

  backend:
    build:
      context: ./auto-parts-backend
      dockerfile: Dockerfile
    container_name: auto-parts-backend
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/auto-parts-backend?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: your_root_password
    ports:
      - "8080:8080"
    networks:
      - auto-parts-network
    restart: always

  frontend:
    build:
      context: ./auto-parts-frontend
      dockerfile: Dockerfile
    container_name: auto-parts-frontend
    depends_on:
      - backend
    ports:
      - "80:80"
    networks:
      - auto-parts-network
    restart: always

volumes:
  mysql_data:

networks:
  auto-parts-network:
    driver: bridge
```

### 4. 启动容器

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启服务
docker-compose restart
```

---

## ⚙️ Nginx 配置

### 完整配置示例

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    keepalive_timeout 65;
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    server {
        listen 80;
        server_name your-domain.com;

        # 前端
        location / {
            root /usr/share/nginx/html;
            index index.html index.htm;
            try_files $uri $uri/ /index.html;
        }

        # 后端 API
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # 超时设置
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # Swagger
        location /doc.html {
            proxy_pass http://backend:8080/doc.html;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        location /webjars/ {
            proxy_pass http://backend:8080/webjars/;
        }

        location /v3/ {
            proxy_pass http://backend:8080/v3/;
        }

        # 错误页面
        error_page 404 /404.html;
        error_page 500 502 503 504 /50x.html;
        location = /50x.html {
            root /usr/share/nginx/html;
        }
    }
}
```

---

## 🔒 HTTPS 配置（可选）

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 其他配置同上...
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 🔍 常见问题

### 1. 后端启动失败

```bash
# 检查 Java 版本
java -version

# 检查端口占用
netstat -tlnp | grep 8080

# 查看日志
tail -f /opt/auto-parts/logs/app.log
```

### 2. 前端页面空白

- 检查浏览器控制台错误
- 确认 API 地址配置正确
- 检查 Nginx 配置和日志

### 3. 数据库连接失败

```bash
# 检查 MySQL 服务
systemctl status mysqld

# 测试连接
mysql -u root -p -e "SHOW DATABASES;"

# 检查防火墙
firewall-cmd --list-ports
```

### 4. 跨域问题

确保 Nginx 配置了正确的代理，或者在后端添加 CORS 配置。

---

## 📝 维护脚本

### 后端服务管理

```bash
#!/bin/bash
# /opt/auto-parts/manage.sh

APP_NAME="auto-parts-backend"
APP_JAR="$APP_NAME.jar"
LOG_FILE="logs/app.log"

case "$1" in
    start)
        nohup java -jar $APP_JAR > $LOG_FILE 2>&1 &
        echo "应用启动成功"
        ;;
    stop)
        pkill -f $APP_JAR
        echo "应用已停止"
        ;;
    restart)
        $0 stop
        sleep 2
        $0 start
        ;;
    status)
        pgrep -f $APP_JAR && echo "应用运行中" || echo "应用未运行"
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        ;;
esac
```

---

## 📞 技术支持

如遇到部署问题，请检查：
1. 服务器日志
2. 应用日志
3. 数据库连接状态
4. 网络配置
