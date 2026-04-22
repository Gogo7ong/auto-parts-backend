package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.ai.AiMetricsMonitor;
import com.djw.autopartsbackend.ai.AiUsageTracker;
import com.djw.autopartsbackend.biz.AiCustomerBiz;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.req.AiChatReq;
import com.djw.autopartsbackend.dto.req.PartRecommendReq;
import com.djw.autopartsbackend.dto.resp.AiChatResp;
import com.djw.autopartsbackend.dto.resp.PartRecommendationResp;
import com.djw.autopartsbackend.service.AiCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI智能客服控制器
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI智能客服", description = "基于通义千问的智能客服接口")
@RequiredArgsConstructor
public class AiCustomerController {

    private final AiCustomerBiz aiCustomerBiz;
    private final AiCustomerService aiCustomerService;

    /**
     * 智能问答接口
     *
     * @param req 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    @Operation(summary = "智能问答", description = "用户发送问题，AI返回回答")
    public Result<AiChatResp> chat(@Valid @RequestBody AiChatReq req) {
        AiChatResp resp = aiCustomerBiz.chat(req);
        return Result.success(resp);
    }

    /**
     * 流式智能问答接口（Server-Sent Events）
     * 逐token返回，提升用户体验
     *
     * @param req 对话请求
     * @return SSE事件流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式智能问答", description = "用户发送问题，AI流式返回回答（SSE）")
    public SseEmitter chatStream(@Valid @RequestBody AiChatReq req) {
        return aiCustomerBiz.chatStream(req);
    }

    /**
     * 配件推荐接口（结构化输出）
     * 根据车型、分类、预算推荐合适的配件
     *
     * @param req 推荐请求
     * @return 推荐结果
     */
    @PostMapping("/recommend")
    @Operation(summary = "配件推荐", description = "AI根据需求推荐合适的配件")
    public Result<PartRecommendationResp> recommendParts(@Valid @RequestBody PartRecommendReq req) {
        PartRecommendationResp result = aiCustomerService.recommendParts(
                req.getCarModel(),
                req.getCategory(),
                req.getBudget()
        );
        return Result.success(result);
    }

    /**
     * 动态角色对话
     * 支持不同角色和专业领域
     *
     * @param message 用户消息
     * @param role    角色（销售顾问、技术专家、售后客服）
     * @param domain  专业领域（发动机、制动系统、电气系统）
     * @return AI回复
     */
    @PostMapping("/chat/role")
    @Operation(summary = "角色对话", description = "指定AI角色和专业领域进行对话")
    public Result<String> chatWithRole(
            @RequestParam String message,
            @RequestParam(defaultValue = "销售顾问") String role,
            @RequestParam(defaultValue = "汽车配件") String domain) {
        String answer = aiCustomerService.chatWithRole(message, role, domain);
        return Result.success(answer);
    }

    /**
     * 导入知识库文档
     *
     * @param content 文档内容
     * @return 操作结果
     */
    @PostMapping("/knowledge/import")
    @Operation(summary = "导入知识库", description = "导入文档到RAG知识库")
    public Result<String> importKnowledge(@RequestBody String content) {
        aiCustomerService.importKnowledge(content);
        return Result.success("导入成功");
    }

    /**
     * 检索知识库
     *
     * @param query 查询文本
     * @return 相关文档列表
     */
    @GetMapping("/knowledge/search")
    @Operation(summary = "检索知识库", description = "语义检索知识库相关内容")
    public Result<Object> searchKnowledge(@RequestParam String query) {
        return Result.success(aiCustomerService.searchKnowledge(query));
    }

    /**
     * 获取AI使用统计
     *
     * @return 使用统计
     */
    @GetMapping("/stats/usage")
    @Operation(summary = "使用统计", description = "获取AI调用的Token使用量和成本统计")
    public Result<AiUsageTracker.GlobalUsage> getUsageStats() {
        return Result.success(aiCustomerService.getUsageStats());
    }

    /**
     * 获取AI监控指标
     *
     * @return 监控指标
     */
    @GetMapping("/stats/metrics")
    @Operation(summary = "监控指标", description = "获取AI调用的性能监控指标")
    public Result<AiMetricsMonitor.Metrics> getMetrics() {
        return Result.success(aiCustomerService.getMetrics());
    }
}
