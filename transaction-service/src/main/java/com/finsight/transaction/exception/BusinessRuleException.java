package com.finsight.transaction.exception;

public class BusinessRuleException extends RuntimeException {
    private final String errorCode;

    public BusinessRuleException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
