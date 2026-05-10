package com.finsight.transaction.dto.response;

import com.finsight.transaction.projection.DailyVolumeProjection;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DailyVolumeResponse {
    private LocalDate transactionDay;
    private String transactionType;
    private long totalCount;
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
    private long suspiciousCount;

    public static DailyVolumeResponse from(DailyVolumeProjection p) {
        return DailyVolumeResponse.builder()
                .transactionDay(p.getTransactionDay())
                .transactionType(p.getTransactionType())
                .totalCount(p.getTotalCount())
                .totalAmount(p.getTotalAmount())
                .avgAmount(p.getAvgAmount())
                .suspiciousCount(p.getSuspiciousCount())
                .build();
    }
}
