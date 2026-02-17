package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.dto.SalesOrderDTO;
import com.djw.autopartsbackend.entity.SalesOrder;
import com.djw.autopartsbackend.entity.SalesOrderItem;
import com.djw.autopartsbackend.mapper.SalesOrderMapper;
import com.djw.autopartsbackend.mapper.SalesOrderItemMapper;
import com.djw.autopartsbackend.service.impl.SalesOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SalesOrderService 单元测试类
 * 测试销售订单服务层的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SalesOrderService 单元测试")
class SalesOrderServiceTest {

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private SalesOrderItemMapper salesOrderItemMapper;

    @Mock
    private InventoryLogService inventoryLogService;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    private SalesOrder testOrder;
    private SalesOrderItem testOrderItem;
    private SalesOrderDTO testOrderDTO;

    @BeforeEach
    @DisplayName("初始化测试数据")
    void setUp() {
        // 创建测试销售订单
        testOrder = new SalesOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("SO202502170001");
        testOrder.setCustomerName("张三");
        testOrder.setCustomerPhone("13800138000");
        testOrder.setCustomerAddress("北京市朝阳区");
        testOrder.setTotalAmount(new BigDecimal("500.00"));
        testOrder.setStatus("PENDING");
        testOrder.setCreateUserId(1L);
        testOrder.setCreateUserName("管理员");
        testOrder.setRemark("测试订单");
        testOrder.setCreateTime(LocalDateTime.now());

        // 创建测试订单明细
        testOrderItem = new SalesOrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrderId(1L);
        testOrderItem.setPartId(1L);
        testOrderItem.setPartCode("P001");
        testOrderItem.setPartName("刹车片");
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(new BigDecimal("250.00"));
        testOrderItem.setTotalPrice(new BigDecimal("500.00"));

        // 创建测试 DTO
        testOrderDTO = new SalesOrderDTO();
        testOrderDTO.setOrder(testOrder);
        List<SalesOrderItem> items = new ArrayList<>();
        items.add(testOrderItem);
        testOrderDTO.setItems(items);
    }

    @Test
    @DisplayName("测试根据订单号查询")
    void testGetByOrderNo() {
        // 模拟行为
        when(salesOrderMapper.selectOne(any())).thenReturn(testOrder);

        // 执行测试
        SalesOrder result = salesOrderService.getByOrderNo("SO202502170001");

        // 验证结果
        assertNotNull(result);
        assertEquals("SO202502170001", result.getOrderNo());
        assertEquals("张三", result.getCustomerName());
    }

    @Test
    @DisplayName("测试根据订单号查询 - 不存在")
    void testGetByOrderNoNotFound() {
        // 模拟行为
        when(salesOrderMapper.selectOne(any())).thenReturn(null);

        // 执行测试
        SalesOrder result = salesOrderService.getByOrderNo("SO999");

        // 验证结果
        assertNull(result);
    }

