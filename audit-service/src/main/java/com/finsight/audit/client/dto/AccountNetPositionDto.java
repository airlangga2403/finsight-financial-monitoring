package com.finsight.audit.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountNetPositionDto {
    private String id;
    private String accountNumber;
    private String holderName;
    private String accountType;
    private BigDecimal currentBalance;
    private BigDecimal totalCredited;
    private BigDecimal totalDebited;
    private BigDecimal netFlow;
}
