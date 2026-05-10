package com.finsight.audit.service.impl;

import com.finsight.audit.client.TransactionServiceClient;
import com.finsight.audit.client.dto.AccountNetPositionDto;
import com.finsight.audit.dto.response.LineItemResponse;
import com.finsight.audit.dto.response.MonthlySummaryResponse;
import com.finsight.audit.dto.response.ReconciliationDetailResponse;
import com.finsight.audit.dto.response.ReconciliationResponse;
import com.finsight.audit.entity.ReconciliationLineItem;
import com.finsight.audit.entity.ReconciliationRecord;
import com.finsight.audit.enums.ReconciliationStatus;
import com.finsight.audit.repository.ReconciliationLineItemRepository;
import com.finsight.audit.repository.ReconciliationRepository;
import com.finsight.audit.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationServiceImpl implements ReconciliationService {

    private final TransactionServiceClient         transactionServiceClient;
    private final ReconciliationRepository         reconciliationRepository;
    private final ReconciliationLineItemRepository lineItemRepository;

    @Value("${finsight.reconciliation.discrepancy-tolerance:0.01}")
    private double discrepancyTolerance;

    @Override
    @Transactional
    public ReconciliationResponse runReconciliation(LocalDate date) {
        if (reconciliationRepository.existsByReconciliationDate(date)) {
            throw new IllegalArgumentException(
                    "Reconciliation for date " + date + " has already been executed. " +
                            "Retrieve it via GET /api/v1/reconciliation/date/" + date
            );
        }

        log.info("Starting reconciliation run for date: {}", date);

        List<AccountNetPositionDto> positions = transactionServiceClient.fetchAccountNetPositions(date);

        if (positions.isEmpty()) {
            log.warn("No account data returned from transaction-service for date {}", date);
            ReconciliationRecord emptyRecord = ReconciliationRecord.builder()
                    .reconciliationDate(date)
                    .totalAccounts(0)
                    .accountsWithGap(0)
                    .status(ReconciliationStatus.BALANCED)
                    .notes("No active accounts found for this date")
                    .build();
            return ReconciliationResponse.from(reconciliationRepository.save(emptyRecord));
        }

        List<ReconciliationLineItem> lineItems = buildLineItems(positions);

        BigDecimal totalCredited = lineItems.stream()
                .map(ReconciliationLineItem::getTotalCredited)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebited = lineItems.stream()
                .map(ReconciliationLineItem::getTotalDebited)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSystemFlow = totalCredited.subtract(totalDebited);

        long gapCount = lineItems.stream()
                .filter(ReconciliationLineItem::isHasDiscrepancy)
                .count();

        ReconciliationStatus status = lineItems.stream()
                .anyMatch(ReconciliationLineItem::isHasDiscrepancy)
                ? ReconciliationStatus.DISCREPANCY_FOUND
                : ReconciliationStatus.BALANCED;

        String notes = buildReconciliationNotes(lineItems, status);

        ReconciliationRecord record = ReconciliationRecord.builder()
                .reconciliationDate(date)
                .totalAccounts(positions.size())
                .accountsWithGap((int) gapCount)
                .totalCredited(totalCredited)
                .totalDebited(totalDebited)
                .netSystemFlow(netSystemFlow)
                .status(status)
                .notes(notes)
                .build();

        ReconciliationRecord saved = reconciliationRepository.save(record);

        lineItems.forEach(item -> item.setReconciliationRecord(saved));
        lineItemRepository.saveAll(lineItems);
        saved.setLineItems(lineItems);

        log.info("Reconciliation complete for {}: status={}, accounts={}, gaps={}",
                date, status, positions.size(), gapCount);

        return ReconciliationResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationDetailResponse getReconciliationDetail(String recordId) {
        ReconciliationRecord record = reconciliationRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Reconciliation record not found: " + recordId));
        return buildDetailResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationDetailResponse getReconciliationByDate(LocalDate date) {
        ReconciliationRecord record = reconciliationRepository.findByReconciliationDate(date)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No reconciliation found for date: " + date));
        return buildDetailResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getHistory() {
        return reconciliationRepository.findAllByOrderByReconciliationDateDesc()
                .stream()
                .map(ReconciliationResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getDiscrepancyHistory(
            LocalDate fromDate, LocalDate toDate, double minDiscrepancy) {

        return reconciliationRepository
                .findDiscrepancyRecordsByDateRange(fromDate, toDate, minDiscrepancy)
                .stream()
                .map(ReconciliationResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> getMonthlySummary(int year) {
        return reconciliationRepository.findMonthlySummaryByYear(year)
                .stream()
                .map(MonthlySummaryResponse::from)
                .collect(Collectors.toList());
    }

    private List<ReconciliationLineItem> buildLineItems(List<AccountNetPositionDto> positions) {
        BigDecimal tolerance = BigDecimal.valueOf(discrepancyTolerance);

        return positions.stream()
                .map(pos -> {
                    BigDecimal discrepancy = pos.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0
                            ? pos.getCurrentBalance().abs()
                            : BigDecimal.ZERO;

                    boolean hasGap = discrepancy.compareTo(tolerance) > 0;

                    return ReconciliationLineItem.builder()
                            .accountId(pos.getId())
                            .accountNumber(pos.getAccountNumber())
                            .holderName(pos.getHolderName())
                            .accountType(pos.getAccountType())
                            .reportedBalance(pos.getCurrentBalance())
                            .calculatedNetFlow(pos.getNetFlow())
                            .totalCredited(pos.getTotalCredited())
                            .totalDebited(pos.getTotalDebited())
                            .hasDiscrepancy(hasGap)
                            .discrepancyAmount(discrepancy)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String buildReconciliationNotes(List<ReconciliationLineItem> lineItems,
                                            ReconciliationStatus status) {
        if (ReconciliationStatus.BALANCED.equals(status)) {
            return String.format("All %d accounts reconciled successfully.", lineItems.size());
        }

        String discrepantAccounts = lineItems.stream()
                .filter(ReconciliationLineItem::isHasDiscrepancy)
                .map(item -> String.format("%s (%s) — gap: %.2f IDR",
                        item.getAccountNumber(), item.getHolderName(), item.getDiscrepancyAmount()))
                .collect(Collectors.joining("; "));

        return "Discrepancies found in: " + discrepantAccounts;
    }

    private ReconciliationDetailResponse buildDetailResponse(ReconciliationRecord record) {
        List<ReconciliationLineItem> items = lineItemRepository
                .findByReconciliationRecordId(record.getId());

        List<LineItemResponse> allLineItems = items.stream()
                .map(LineItemResponse::from)
                .collect(Collectors.toList());

        List<LineItemResponse> discrepancies = allLineItems.stream()
                .filter(LineItemResponse::isHasDiscrepancy)
                .collect(Collectors.toList());

        Map<String, Long> countByType = items.stream()
                .collect(Collectors.groupingBy(
                        ReconciliationLineItem::getAccountType,
                        Collectors.counting()
                ));
        log.debug("Line item breakdown by type: {}", countByType);

        return ReconciliationDetailResponse.builder()
                .summary(ReconciliationResponse.from(record))
                .lineItems(allLineItems)
                .discrepancies(discrepancies)
                .discrepancyCount(discrepancies.size())
                .build();
    }
}
