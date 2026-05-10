package com.finsight.transaction.service.impl;

import com.finsight.transaction.dto.request.CreateTransactionRequest;
import com.finsight.transaction.dto.request.TransactionFilterRequest;
import com.finsight.transaction.dto.response.*;
import com.finsight.transaction.entity.Account;
import com.finsight.transaction.entity.Transaction;
import com.finsight.transaction.enums.TransactionStatus;
import com.finsight.transaction.enums.TransactionType;
import com.finsight.transaction.exception.BusinessRuleException;
import com.finsight.transaction.exception.DuplicateReferenceException;
import com.finsight.transaction.exception.InsufficientBalanceException;
import com.finsight.transaction.exception.ResourceNotFoundException;
import com.finsight.transaction.repository.AccountRepository;
import com.finsight.transaction.repository.TransactionRepository;
import com.finsight.transaction.service.SuspiciousActivityDetector;
import com.finsight.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository     accountRepository;
    private final SuspiciousActivityDetector suspiciousActivityDetector;

    @Override
    @Transactional
    public TransactionResponse processTransaction(CreateTransactionRequest request) {
        if (transactionRepository.existsByReferenceNumber(request.getReferenceNumber())) {
            throw new DuplicateReferenceException(request.getReferenceNumber());
        }

        Account sourceAccount      = resolveSourceAccount(request);
        Account destinationAccount = resolveDestinationAccount(request);

        validateTransactionRules(request, sourceAccount, destinationAccount);

        Transaction transaction = Transaction.builder()
                .referenceNumber(request.getReferenceNumber())
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .transactionDate(LocalDateTime.now())
                .build();

        Optional<String> suspiciousReason = suspiciousActivityDetector.detect(transaction, sourceAccount);
        suspiciousReason.ifPresent(transaction::markSuspicious);

        if (!transaction.isSuspicious()) {
            executeBalanceMovement(transaction, sourceAccount, destinationAccount);
            transaction.setStatus(TransactionStatus.COMPLETED);
        }

        if (sourceAccount != null) {
            accountRepository.save(sourceAccount);
        }
        if (destinationAccount != null) {
            accountRepository.save(destinationAccount);
        }

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction [{}] processed with status [{}]", saved.getReferenceNumber(), saved.getStatus());

        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(String id) {
        return transactionRepository.findById(id)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findByReferenceNumber(String referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", referenceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findByAccountId(String accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        return transactionRepository
                .findBySourceAccountIdOrDestinationAccountId(accountId, accountId)
                .stream()
                .map(TransactionResponse::from)
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuspiciousTransactionResponse> findSuspiciousTransactions(TransactionFilterRequest filter) {
        return transactionRepository.findSuspiciousTransactions(
                        filter.getAccountId(),
                        filter.getFromDate(),
                        filter.getToDate(),
                        filter.getMinAmount()
                ).stream()
                .map(SuspiciousTransactionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyVolumeResponse> getDailyVolumeReport(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findDailyVolumeReport(startDate, endDate)
                .stream()
                .map(DailyVolumeResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday   = startOfToday.plusDays(1).minusNanos(1);
        LocalDateTime sevenDaysAgo = startOfToday.minusDays(7);

        List<Transaction> todayTransactions = transactionRepository
                .findBySourceAccountIdOrDestinationAccountId(null, null)
                .stream()
                .filter(t -> !t.getTransactionDate().isBefore(startOfToday)
                        && !t.getTransactionDate().isAfter(endOfToday))
                .collect(Collectors.toList());

        Map<String, DashboardSummaryResponse.TypeBreakdown> volumeByType = todayTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionType().name(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                group -> {
                                    long count   = group.size();
                                    BigDecimal total = group.stream()
                                            .map(Transaction::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    BigDecimal avg = count > 0
                                            ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO;
                                    long suspicious = group.stream()
                                            .filter(Transaction::isSuspicious)
                                            .count();
                                    return DashboardSummaryResponse.TypeBreakdown.builder()
                                            .count(count)
                                            .totalAmount(total)
                                            .averageAmount(avg)
                                            .suspiciousCount(suspicious)
                                            .build();
                                }
                        )
                ));

        BigDecimal totalVolumeToday = volumeByType.values().stream()
                .map(DashboardSummaryResponse.TypeBreakdown::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long suspiciousToday = todayTransactions.stream()
                .filter(Transaction::isSuspicious)
                .count();

        long suspiciousTotal = transactionRepository.findAll().stream()
                .filter(Transaction::isSuspicious)
                .count();

        long totalActiveAccounts = accountRepository.findAll().stream()
                .filter(Account::isActive)
                .count();

        List<DailyVolumeResponse> last7DaysTrend = getDailyVolumeReport(sevenDaysAgo, endOfToday);

        List<AccountNetPositionResponse> topAccounts = accountRepository
                .findAccountNetPositions(sevenDaysAgo, endOfToday)
                .stream()
                .map(AccountNetPositionResponse::from)
                .limit(5)
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .reportDate(LocalDate.now())
                .totalActiveAccounts(totalActiveAccounts)
                .totalTransactionsToday(todayTransactions.size())
                .totalVolumeToday(totalVolumeToday)
                .suspiciousTransactionsToday(suspiciousToday)
                .suspiciousTransactionsTotal(suspiciousTotal)
                .volumeByType(volumeByType)
                .last7DaysTrend(last7DaysTrend)
                .topAccountsByNetFlow(topAccounts)
                .build();
    }

    private Account resolveSourceAccount(CreateTransactionRequest request) {
        if (TransactionType.CREDIT.equals(request.getTransactionType())) {
            return null;
        }
        if (request.getSourceAccountId() == null || request.getSourceAccountId().isBlank()) {
            throw new BusinessRuleException(
                    "Source account is required for " + request.getTransactionType() + " transactions",
                    "MISSING_SOURCE_ACCOUNT"
            );
        }
        return accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getSourceAccountId()));
    }

    private Account resolveDestinationAccount(CreateTransactionRequest request) {
        if (TransactionType.DEBIT.equals(request.getTransactionType())) {
            return null;
        }
        if (request.getDestinationAccountId() == null || request.getDestinationAccountId().isBlank()) {
            throw new BusinessRuleException(
                    "Destination account is required for " + request.getTransactionType() + " transactions",
                    "MISSING_DESTINATION_ACCOUNT"
            );
        }
        return accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getDestinationAccountId()));
    }

    private void validateTransactionRules(CreateTransactionRequest request,
                                          Account sourceAccount,
                                          Account destinationAccount) {
        if (sourceAccount != null && !sourceAccount.isActive()) {
            throw new BusinessRuleException(
                    "Source account " + sourceAccount.getAccountNumber() + " is not active (status: " + sourceAccount.getStatus() + ")",
                    "INACTIVE_SOURCE_ACCOUNT"
            );
        }

        if (destinationAccount != null && !destinationAccount.isActive()) {
            throw new BusinessRuleException(
                    "Destination account " + destinationAccount.getAccountNumber() + " is not active",
                    "INACTIVE_DESTINATION_ACCOUNT"
            );
        }

        if (TransactionType.TRANSFER.equals(request.getTransactionType())
                && sourceAccount != null && destinationAccount != null
                && sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BusinessRuleException("Cannot transfer to the same account", "SELF_TRANSFER_NOT_ALLOWED");
        }

        if (sourceAccount != null && !sourceAccount.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException(
                    sourceAccount.getAccountNumber(),
                    request.getAmount(),
                    sourceAccount.getBalance()
            );
        }
    }

    private void executeBalanceMovement(Transaction transaction, Account source, Account destination) {
        switch (transaction.getTransactionType()) {
            case CREDIT   -> destination.credit(transaction.getAmount());
            case DEBIT    -> source.debit(transaction.getAmount());
            case TRANSFER -> {
                source.debit(transaction.getAmount());
                destination.credit(transaction.getAmount());
            }
        }
    }
}
