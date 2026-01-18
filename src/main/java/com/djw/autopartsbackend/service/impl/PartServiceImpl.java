package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.service.PartService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Service
public class PartServiceImpl extends ServiceImpl<PartMapper, Part> implements PartService {

    @Override
    public Page<Part> pageQuery(Page<Part> page, String partCode, String partName, String category) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(partCode), Part::getPartCode, partCode)
                .like(StringUtils.hasText(partName), Part::getPartName, partName)
                .eq(StringUtils.hasText(category), Part::getCategory, category)
                .orderByDesc(Part::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Part getByPartCode(String partCode) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getPartCode, partCode);
        return this.getOne(wrapper);
    }

    @Override
    public boolean checkPartCodeExists(String partCode, Long excludeId) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getPartCode, partCode);
        if (excludeId != null) {
            wrapper.ne(Part::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }
}
