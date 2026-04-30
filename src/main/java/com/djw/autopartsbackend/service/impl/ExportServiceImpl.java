package com.djw.autopartsbackend.service.impl;

import com.djw.autopartsbackend.dto.PurchaseStatisticsDTO;
import com.djw.autopartsbackend.dto.SalesStatisticsDTO;
import com.djw.autopartsbackend.service.ExportService;
import com.djw.autopartsbackend.service.StatisticsService;
import com.djw.autopartsbackend.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private StatisticsService statisticsService;

    @Override
    public void exportSalesStatistics(HttpServletResponse response,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      String periodType) throws Exception {
        List<SalesStatisticsDTO> data = statisticsService.getSalesStatistics(startDate, endDate, periodType);
        String fileName = "销售统计_" + startDate + "_" + endDate;
        ExcelExportUtil.export(response, fileName, data, SalesStatisticsDTO.class);
    }

    @Override
    public void exportPurchaseStatistics(HttpServletResponse response,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         String periodType) throws Exception {
        List<PurchaseStatisticsDTO> data = statisticsService.getPurchaseStatistics(startDate, endDate, periodType);
        String fileName = "采购统计_" + startDate + "_" + endDate;
        ExcelExportUtil.export(response, fileName, data, PurchaseStatisticsDTO.class);
    }
}
