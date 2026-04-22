package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.service.PartService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PartServiceImpl extends ServiceImpl<PartMapper, Part> implements PartService {

    private static final String CACHE_NAME = "part";

    @Override
    @Cacheable(value = CACHE_NAME, key = "'page:' + #page.current + ':' + #page.size + ':' + #partCode + ':' + #partName + ':' + #category")
    public Page<Part> pageQuery(Page<Part> page, String partCode, String partName, String category) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(partCode), Part::getPartCode, partCode)
                .like(StringUtils.hasText(partName), Part::getPartName, partName)
                .eq(StringUtils.hasText(category), Part::getCategory, category)
                .orderByDesc(Part::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'code:' + #partCode")
    public Part getByPartCode(String partCode) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getPartCode, partCode);
        return this.getOne(wrapper);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all_enabled'")
    public List<Part> listEnabled() {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Part::getStatus, 1)
                .orderByAsc(Part::getCategory)
                .orderByAsc(Part::getPartCode);
        return this.list(wrapper);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public Part getById(java.io.Serializable id) {
        return super.getById(id);
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

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean save(Part entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean updateById(Part entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean removeById(java.io.Serializable id) {
        return super.removeById(id);
    }
}
