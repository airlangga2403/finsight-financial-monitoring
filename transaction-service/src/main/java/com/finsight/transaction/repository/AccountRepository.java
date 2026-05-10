package com.finsight.transaction.repository;

import com.finsight.transaction.entity.Account;
import com.finsight.transaction.enums.AccountStatus;
import com.finsight.transaction.enums.AccountType;
import com.finsight.transaction.projection.AccountNetPositionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByStatus(AccountStatus status);

    @Query(value = """
            SELECT
                a.id                                                                      AS id,
                a.account_number                                                          AS accountNumber,
                a.holder_name                                                             AS holderName,
                a.account_type                                                            AS accountType,
                a.balance                                                                 AS currentBalance,
                COALESCE(SUM(
                    CASE WHEN t.destination_account_id = a.id
                              AND t.status = 'COMPLETED'
                         THEN t.amount ELSE 0 END
                ), 0)                                                                     AS totalCredited,
                COALESCE(SUM(
                    CASE WHEN t.source_account_id = a.id
                              AND t.status = 'COMPLETED'
                         THEN t.amount ELSE 0 END
                ), 0)                                                                     AS totalDebited,
                COALESCE(SUM(
                    CASE WHEN t.destination_account_id = a.id AND t.status = 'COMPLETED' THEN  t.amount
                         WHEN t.source_account_id      = a.id AND t.status = 'COMPLETED' THEN -t.amount
                         ELSE 0 END
                ), 0)                                                                     AS netFlow
            FROM transaction_svc.accounts a
            LEFT JOIN transaction_svc.transactions t
                   ON (t.source_account_id = a.id OR t.destination_account_id = a.id)
                  AND t.transaction_date BETWEEN :startDate AND :endDate
            WHERE a.status = 'ACTIVE'
            GROUP BY a.id, a.account_number, a.holder_name, a.account_type, a.balance
            ORDER BY netFlow DESC
            """, nativeQuery = true)
    List<AccountNetPositionProjection> findAccountNetPositions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
