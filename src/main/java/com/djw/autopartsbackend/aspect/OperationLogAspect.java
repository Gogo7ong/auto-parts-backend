package com.djw.autopartsbackend.aspect;

import com.djw.autopartsbackend.common.annotation.OperationLog;
import com.djw.autopartsbackend.mapper.OperationLogMapper;
import com.djw.autopartsbackend.mapper.UserMapper;
import com.djw.autopartsbackend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 操作日志 AOP 切面
 * <p>拦截所有标注了 {@link OperationLog} 注解的方法，
 * 自动记录操作人、操作模块、请求参数、执行耗时、成功/失败状态等信息到数据库。</p>
 *
 * @author dengjiawen
 * @since 2026-02-17
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    private final JwtService jwtService;

    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogMapper operationLogMapper,
                               JwtService jwtService,
                               UserMapper userMapper,
                               ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 环绕通知：拦截 @OperationLog 注解的方法，记录操作日志
     *
     * @param joinPoint 切入点
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(com.djw.autopartsbackend.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        com.djw.autopartsbackend.entity.OperationLog operationLog =
                new com.djw.autopartsbackend.entity.OperationLog();
        operationLog.setModule(annotation.module());
        operationLog.setOperationType(annotation.type().name());
        operationLog.setDescription(annotation.description());

        HttpServletRequest request = resolveRequest();
        if (request != null) {
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setOperatorIp(resolveClientIp(request));
            fillOperatorInfo(operationLog, request);

            if (annotation.recordParams()) {
                operationLog.setRequestParams(serializeArgs(joinPoint.getArgs()));
            }
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            operationLog.setStatus(1);

            if (annotation.recordResult() && result != null) {
                try {
                    operationLog.setResponseResult(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.warn("操作日志：序列化响应结果失败: {}", e.getMessage());
                }
            }
        } catch (Throwable e) {
            operationLog.setStatus(0);
            String errorMsg = e.getMessage();
            if (StringUtils.hasText(errorMsg) && errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 1000);
            }
            operationLog.setErrorMessage(errorMsg);
            throw e;
        } finally {
            operationLog.setExecutionTime(System.currentTimeMillis() - startTime);
            saveLog(operationLog);
        }

        return result;
    }

    /**
     * 从 RequestContextHolder 获取当前请求
     *
     * @return 当前 HttpServletRequest，获取失败返回 null
     */
    private HttpServletRequest resolveRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析请求来源 IP（兼容反向代理场景）
     *
     * @param request 当前请求
     * @return 客户端 IP 地址
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个（真实来源）
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 从请求头 token 中解析操作人信息并填充到日志实体
     *
     * @param operationLog 日志实体
     * @param request      当前请求
     */
    private void fillOperatorInfo(com.djw.autopartsbackend.entity.OperationLog operationLog,
                                   HttpServletRequest request) {
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            Long userId = jwtService.parseUserId(token);
            operationLog.setOperatorId(userId);
            var user = userMapper.selectById(userId);
            if (user != null) {
                String name = StringUtils.hasText(user.getRealName())
                        ? user.getRealName()
                        : user.getUsername();
                operationLog.setOperatorName(name);
            }
        } catch (Exception e) {
            log.warn("操作日志：解析操作人信息失败: {}", e.getMessage());
        }
    }

    /**
     * 序列化方法参数为 JSON 字符串（过滤 Servlet 相关参数）
     *
     * @param args 方法参数数组
     * @return JSON 字符串，序列化失败返回 null
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            Object[] filteredArgs = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest)
                            && !(arg instanceof HttpServletResponse))
                    .toArray();
            String json = objectMapper.writeValueAsString(filteredArgs);
            // 防止超长参数写入数据库（TEXT 字段最大 65535 字节）
            if (json.length() > 5000) {
                json = json.substring(0, 5000) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            log.warn("操作日志：序列化请求参数失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 异步保存操作日志，避免日志记录失败影响主业务
     *
     * @param operationLog 待保存的日志实体
     */
    private void saveLog(com.djw.autopartsbackend.entity.OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("操作日志：保存失败: {}", e.getMessage());
        }
    }
}
