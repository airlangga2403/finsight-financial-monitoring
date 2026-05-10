package com.finsight.audit.dto.response;

import com.finsight.audit.entity.ReconciliationLineItem;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class LineItemResponse {
    private String id;
    private String accountId;
    private String accountNumber;
    private String holderName;
    private String accountType;
    private BigDecimal reportedBalance;
    private BigDecimal calculatedNetFlow;
    private BigDecimal totalCredited;
    private BigDecimal totalDebited;
    private boolean hasDiscrepancy;
    private BigDecimal discrepancyAmount;

    public static LineItemResponse from(ReconciliationLineItem item) {
        return LineItemResponse.builder()
                .id(item.getId())
                .accountId(item.getAccountId())
                .accountNumber(item.getAccountNumber())
                .holderName(item.getHolderName())
                .accountType(item.getAccountType())
                .reportedBalance(item.getReportedBalance())
                .calculatedNetFlow(item.getCalculatedNetFlow())
                .totalCredited(item.getTotalCredited())
                .totalDebited(item.getTotalDebited())
                .hasDiscrepancy(item.isHasDiscrepancy())
                .discrepancyAmount(item.getDiscrepancyAmount())
                .build();
    }
}
