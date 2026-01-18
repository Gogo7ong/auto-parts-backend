package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.Part;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
public interface PartService extends IService<Part> {

    Page<Part> pageQuery(Page<Part> page, String partCode, String partName, String category);

    Part getByPartCode(String partCode);

    boolean checkPartCodeExists(String partCode, Long excludeId);
}
