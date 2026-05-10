package com.finsight.transaction.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SuspiciousTransactionProjection {
    String getId();
    String getReferenceNumber();
    BigDecimal getAmount();
    String getTransactionType();
    String getStatus();
    String getSuspiciousReason();
    LocalDateTime getTransactionDate();
    String getSourceAccountNumber();
    String getSourceHolderName();
    String getDestAccountNumber();
    String getDestHolderName();
}
