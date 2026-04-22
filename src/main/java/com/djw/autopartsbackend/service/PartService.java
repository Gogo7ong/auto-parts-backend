package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.Part;

import java.util.List;

/**
 * @author dengjiawen
 * @since 2026-01-18
 */
public interface PartService extends IService<Part> {

    Page<Part> pageQuery(Page<Part> page, String partCode, String partName, String category);

    Part getByPartCode(String partCode);

    List<Part> listEnabled();

    boolean checkPartCodeExists(String partCode, Long excludeId);
}
