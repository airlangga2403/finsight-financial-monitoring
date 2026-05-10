package com.finsight.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_record_id", nullable = false)
    private ReconciliationRecord reconciliationRecord;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "holder_name", nullable = false, length = 100)
    private String holderName;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "reported_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal reportedBalance;

    @Column(name = "calculated_net_flow", nullable = false, precision = 19, scale = 2)
    private BigDecimal calculatedNetFlow;

    @Column(name = "total_credited", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalCredited = BigDecimal.ZERO;

    @Column(name = "total_debited", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalDebited = BigDecimal.ZERO;

    @Column(name = "has_discrepancy", nullable = false)
    @Builder.Default
    private boolean hasDiscrepancy = false;

    @Column(name = "discrepancy_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal discrepancyAmount = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
