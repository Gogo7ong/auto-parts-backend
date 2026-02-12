package com.djw.autopartsbackend.service;

import com.djw.autopartsbackend.dto.analytics.AnalyticsOverviewDTO;

import java.time.LocalDate;

public interface AnalyticsService {
    AnalyticsOverviewDTO getOverview(LocalDate startDate, LocalDate endDate, String granularity, Integer topN);
}

