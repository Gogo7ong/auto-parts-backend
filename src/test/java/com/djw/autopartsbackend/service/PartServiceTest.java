package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.entity.Part;
import com.djw.autopartsbackend.mapper.PartMapper;
import com.djw.autopartsbackend.service.impl.PartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PartService 单元测试类
 * 测试配件服务层的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PartService 单元测试")
class PartServiceTest {

    @Mock
    private PartMapper partMapper;

    @InjectMocks
    private PartServiceImpl partService;

    private Part testPart;

    @BeforeEach
    @DisplayName("初始化测试数据")
    void setUp() {
        // 创建测试配件数据
        testPart = new Part();
        testPart.setId(1L);
        testPart.setPartCode("P001");
        testPart.setPartName("刹车片");
        testPart.setSpecification("前轮");
        testPart.setBrand("Bosch");
        testPart.setSupplier("博世汽配");
        testPart.setUnitPrice(new BigDecimal("150.00"));
        testPart.setCategory("制动系统");
        testPart.setUnit("件");
        testPart.setMinStock(10);
        testPart.setDescription("高性能刹车片");
        testPart.setStatus(1);
        testPart.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试分页查询 - 无条件")
    void testPageQueryNoConditions() {
        // 准备数据
        Page<Part> page = new Page<>(1, 10);
        Page<Part> expectedPage = new Page<>(1, 10);
        expectedPage.getRecords().add(testPart);
        expectedPage.setTotal(1);

        // 模拟行为
        when(partMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        Page<Part> result = partService.pageQuery(page, null, null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("P001", result.getRecords().get(0).getPartCode());

        // 验证调用
        verify(partMapper, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("测试分页查询 - 按配件编号模糊查询")
    void testPageQueryByPartCode() {
        // 准备数据
        Page<Part> page = new Page<>(1, 10);
        Page<Part> expectedPage = new Page<>(1, 10);
        expectedPage.getRecords().add(testPart);
        expectedPage.setTotal(1);

        // 模拟行为
        when(partMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        Page<Part> result = partService.pageQuery(page, "P00", null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("P001", result.getRecords().get(0).getPartCode());
    }

    @Test
    @DisplayName("测试分页查询 - 按分类查询")
    void testPageQueryByCategory() {
        // 准备数据
        Page<Part> page = new Page<>(1, 10);
        Page<Part> expectedPage = new Page<>(1, 10);
        expectedPage.getRecords().add(testPart);
        expectedPage.setTotal(1);

        // 模拟行为
        when(partMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        Page<Part> result = partService.pageQuery(page, null, null, "制动系统");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("制动系统", result.getRecords().get(0).getCategory());
    }

    @Test
    @DisplayName("测试根据配件编号查询")
    void testGetByPartCode() {
        // 模拟行为
        when(partMapper.selectOne(any())).thenReturn(testPart);

        // 执行测试
        Part result = partService.getByPartCode("P001");

        // 验证结果
        assertNotNull(result);
        assertEquals("P001", result.getPartCode());
        assertEquals("刹车片", result.getPartName());
    }

    @Test
    @DisplayName("测试根据配件编号查询 - 不存在")
    void testGetByPartCodeNotFound() {
        // 模拟行为
        when(partMapper.selectOne(any())).thenReturn(null);

        // 执行测试
        Part result = partService.getByPartCode("P999");

        // 验证结果
        assertNull(result);
    }

    @Test
    @DisplayName("测试检查配件编号是否存在 - 存在")
    void testCheckPartCodeExistsTrue() {
        // 模拟行为
        when(partMapper.selectCount(any())).thenReturn(1L);

        // 执行测试
        boolean result = partService.checkPartCodeExists("P001", null);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查配件编号是否存在 - 不存在")
    void testCheckPartCodeExistsFalse() {
        // 模拟行为
        when(partMapper.selectCount(any())).thenReturn(0L);

        // 执行测试
        boolean result = partService.checkPartCodeExists("P999", null);

        // 验证结果
        assertFalse(result);
    }

    @Test
    @DisplayName("测试检查配件编号是否存在 - 排除自身")
    void testCheckPartCodeExistsExcludeSelf() {
        // 模拟行为：查询除了 ID 为 1 之外的记录，返回 0
        when(partMapper.selectCount(any())).thenReturn(0L);

        // 执行测试（更新时检查，排除自身）
        boolean result = partService.checkPartCodeExists("P001", 1L);

        // 验证结果：排除自身后不存在，返回 false
        assertFalse(result);
    }

    @Test
    @DisplayName("测试保存配件")
    void testSavePart() {
        // 模拟行为
        when(partMapper.insert(any(Part.class))).thenReturn(1);

        // 执行测试
        boolean result = partService.save(testPart);

        // 验证结果
        assertTrue(result);
        verify(partMapper, times(1)).insert(any(Part.class));
    }

    @Test
    @DisplayName("测试更新配件")
    void testUpdatePart() {
        // 准备更新数据
        testPart.setPartName("更新后的刹车片");
        testPart.setUnitPrice(new BigDecimal("180.00"));

        // 模拟行为
        when(partMapper.updateById(any(Part.class))).thenReturn(1);

        // 执行测试
        boolean result = partService.updateById(testPart);

        // 验证结果
        assertTrue(result);
        verify(partMapper, times(1)).updateById(any(Part.class));
    }

    @Test
    @DisplayName("测试删除配件")
    void testDeletePart() {
        // 模拟行为
        when(partMapper.deleteById(anyLong())).thenReturn(1);

        // 执行测试
        boolean result = partService.removeById(1L);

        // 验证结果
        assertTrue(result);
        verify(partMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("测试批量保存配件")
    void testSaveBatch() {
        // 准备数据
        Part part2 = new Part();
        part2.setPartCode("P002");
        part2.setPartName("机油滤清器");
        part2.setUnitPrice(new BigDecimal("50.00"));

        // 模拟行为
        when(partMapper.insert(any(Part.class))).thenReturn(1);

        // 执行测试
        boolean result = partService.saveBatch(java.util.Arrays.asList(testPart, part2));

        // 验证结果
        assertTrue(result);
    }
}
