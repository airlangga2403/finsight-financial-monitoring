package com.finsight.transaction.projection;

import java.math.BigDecimal;

public interface AccountNetPositionProjection {
    String getId();
    String getAccountNumber();
    String getHolderName();
    String getAccountType();
    BigDecimal getCurrentBalance();
    BigDecimal getTotalCredited();
    BigDecimal getTotalDebited();
    BigDecimal getNetFlow();
}
