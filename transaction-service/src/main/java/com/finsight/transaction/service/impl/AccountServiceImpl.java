package com.finsight.transaction.service.impl;

import com.finsight.transaction.dto.request.CreateAccountRequest;
import com.finsight.transaction.dto.response.AccountNetPositionResponse;
import com.finsight.transaction.dto.response.AccountResponse;
import com.finsight.transaction.entity.Account;
import com.finsight.transaction.enums.AccountStatus;
import com.finsight.transaction.exception.BusinessRuleException;
import com.finsight.transaction.exception.ResourceNotFoundException;
import com.finsight.transaction.repository.AccountRepository;
import com.finsight.transaction.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new BusinessRuleException(
                    "Account number already exists: " + request.getAccountNumber(),
                    "DUPLICATE_ACCOUNT_NUMBER"
            );
        }

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .holderName(request.getHolderName())
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance())
                .currency(request.getCurrency() != null ? request.getCurrency() : "IDR")
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        log.info("New account created: {} for {}", saved.getAccountNumber(), saved.getHolderName());
        return AccountResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findById(String id) {
        return accountRepository.findById(id)
                .map(AccountResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(AccountResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findByStatus(AccountStatus status) {
        return accountRepository.findByStatus(status).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse updateStatus(String id, AccountStatus newStatus) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        if (AccountStatus.CLOSED.equals(account.getStatus()) && AccountStatus.ACTIVE.equals(newStatus)) {
            throw new BusinessRuleException(
                    "Closed accounts cannot be reactivated. Please create a new account.",
                    "INVALID_STATUS_TRANSITION"
            );
        }

        account.setStatus(newStatus);
        Account updated = accountRepository.save(account);
        log.info("Account [{}] status changed to {}", account.getAccountNumber(), newStatus);
        return AccountResponse.from(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountNetPositionResponse> getAccountNetPositions(LocalDateTime startDate, LocalDateTime endDate) {
        return accountRepository.findAccountNetPositions(startDate, endDate).stream()
                .map(AccountNetPositionResponse::from)
                .collect(Collectors.toList());
    }
}
