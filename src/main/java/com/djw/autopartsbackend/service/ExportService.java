package com.djw.autopartsbackend.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
public interface ExportService {

    void exportInventoryStatistics(HttpServletResponse response, LocalDate startDate, LocalDate endDate, String periodType) throws Exception;

    void exportTurnoverRateStatistics(HttpServletResponse response) throws Exception;

    void exportSalesStatistics(HttpServletResponse response, LocalDate startDate, LocalDate endDate, String periodType) throws Exception;

    void exportPurchaseStatistics(HttpServletResponse response, LocalDate startDate, LocalDate endDate, String periodType) throws Exception;
}
