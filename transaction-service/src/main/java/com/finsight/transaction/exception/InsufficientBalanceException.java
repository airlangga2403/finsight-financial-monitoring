package com.finsight.transaction.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessRuleException {

    public InsufficientBalanceException(String accountNumber, BigDecimal required, BigDecimal available) {
        super(String.format(
            "Insufficient balance for account %s. Required: %.2f, Available: %.2f",
            accountNumber, required, available
        ), "INSUFFICIENT_BALANCE");
    }
}
