package com.finsight.transaction.dto.response;

import com.finsight.transaction.projection.AccountNetPositionProjection;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountNetPositionResponse {
    private String id;
    private String accountNumber;
    private String holderName;
    private String accountType;
    private BigDecimal currentBalance;
    private BigDecimal totalCredited;
    private BigDecimal totalDebited;
    private BigDecimal netFlow;

    public static AccountNetPositionResponse from(AccountNetPositionProjection p) {
        return AccountNetPositionResponse.builder()
                .id(p.getId())
                .accountNumber(p.getAccountNumber())
                .holderName(p.getHolderName())
                .accountType(p.getAccountType())
                .currentBalance(p.getCurrentBalance())
                .totalCredited(p.getTotalCredited())
                .totalDebited(p.getTotalDebited())
                .netFlow(p.getNetFlow())
                .build();
    }
}
