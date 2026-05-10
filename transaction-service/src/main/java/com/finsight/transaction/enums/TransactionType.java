package com.finsight.transaction.enums;

public enum TransactionType {
    CREDIT,     // Money coming into an account from external source
    DEBIT,      // Money going out of an account to external destination
    TRANSFER    // Internal movement between two accounts
}
