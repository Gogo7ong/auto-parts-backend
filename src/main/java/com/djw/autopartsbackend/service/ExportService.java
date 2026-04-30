package com.djw.autopartsbackend.service;

import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface ExportService {

    void exportSalesStatistics(HttpServletResponse response, LocalDate startDate, LocalDate endDate, String periodType) throws Exception;

    void exportPurchaseStatistics(HttpServletResponse response, LocalDate startDate, LocalDate endDate, String periodType) throws Exception;
}
