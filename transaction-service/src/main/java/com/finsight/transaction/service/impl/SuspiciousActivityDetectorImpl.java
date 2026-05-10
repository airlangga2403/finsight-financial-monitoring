package com.finsight.transaction.service.impl;

import com.finsight.transaction.entity.Account;
import com.finsight.transaction.entity.Transaction;
import com.finsight.transaction.enums.TransactionStatus;
import com.finsight.transaction.repository.TransactionRepository;
import com.finsight.transaction.service.SuspiciousActivityDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuspiciousActivityDetectorImpl implements SuspiciousActivityDetector {

    private final TransactionRepository transactionRepository;

    @Value("${finsight.business.suspicious-velocity-threshold:5}")
    private int velocityThreshold;

    @Value("${finsight.business.suspicious-amount-multiplier:3.0}")
    private double amountMultiplier;

    private static final BigDecimal LARGE_AMOUNT_FLOOR = new BigDecimal("100000000");

    @Override
    public Optional<String> detect(Transaction transaction, Account sourceAccount) {
        List<String> triggeredRules = new ArrayList<>();

        if (sourceAccount != null) {
            checkVelocityBreach(sourceAccount).ifPresent(triggeredRules::add);
            checkLargeAmountAnomaly(transaction, sourceAccount).ifPresent(triggeredRules::add);
        }

        checkRoundAmountPattern(transaction).ifPresent(triggeredRules::add);

        if (triggeredRules.isEmpty()) {
            return Optional.empty();
        }

        String combinedReason = String.join("; ", triggeredRules);
        log.warn("Suspicious activity detected for txn [{}]: {}", transaction.getReferenceNumber(), combinedReason);
        return Optional.of(combinedReason);
    }

    private Optional<String> checkVelocityBreach(Account sourceAccount) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        List<Transaction> recentTxns = transactionRepository
                .findRecentTransactionsByAccount(sourceAccount.getId(), oneHourAgo);

        long outgoingCount = recentTxns.stream()
                .filter(t -> sourceAccount.getId().equals(
                        t.getSourceAccount() != null ? t.getSourceAccount().getId() : null
                ))
                .filter(t -> !TransactionStatus.FAILED.equals(t.getStatus()))
                .count();

        if (outgoingCount >= velocityThreshold) {
            return Optional.of(String.format(
                    "VELOCITY_BREACH: %d outgoing transactions in last 60 minutes (threshold: %d)",
                    outgoingCount, velocityThreshold
            ));
        }
        return Optional.empty();
    }

    private Optional<String> checkLargeAmountAnomaly(Transaction transaction, Account sourceAccount) {
        if (transaction.getAmount().compareTo(LARGE_AMOUNT_FLOOR) < 0) {
            return Optional.empty();
        }

        BigDecimal historicalAverage = transactionRepository
                .findAverageTransactionAmountByAccount(sourceAccount.getId());

        if (historicalAverage.compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }

        BigDecimal threshold = historicalAverage.multiply(BigDecimal.valueOf(amountMultiplier))
                .setScale(2, RoundingMode.HALF_UP);

        if (transaction.getAmount().compareTo(threshold) > 0) {
            return Optional.of(String.format(
                    "LARGE_AMOUNT_ANOMALY: Amount %.2f exceeds %.1fx historical avg of %.2f (threshold: %.2f)",
                    transaction.getAmount(), amountMultiplier, historicalAverage, threshold
            ));
        }
        return Optional.empty();
    }

    private Optional<String> checkRoundAmountPattern(Transaction transaction) {
        if (transaction.getAmount().compareTo(LARGE_AMOUNT_FLOOR) < 0) {
            return Optional.empty();
        }

        List<BigDecimal> suspiciousDenominators = List.of(
                new BigDecimal("500000000"),
                new BigDecimal("1000000000"),
                new BigDecimal("5000000000")
        );

        boolean isRoundAmount = suspiciousDenominators.stream()
                .anyMatch(denom -> transaction.getAmount()
                        .remainder(denom)
                        .compareTo(BigDecimal.ZERO) == 0
                );

        if (isRoundAmount) {
            return Optional.of(String.format(
                    "ROUND_AMOUNT_PATTERN: Amount %.2f is a large round figure — possible structuring",
                    transaction.getAmount()
            ));
        }
        return Optional.empty();
    }
}
