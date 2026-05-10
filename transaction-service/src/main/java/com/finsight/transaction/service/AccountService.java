package com.finsight.transaction.service;

import com.finsight.transaction.dto.request.CreateAccountRequest;
import com.finsight.transaction.dto.response.AccountNetPositionResponse;
import com.finsight.transaction.dto.response.AccountResponse;
import com.finsight.transaction.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    AccountResponse findById(String id);

    AccountResponse findByAccountNumber(String accountNumber);

    List<AccountResponse> findAll();

    List<AccountResponse> findByStatus(AccountStatus status);

    AccountResponse updateStatus(String id, AccountStatus newStatus);

    List<AccountNetPositionResponse> getAccountNetPositions(LocalDateTime startDate, LocalDateTime endDate);
}
