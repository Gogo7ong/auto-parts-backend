package com.djw.autopartsbackend.service;

import com.djw.autopartsbackend.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
public interface StatisticsService {

    List<InventoryStatisticsDTO> getInventoryStatistics(LocalDate startDate, LocalDate endDate, String periodType);

    List<TurnoverRateDTO> getTurnoverRateStatistics();

    List<SalesStatisticsDTO> getSalesStatistics(LocalDate startDate, LocalDate endDate, String periodType);

    List<PurchaseStatisticsDTO> getPurchaseStatistics(LocalDate startDate, LocalDate endDate, String periodType);
}
