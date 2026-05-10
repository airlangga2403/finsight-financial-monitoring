package com.finsight.audit.dto.response;

import com.finsight.audit.entity.ReconciliationRecord;
import com.finsight.audit.enums.ReconciliationStatus;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReconciliationResponse {
    private String id;
    private LocalDate reconciliationDate;
    private int totalAccounts;
    private int accountsWithGap;
    private BigDecimal totalCredited;
    private BigDecimal totalDebited;
    private BigDecimal netSystemFlow;
    private ReconciliationStatus status;
    private String notes;
    private LocalDateTime executedAt;

    public static ReconciliationResponse from(ReconciliationRecord r) {
        return ReconciliationResponse.builder()
                .id(r.getId())
                .reconciliationDate(r.getReconciliationDate())
                .totalAccounts(r.getTotalAccounts())
                .accountsWithGap(r.getAccountsWithGap())
                .totalCredited(r.getTotalCredited())
                .totalDebited(r.getTotalDebited())
                .netSystemFlow(r.getNetSystemFlow())
                .status(r.getStatus())
                .notes(r.getNotes())
                .executedAt(r.getExecutedAt())
                .build();
    }
}
