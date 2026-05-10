package com.finsight.audit.dto.response;

import com.finsight.audit.projection.MonthlyReconciliationProjection;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class MonthlySummaryResponse {
    private int year;
    private int month;
    private String monthLabel;
    private long totalRuns;
    private long balancedCount;
    private long discrepancyCount;
    private double healthRate;
    private BigDecimal totalCreditedSum;
    private BigDecimal totalDebitedSum;
    private BigDecimal netSystemFlowSum;

    private static final String[] MONTH_NAMES = {
        "", "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public static MonthlySummaryResponse from(MonthlyReconciliationProjection p) {
        double healthRate = p.getTotalRuns() > 0
                ? (double) p.getBalancedCount() / p.getTotalRuns() * 100.0
                : 0.0;
        return MonthlySummaryResponse.builder()
                .year(p.getYear())
                .month(p.getMonth())
                .monthLabel(MONTH_NAMES[p.getMonth()])
                .totalRuns(p.getTotalRuns())
                .balancedCount(p.getBalancedCount())
                .discrepancyCount(p.getDiscrepancyCount())
                .healthRate(Math.round(healthRate * 100.0) / 100.0)
                .totalCreditedSum(p.getTotalCreditedSum())
                .totalDebitedSum(p.getTotalDebitedSum())
                .netSystemFlowSum(p.getNetSystemFlowSum())
                .build();
    }
}
