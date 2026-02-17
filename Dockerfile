# 多阶段构建 - 后端 Dockerfile
# 使用 Eclipse Temurin (OpenJDK) 基础镜像

# ========================================
# 第一阶段：构建阶段（可选，如果已有 jar 包可跳过）
# ========================================
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /build

# 复制 Maven 配置文件（利用 Docker 缓存层）
COPY pom.xml .

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 打包应用（跳过测试）
RUN mvn clean package -DskipTests -B

# ========================================
# 第二阶段：运行阶段
# ========================================
FROM eclipse-temurin:17-jre-alpine

# 设置维护者信息
LABEL maintainer="auto-parts-management-system"
LABEL description="Auto Parts Management System Backend"
LABEL version="1.0.0"

# 设置工作目录
WORKDIR /app

# 创建非 root 用户（安全最佳实践）
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/auto-parts-backend-0.0.1-SNAPSHOT.jar app.jar

# 或者直接使用本地构建的 jar 包（注释掉上面的 COPY，取消下面这行的注释）
# COPY target/auto-parts-backend-0.0.1-SNAPSHOT.jar app.jar

# 设置文件所有者
RUN chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
