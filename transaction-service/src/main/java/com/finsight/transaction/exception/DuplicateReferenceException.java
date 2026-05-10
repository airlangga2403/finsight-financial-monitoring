package com.finsight.transaction.exception;

public class DuplicateReferenceException extends BusinessRuleException {

    public DuplicateReferenceException(String referenceNumber) {
        super("Transaction with reference number already exists: " + referenceNumber, "DUPLICATE_REFERENCE");
    }
}
