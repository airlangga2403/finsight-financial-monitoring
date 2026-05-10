package com.finsight.transaction.dto.response;

import com.finsight.transaction.projection.SuspiciousTransactionProjection;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SuspiciousTransactionResponse {
    private String id;
    private String referenceNumber;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private String suspiciousReason;
    private LocalDateTime transactionDate;
    private String sourceAccountNumber;
    private String sourceHolderName;
    private String destAccountNumber;
    private String destHolderName;

    public static SuspiciousTransactionResponse from(SuspiciousTransactionProjection p) {
        return SuspiciousTransactionResponse.builder()
                .id(p.getId())
                .referenceNumber(p.getReferenceNumber())
                .amount(p.getAmount())
                .transactionType(p.getTransactionType())
                .status(p.getStatus())
                .suspiciousReason(p.getSuspiciousReason())
                .transactionDate(p.getTransactionDate())
                .sourceAccountNumber(p.getSourceAccountNumber())
                .sourceHolderName(p.getSourceHolderName())
                .destAccountNumber(p.getDestAccountNumber())
                .destHolderName(p.getDestHolderName())
                .build();
    }
}
