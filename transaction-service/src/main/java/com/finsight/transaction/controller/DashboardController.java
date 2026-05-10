package com.finsight.transaction.controller;

import com.finsight.transaction.dto.response.ApiResponse;
import com.finsight.transaction.dto.response.DailyVolumeResponse;
import com.finsight.transaction.dto.response.DashboardSummaryResponse;
import com.finsight.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Operational Dashboard", description = "Aggregated monitoring and reporting endpoints")
public class DashboardController {

    private final TransactionService transactionService;

    @GetMapping("/summary")
    @Operation(summary = "Get today's operational dashboard summary",
               description = "Returns active account count, today's volume, suspicious activity counts, and per-type breakdowns")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getDashboardSummary()));
    }

    @GetMapping("/daily-volume")
    @Operation(summary = "Get transaction volume report by day and type",
               description = "Backed by native SQL aggregation query with GROUP BY date and type")
    public ResponseEntity<ApiResponse<List<DailyVolumeResponse>>> getDailyVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getDailyVolumeReport(startDate, endDate)));
    }
}
