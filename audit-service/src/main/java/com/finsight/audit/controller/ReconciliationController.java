package com.finsight.audit.controller;

import com.finsight.audit.dto.response.ApiResponse;
import com.finsight.audit.dto.response.MonthlySummaryResponse;
import com.finsight.audit.dto.response.ReconciliationDetailResponse;
import com.finsight.audit.dto.response.ReconciliationResponse;
import com.finsight.audit.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation", description = "Trigger and query daily financial reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/run")
    @Operation(
        summary = "Trigger a reconciliation run for a specific date",
        description = """
            Calls transaction-service to fetch all active account net positions,
            evaluates balance integrity for each account, and persists the result.
            Returns BALANCED or DISCREPANCY_FOUND with full account breakdown.
            """
    )
    public ResponseEntity<ApiResponse<ReconciliationResponse>> runReconciliation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ReconciliationResponse result = reconciliationService.runReconciliation(date);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reconciliation completed for " + date, result));
    }

    @GetMapping("/history")
    @Operation(summary = "List all past reconciliation runs ordered by date descending")
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getHistory()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reconciliation detail by record ID (includes all line items)")
    public ResponseEntity<ApiResponse<ReconciliationDetailResponse>> getById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getReconciliationDetail(id)));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get reconciliation detail for a specific date")
    public ResponseEntity<ApiResponse<ReconciliationDetailResponse>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getReconciliationByDate(date)));
    }

    @GetMapping("/discrepancies")
    @Operation(
        summary = "Filter reconciliation runs with discrepancies above a threshold",
        description = "Backed by native SQL with EXISTS subquery. Useful for compliance and audit trail."
    )
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>> getDiscrepancies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0.01") double minDiscrepancy) {
        return ResponseEntity.ok(ApiResponse.ok(
                reconciliationService.getDiscrepancyHistory(fromDate, toDate, minDiscrepancy)));
    }

    @GetMapping("/monthly-summary")
    @Operation(
        summary = "Monthly reconciliation health summary",
        description = "Groups reconciliation records by month — shows balanced vs discrepancy ratio and system-wide flows. Backed by native SQL aggregation."
    )
    public ResponseEntity<ApiResponse<List<MonthlySummaryResponse>>> getMonthlySummary(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getMonthlySummary(year)));
    }
}
