package com.finsight.audit.repository;

import com.finsight.audit.entity.ReconciliationRecord;
import com.finsight.audit.enums.ReconciliationStatus;
import com.finsight.audit.projection.MonthlyReconciliationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationRepository extends JpaRepository<ReconciliationRecord, String> {

    Optional<ReconciliationRecord> findByReconciliationDate(LocalDate date);

    boolean existsByReconciliationDate(LocalDate date);

    List<ReconciliationRecord> findAllByOrderByReconciliationDateDesc();

    @Query(value = """
            SELECT
                EXTRACT(YEAR  FROM r.reconciliation_date)::INTEGER    AS year,
                EXTRACT(MONTH FROM r.reconciliation_date)::INTEGER    AS month,
                COUNT(r.id)                                            AS totalRuns,
                COUNT(CASE WHEN r.status = 'BALANCED'           THEN 1 END) AS balancedCount,
                COUNT(CASE WHEN r.status = 'DISCREPANCY_FOUND'  THEN 1 END) AS discrepancyCount,
                COALESCE(SUM(r.total_credited),   0)                   AS totalCreditedSum,
                COALESCE(SUM(r.total_debited),    0)                   AS totalDebitedSum,
                COALESCE(SUM(r.net_system_flow),  0)                   AS netSystemFlowSum
            FROM audit_svc.reconciliation_records r
            WHERE EXTRACT(YEAR FROM r.reconciliation_date) = :year
            GROUP BY
                EXTRACT(YEAR  FROM r.reconciliation_date),
                EXTRACT(MONTH FROM r.reconciliation_date)
            ORDER BY month ASC
            """, nativeQuery = true)
    List<MonthlyReconciliationProjection> findMonthlySummaryByYear(@Param("year") int year);

    @Query(value = """
            SELECT DISTINCT r.*
            FROM audit_svc.reconciliation_records r
            WHERE r.reconciliation_date BETWEEN :fromDate AND :toDate
              AND r.status = 'DISCREPANCY_FOUND'
              AND EXISTS (
                  SELECT 1
                  FROM audit_svc.reconciliation_line_items li
                  WHERE li.reconciliation_record_id = r.id
                    AND li.has_discrepancy = TRUE
                    AND li.discrepancy_amount >= :minDiscrepancy
              )
            ORDER BY r.reconciliation_date DESC
            """, nativeQuery = true)
    List<ReconciliationRecord> findDiscrepancyRecordsByDateRange(
            @Param("fromDate")        LocalDate fromDate,
            @Param("toDate")          LocalDate toDate,
            @Param("minDiscrepancy")  double minDiscrepancy
    );
}
