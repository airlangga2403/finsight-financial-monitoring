package com.finsight.transaction.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyVolumeProjection {
    LocalDate getTransactionDay();
    String getTransactionType();
    Long getTotalCount();
    BigDecimal getTotalAmount();
    BigDecimal getAvgAmount();
    Long getSuspiciousCount();
}
