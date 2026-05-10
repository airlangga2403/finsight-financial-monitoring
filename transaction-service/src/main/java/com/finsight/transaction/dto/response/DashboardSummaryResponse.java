package com.finsight.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardSummaryResponse {

    private LocalDate reportDate;
    private long totalActiveAccounts;
    private long totalTransactionsToday;
    private BigDecimal totalVolumeToday;
    private long suspiciousTransactionsToday;
    private long suspiciousTransactionsTotal;
    private Map<String, TypeBreakdown> volumeByType;
    private List<DailyVolumeResponse> last7DaysTrend;
    private List<AccountNetPositionResponse> topAccountsByNetFlow;

    @Getter
    @Builder
    public static class TypeBreakdown {
        private long count;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private long suspiciousCount;
    }
}
