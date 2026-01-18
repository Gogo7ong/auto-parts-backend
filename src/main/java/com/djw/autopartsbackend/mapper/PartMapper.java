package com.djw.autopartsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.djw.autopartsbackend.entity.Part;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Mapper
public interface PartMapper extends BaseMapper<Part> {
}
