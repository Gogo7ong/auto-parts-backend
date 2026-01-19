package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Tag(name = "配件管理", description = "配件信息管理接口")
@RestController
@RequestMapping("/api/parts")
public class PartController {

    @Autowired
    private PartService partService;

    @Operation(summary = "获取所有配件（用于下拉选择）")
    @GetMapping("/all")
    public Result<List<Part>> getAll() {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getStatus, 1);
        List<Part> list = partService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "分页查询配件列表")
    @GetMapping("/page")
    public Result<PageResult<Part>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String partCode,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String category) {
        Page<Part> pagination = new Page<>(page, pageSize);
        Page<Part> result = partService.pageQuery(pagination, partCode, partName, category);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询配件详情")
    @GetMapping("/{id}")
    public Result<Part> getById(@PathVariable Long id) {
        Part part = partService.getById(id);
        return Result.success(part);
    }

    @Operation(summary = "新增配件")
    @PostMapping
    public Result<Void> add(@RequestBody Part part) {
        if (partService.checkPartCodeExists(part.getPartCode(), null)) {
            return Result.error("配件编号已存在");
        }
        partService.save(part);
        return Result.success();
    }

    @Operation(summary = "更新配件信息")
    @PutMapping
    public Result<Void> update(@RequestBody Part part) {
        if (partService.checkPartCodeExists(part.getPartCode(), part.getId())) {
            return Result.error("配件编号已存在");
        }
        partService.updateById(part);
        return Result.success();
    }

    @Operation(summary = "删除配件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        partService.removeById(id);
        return Result.success();
    }
}
