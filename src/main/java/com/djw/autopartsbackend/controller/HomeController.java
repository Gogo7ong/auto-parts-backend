package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Tag(name = "首页", description = "系统首页接口")
@RestController
public class HomeController {

    @Operation(summary = "系统首页")
    @GetMapping("/")
    public Result<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "欢迎使用汽车配件管理系统");
        data.put("version", "1.0.0");
        data.put("docs", "/doc.html");
        return Result.success(data);
    }
}
