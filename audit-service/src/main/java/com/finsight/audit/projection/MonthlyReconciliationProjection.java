package com.finsight.audit.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyReconciliationProjection {
    Integer getYear();
    Integer getMonth();
    Long    getTotalRuns();
    Long    getBalancedCount();
    Long    getDiscrepancyCount();
    BigDecimal getTotalCreditedSum();
    BigDecimal getTotalDebitedSum();
    BigDecimal getNetSystemFlowSum();
}
