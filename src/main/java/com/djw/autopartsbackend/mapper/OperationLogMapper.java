package com.djw.autopartsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.djw.autopartsbackend.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 * 
 * @author dengjiawen
 * @since 2026-02-17
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