    @Test
    @DisplayName("测试分页查询 - 无条件")
    void testPageQueryNoConditions() {
        // 准备数据
        Page<SalesOrder> page = new Page<>(1, 10);
        Page<SalesOrder> expectedPage = new Page<>(1, 10);
        expectedPage.getRecords().add(testOrder);
        expectedPage.setTotal(1);

        // 模拟行为
        when(salesOrderMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        Page<SalesOrder> result = salesOrderService.pageQuery(page, null, null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("测试分页查询 - 按客户名称查询")
    void testPageQueryByCustomerName() {
        // 准备数据
        Page<SalesOrder> page = new Page<>(1, 10);
        Page<SalesOrder> expectedPage = new Page<>(1, 10);
        expectedPage.getRecords().add(testOrder);
        expectedPage.setTotal(1);

        // 模拟行为
        when(salesOrderMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        Page<SalesOrder> result = salesOrderService.pageQuery(page, null, "张三", null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试分页查询 - 按状态查询")
    void testPageQueryByStatus() {
        // 准备数据
        Page<SalesOrder> page = new Page<>(1, 10);
        Page<SalesOrder> expectedPage = new Page<>(1, 10);
        expectedPage.getRecords().add(testOrder);
        expectedPage.setTotal(1);

        // 模拟行为
        when(salesOrderMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        Page<SalesOrder> result = salesOrderService.pageQuery(page, null, null, "PENDING");

        // 验证结果
        assertNotNull(result);
        assertEquals("PENDING", result.getRecords().get(0).getStatus());
    }

    @Test
    @DisplayName("测试创建销售订单（含明细）")
    void testCreateOrderWithItems() {
        // 模拟行为
        when(salesOrderMapper.insert(any(SalesOrder.class))).thenReturn(1);
        when(salesOrderItemMapper.insert(any(SalesOrderItem.class))).thenReturn(1);

        // 执行测试
        boolean result = salesOrderService.createOrderWithItems(testOrderDTO);

        // 验证结果
        assertTrue(result);
        verify(salesOrderMapper, times(1)).insert(any(SalesOrder.class));
        verify(salesOrderItemMapper, atLeastOnce()).insert(any(SalesOrderItem.class));
    }

    @Test
    @DisplayName("测试创建销售订单 - 自动生成订单号")
    void testCreateOrderWithItemsAutoGenerateOrderNo() {
        // 准备数据（不设置订单号）
        testOrder.setOrderNo(null);
        testOrderDTO.setOrder(testOrder);

        // 模拟行为
        when(salesOrderMapper.insert(any(SalesOrder.class))).thenReturn(1);
        when(salesOrderItemMapper.insert(any(SalesOrderItem.class))).thenReturn(1);

        // 执行测试
        boolean result = salesOrderService.createOrderWithItems(testOrderDTO);

        // 验证结果
        assertTrue(result);
        assertNotNull(testOrder.getOrderNo());
        assertTrue(testOrder.getOrderNo().startsWith("SO"));
    }

    @Test
    @DisplayName("测试出库操作")
    void testShipOrder() {
        // 准备数据
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setStatus("PENDING");

        // 模拟行为
        when(salesOrderMapper.selectById(1L)).thenReturn(order);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // 执行测试
        boolean result = salesOrderService.shipOrder(1L, 2L, "仓库管理员");

        // 验证结果
        assertTrue(result);
        assertEquals("SHIPPED", order.getStatus());
        assertNotNull(order.getWarehouseTime());
    }

    @Test
    @DisplayName("测试完成订单")
    void testCompleteOrder() {
        // 准备数据
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setStatus("SHIPPED");

        // 模拟行为
        when(salesOrderMapper.selectById(1L)).thenReturn(order);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // 执行测试
        boolean result = salesOrderService.completeOrder(1L);

        // 验证结果
        assertTrue(result);
        assertEquals("COMPLETED", order.getStatus());
    }

    @Test
    @DisplayName("测试退货操作")
    void testReturnOrder() {
        // 准备数据
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setStatus("SHIPPED");

        // 模拟行为
        when(salesOrderMapper.selectById(1L)).thenReturn(order);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // 执行测试
        boolean result = salesOrderService.returnOrder(1L);

        // 验证结果
        assertTrue(result);
        assertEquals("RETURNED", order.getStatus());
    }

    @Test
    @DisplayName("测试更新销售订单（含明细）")
    void testUpdateOrderWithItems() {
        // 模拟行为
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderMapper.updateById(any(SalesOrder.class))).thenReturn(1);

        // 执行测试
        boolean result = salesOrderService.updateOrderWithItems(1L, testOrderDTO);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试获取订单详情（含明细）")
    void testGetOrderWithItems() {
        // 模拟行为
        when(salesOrderMapper.selectById(1L)).thenReturn(testOrder);
        when(salesOrderItemMapper.selectList(any())).thenReturn(List.of(testOrderItem));

        // 执行测试
        SalesOrderDTO result = salesOrderService.getOrderWithItems(1L);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOrder());
        assertNotNull(result.getItems());
        assertFalse(result.getItems().isEmpty());
    }
}
