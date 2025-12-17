package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.BestSellingItemDTO;
import com.example.finalexam_jvnc.dto.RevenueReportDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    RevenueReportDTO getRevenueReportByDay(LocalDate date);
    RevenueReportDTO getRevenueReportByMonth(int year, int month);
    List<BestSellingItemDTO> getBestSellingItems(int limit);
    RevenueReportDTO getOrderStatistics(LocalDate startDate, LocalDate endDate);
}

