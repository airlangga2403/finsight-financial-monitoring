package com.finsight.transaction.repository;

import com.finsight.transaction.entity.Transaction;
import com.finsight.transaction.enums.TransactionStatus;
import com.finsight.transaction.projection.DailyVolumeProjection;
import com.finsight.transaction.projection.SuspiciousTransactionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    List<Transaction> findBySourceAccountIdOrDestinationAccountId(String sourceId, String destId);

    @Query(value = """
            SELECT
                DATE(t.transaction_date)                         AS transactionDay,
                t.transaction_type                               AS transactionType,
                COUNT(t.id)                                      AS totalCount,
                SUM(t.amount)                                    AS totalAmount,
                AVG(t.amount)                                    AS avgAmount,
                COUNT(CASE WHEN t.is_suspicious THEN 1 END)      AS suspiciousCount
            FROM transaction_svc.transactions t
            WHERE t.transaction_date BETWEEN :startDate AND :endDate
              AND t.status IN ('COMPLETED', 'FLAGGED')
            GROUP BY DATE(t.transaction_date), t.transaction_type
            ORDER BY DATE(t.transaction_date) DESC, SUM(t.amount) DESC
            """, nativeQuery = true)
    List<DailyVolumeProjection> findDailyVolumeReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
            SELECT
                t.id                    AS id,
                t.reference_number      AS referenceNumber,
                t.amount                AS amount,
                t.transaction_type      AS transactionType,
                t.status                AS status,
                t.suspicious_reason     AS suspiciousReason,
                t.transaction_date      AS transactionDate,
                sa.account_number       AS sourceAccountNumber,
                sa.holder_name          AS sourceHolderName,
                da.account_number       AS destAccountNumber,
                da.holder_name          AS destHolderName
            FROM transaction_svc.transactions t
            LEFT JOIN transaction_svc.accounts sa ON t.source_account_id      = sa.id
            LEFT JOIN transaction_svc.accounts da ON t.destination_account_id = da.id
            WHERE t.is_suspicious = TRUE
              AND (:accountId    IS NULL OR sa.id = :accountId  OR da.id = :accountId)
              AND (:fromDate     IS NULL OR t.transaction_date >= :fromDate)
              AND (:toDate       IS NULL OR t.transaction_date <= :toDate)
              AND (:minAmount    IS NULL OR t.amount >= :minAmount)
            ORDER BY t.transaction_date DESC
            """, nativeQuery = true)
    List<SuspiciousTransactionProjection> findSuspiciousTransactions(
            @Param("accountId")  String accountId,
            @Param("fromDate")   LocalDateTime fromDate,
            @Param("toDate")     LocalDateTime toDate,
            @Param("minAmount")  BigDecimal minAmount
    );

    @Query(value = """
            SELECT t FROM Transaction t
            WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId)
              AND t.transactionDate >= :since
              AND t.status != 'FAILED'
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findRecentTransactionsByAccount(
            @Param("accountId") String accountId,
            @Param("since") LocalDateTime since
    );

    @Query(value = """
            SELECT COALESCE(AVG(t.amount), 0)
            FROM transaction_svc.transactions t
            WHERE (t.source_account_id = :accountId OR t.destination_account_id = :accountId)
              AND t.status = 'COMPLETED'
            """, nativeQuery = true)
    BigDecimal findAverageTransactionAmountByAccount(@Param("accountId") String accountId);
}
