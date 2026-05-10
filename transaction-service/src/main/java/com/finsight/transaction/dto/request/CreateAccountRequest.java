package com.finsight.transaction.dto.request;

import com.finsight.transaction.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^ACC-\\d{4}-[A-Z]{4}$", message = "Account number must match pattern ACC-XXXX-XXXX")
    private String accountNumber;

    @NotBlank(message = "Holder name is required")
    @Size(min = 3, max = 100, message = "Holder name must be between 3 and 100 characters")
    private String holderName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid balance format")
    private BigDecimal initialBalance;

    @Pattern(regexp = "^(IDR|USD)$", message = "Currency must be IDR or USD")
    private String currency = "IDR";
}
