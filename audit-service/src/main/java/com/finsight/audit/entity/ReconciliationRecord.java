package com.finsight.audit.entity;

import com.finsight.audit.enums.ReconciliationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reconciliation_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "reconciliation_date", nullable = false, unique = true)
    private LocalDate reconciliationDate;

    @Column(name = "total_accounts", nullable = false)
    @Builder.Default
    private int totalAccounts = 0;

    @Column(name = "accounts_with_gap", nullable = false)
    @Builder.Default
    private int accountsWithGap = 0;

    @Column(name = "total_credited", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalCredited = BigDecimal.ZERO;

    @Column(name = "total_debited", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalDebited = BigDecimal.ZERO;

    @Column(name = "net_system_flow", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal netSystemFlow = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private ReconciliationStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @OneToMany(mappedBy = "reconciliationRecord", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReconciliationLineItem> lineItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.executedAt == null) {
            this.executedAt = LocalDateTime.now();
        }
    }
}
