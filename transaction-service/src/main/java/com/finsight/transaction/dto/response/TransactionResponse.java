package com.finsight.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finsight.transaction.entity.Transaction;
import com.finsight.transaction.enums.TransactionStatus;
import com.finsight.transaction.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private String id;
    private String referenceNumber;
    private String sourceAccountId;
    private String sourceAccountNumber;
    private String destinationAccountId;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private boolean suspicious;
    private String suspiciousReason;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .referenceNumber(txn.getReferenceNumber())
                .sourceAccountId(txn.getSourceAccount() != null ? txn.getSourceAccount().getId() : null)
                .sourceAccountNumber(txn.getSourceAccount() != null ? txn.getSourceAccount().getAccountNumber() : null)
                .destinationAccountId(txn.getDestinationAccount() != null ? txn.getDestinationAccount().getId() : null)
                .destinationAccountNumber(txn.getDestinationAccount() != null ? txn.getDestinationAccount().getAccountNumber() : null)
                .amount(txn.getAmount())
                .transactionType(txn.getTransactionType())
                .status(txn.getStatus())
                .description(txn.getDescription())
                .suspicious(txn.isSuspicious())
                .suspiciousReason(txn.getSuspiciousReason())
                .transactionDate(txn.getTransactionDate())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
