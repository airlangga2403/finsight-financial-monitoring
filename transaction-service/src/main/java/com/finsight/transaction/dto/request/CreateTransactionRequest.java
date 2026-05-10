package com.finsight.transaction.dto.request;

import com.finsight.transaction.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {

    @NotBlank(message = "Reference number is required")
    @Pattern(regexp = "^TXN-\\d{8}-\\d{3}$", message = "Reference number must match pattern TXN-YYYYMMDD-NNN")
    private String referenceNumber;

    // Required for DEBIT and TRANSFER
    private String sourceAccountId;

    // Required for CREDIT and TRANSFER
    private String destinationAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum transaction amount is IDR 1,000")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
